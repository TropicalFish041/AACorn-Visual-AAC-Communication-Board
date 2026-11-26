package use_case.customize_board;

import entity.CommunicationBoard;
import entity.LinkedVocabulary;
import entity.Pictogram;
import entity.WordType;

import java.io.UncheckedIOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

/** Searches for pictograms and applies validated edits to the child's vocabulary. */
public final class CustomizeBoardInteractor implements CustomizeBoardInputBoundary {
    private final BoardCustomizationDataAccess dataAccess;
    private final PictogramSearchDataAccessInterface search;
    private final CustomizeBoardOutputBoundary presenter;

    public CustomizeBoardInteractor(BoardCustomizationDataAccess dataAccess,
                                    PictogramSearchDataAccessInterface search,
                                    CustomizeBoardOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.search = search;
        this.presenter = presenter;
    }

    @Override
    public void loadBoard() {
        showBoard(dataAccess.getBoard(), dataAccess.getLinks(), " ");
    }

    @Override
    public void search(String keyword) {
        if (keyword == null || keyword.isBlank()) {
            presenter.showSearchResults(List.of(), "Enter a word to search.");
            return;
        }

        try {
            List<PictogramData> results = toData(search.searchPictograms(keyword.strip()));
            presenter.showSearchResults(results, results.isEmpty() ? "No pictograms found." : " ");
        } catch (PictogramSearchException exception) {
            presenter.showSearchResults(List.of(), "ARASAAC search failed. Try again in a moment.");
        }
    }

    @Override
    public void addToBoard(PictogramData result, boolean action) {
        edit((board, links) -> board.addPictogram(toPictogram(result, action)),
                "Added " + result.getLabel() + " to the board.");
    }

    @Override
    public void removeFromBoard(int pictogramId) {
        edit((board, links) -> {
            Pictogram pictogram = find(board.getPictograms(), pictogramId, "That tile is no longer on the board.");
            board.removePictogram(pictogram);
            links.removeAllLinks(pictogram);
        }, "Removed tile from the board.");
    }

    @Override
    public void linkThing(int actionId, PictogramData result) {
        edit((board, links) -> links.link(findAction(board, actionId), toPictogram(result, false)),
                "Linked " + result.getLabel() + " to the selected action.");
    }

    @Override
    public void unlinkThing(int actionId, int thingId) {
        edit((board, links) -> {
            Pictogram action = findAction(board, actionId);
            Pictogram thing = find(links.getThings(action), thingId, "That thing is no longer linked.");
            links.unlink(action, thing);
        }, "Removed link.");
    }

    private void edit(BiConsumer<CommunicationBoard, LinkedVocabulary> change, String message) {
        // Save copies so a rejected edit or failed write leaves the active profile intact.
        CommunicationBoard current = dataAccess.getBoard();
        CommunicationBoard board = new CommunicationBoard(current.getCapacity());
        current.getPictograms().forEach(board::addPictogram);

        LinkedVocabulary currentLinks = dataAccess.getLinks();
        LinkedVocabulary links = new LinkedVocabulary();
        for (Pictogram action : currentLinks.getLinkedActions()) {
            for (Pictogram thing : currentLinks.getThings(action)) {
                links.link(action, thing);
            }
        }

        try {
            change.accept(board, links);
            dataAccess.saveVocabulary(board, links);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            presenter.showValidationError(exception.getMessage());
            return;
        } catch (UncheckedIOException exception) {
            presenter.showValidationError("Could not save the board. Your changes were not saved.");
            return;
        }
        showBoard(board, links, message);
    }

    private void showBoard(CommunicationBoard board, LinkedVocabulary links, String message) {
        Map<Integer, List<PictogramData>> linkedThings = new LinkedHashMap<>();
        for (Pictogram tile : board.getPictograms()) {
            if (tile.isAction()) {
                linkedThings.put(tile.getId(), toData(links.getThings(tile)));
            }
        }
        presenter.showBoard(toData(board.getPictograms()), linkedThings, board.getCapacity(), message);
    }

    private static List<PictogramData> toData(List<Pictogram> pictograms) {
        return pictograms.stream().map(pictogram -> new PictogramData(
                pictogram.getId(), pictogram.getLabel(), pictogram.getImageLocation(), pictogram.isAction()))
                .collect(Collectors.toUnmodifiableList());
    }

    private static Pictogram toPictogram(PictogramData result, boolean action) {
        return new Pictogram(result.getId(), result.getLabel(), result.getImageLocation(),
                action ? WordType.ACTION : WordType.THING);
    }

    private static Pictogram findAction(CommunicationBoard board, int actionId) {
        Pictogram action = find(board.getPictograms(), actionId, "That action is no longer on the board.");
        if (!action.isAction()) {
            throw new IllegalArgumentException("Select an action on the board first.");
        }
        return action;
    }

    private static Pictogram find(List<Pictogram> pictograms, int id, String message) {
        return pictograms.stream().filter(item -> item.getId() == id).findFirst()
                .orElseThrow(() -> new IllegalArgumentException(message));
    }
}
