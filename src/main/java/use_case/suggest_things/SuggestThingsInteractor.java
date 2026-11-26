package use_case.suggest_things;

import entity.CommunicationBoard;
import entity.CommunicationMessage;
import entity.LinkedVocabulary;
import entity.Pictogram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Suggests things to link to an action on the board.
 *
 * <p>Order of suggestions:
 * <ol>
 *   <li>Things the child has used with this action before (most used first).</li>
 *   <li>Things from the built-in starter list.</li>
 * </ol>
 * Things already linked to the action are skipped, and nothing is changed here:
 * the caregiver decides what to add.
 */
public class SuggestThingsInteractor implements SuggestThingsInputBoundary {

    static final int MAX_SUGGESTIONS = 6;
    static final String ACTION_NOT_FOUND_MESSAGE = "This action is not on the board.";
    static final String NOT_AN_ACTION_MESSAGE = "Suggestions are only for actions.";
    static final String NO_SUGGESTIONS_MESSAGE = "No suggestions yet. Search to add a thing.";

    private final SuggestThingsDataAccessInterface profileDataAccess;
    private final StarterThingsDataAccessInterface starterDataAccess;
    private final SuggestThingsOutputBoundary presenter;

    public SuggestThingsInteractor(SuggestThingsDataAccessInterface profileDataAccess,
                                   StarterThingsDataAccessInterface starterDataAccess,
                                   SuggestThingsOutputBoundary presenter) {
        this.profileDataAccess =
                Objects.requireNonNull(profileDataAccess, "profileDataAccess cannot be null");
        this.starterDataAccess =
                Objects.requireNonNull(starterDataAccess, "starterDataAccess cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "presenter cannot be null");
    }

    @Override
    public void execute(SuggestThingsInputData inputData) {
        Pictogram action = findOnBoard(profileDataAccess.getBoard(), inputData.getActionId());
        if (action == null) {
            presenter.prepareFailView(ACTION_NOT_FOUND_MESSAGE);
            return;
        }
        if (!action.isAction()) {
            presenter.prepareFailView(NOT_AN_ACTION_MESSAGE);
            return;
        }

        LinkedVocabulary links = profileDataAccess.getLinks();
        List<Pictogram> alreadyLinked = links.getThings(action);
        List<SuggestThingsOutputData.Suggestion> suggestions = new ArrayList<>();
        List<Pictogram> added = new ArrayList<>();

        // 1. From history (most used first).
        Map<Pictogram, Integer> counts = countThingsUsedWith(action);
        for (Map.Entry<Pictogram, Integer> entry : rank(counts)) {
            addSuggestion(entry.getKey(), entry.getValue(), alreadyLinked, added, suggestions);
        }

        // 2. From the starter list.
        for (Pictogram thing : starterDataAccess.getStarterThings(action.getLabel())) {
            addSuggestion(thing, 0, alreadyLinked, added, suggestions);
        }

        if (suggestions.isEmpty()) {
            presenter.prepareFailView(NO_SUGGESTIONS_MESSAGE);
            return;
        }

        presenter.prepareSuccessView(
                new SuggestThingsOutputData(action.getLabel(), suggestions));
    }

    /** Returns the pictogram on the board with this id, or null if there is none. */
    private Pictogram findOnBoard(CommunicationBoard board, int id) {
        for (Pictogram pictogram : board.getPictograms()) {
            if (pictogram.getId() == id) {
                return pictogram;
            }
        }
        return null;
    }

    /**
     * Counts the things that appeared in the same message as this action.
     * Newer messages are looked at first, so on a tie the more recent thing comes first.
     */
    private Map<Pictogram, Integer> countThingsUsedWith(Pictogram action) {
        List<CommunicationMessage> history = profileDataAccess.getHistory();
        Map<Pictogram, Integer> counts = new LinkedHashMap<>();

        for (int i = history.size() - 1; i >= 0; i--) {
            List<Pictogram> pictograms = history.get(i).getSelectedPictograms();
            if (!pictograms.contains(action)) {
                continue;
            }
            for (Pictogram pictogram : pictograms) {
                if (pictogram.isThing()) {
                    counts.merge(pictogram, 1, Integer::sum);
                }
            }
        }
        return counts;
    }

    /** Most used first. A stable sort, so ties keep the "most recent first" order. */
    private List<Map.Entry<Pictogram, Integer>> rank(Map<Pictogram, Integer> counts) {
        List<Map.Entry<Pictogram, Integer>> ranked = new ArrayList<>(counts.entrySet());
        ranked.sort((a, b) -> b.getValue() - a.getValue());
        return ranked;
    }

    private void addSuggestion(Pictogram thing,
                               int timesUsed,
                               List<Pictogram> alreadyLinked,
                               List<Pictogram> added,
                               List<SuggestThingsOutputData.Suggestion> suggestions) {
        if (suggestions.size() >= MAX_SUGGESTIONS
                || !thing.isThing()
                || alreadyLinked.contains(thing)
                || added.contains(thing)) {
            return;
        }
        added.add(thing);
        suggestions.add(new SuggestThingsOutputData.Suggestion(
                thing.getId(), thing.getLabel(), thing.getImageLocation(), timesUsed));
    }
}
