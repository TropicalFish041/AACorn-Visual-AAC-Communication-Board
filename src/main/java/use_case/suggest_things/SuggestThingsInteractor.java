package use_case.suggest_things;

import entity.CommunicationBoard;
import entity.CommunicationMessage;
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
 * Things already linked to the action are skipped, and the board is never changed here:
 * the caregiver decides what to add.
 */
public class SuggestThingsInteractor implements SuggestThingsInputBoundary {

    static final int MAX_SUGGESTIONS = 6;
    static final String ACTION_NOT_FOUND_MESSAGE = "This action is not on the board.";
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
        CommunicationBoard board = profileDataAccess.getBoard();

        Pictogram action = findAction(board, inputData.getActionId());
        if (action == null) {
            presenter.prepareFailView(ACTION_NOT_FOUND_MESSAGE);
            return;
        }

        List<Pictogram> alreadyLinked = board.getThings(action);
        List<SuggestThingsOutputData.Suggestion> suggestions = new ArrayList<>();
        List<Pictogram> added = new ArrayList<>();

        // 1. From history (most used first).
        for (Pictogram thing : rankThingsFromHistory(action)) {
            addSuggestion(thing, true, alreadyLinked, added, suggestions);
        }

        // 2. From the starter list.
        for (Pictogram thing : starterDataAccess.getStarterThings(action.getLabel())) {
            addSuggestion(thing, false, alreadyLinked, added, suggestions);
        }

        if (suggestions.isEmpty()) {
            presenter.prepareFailView(NO_SUGGESTIONS_MESSAGE);
            return;
        }

        presenter.prepareSuccessView(
                new SuggestThingsOutputData(action.getLabel(), suggestions));
    }

    /** Returns the action on the board with this id, or null if there is none. */
    private Pictogram findAction(CommunicationBoard board, int actionId) {
        for (Pictogram action : board.getActions()) {
            if (action.getId() == actionId) {
                return action;
            }
        }
        return null;
    }

    /**
     * Counts the things that appeared in the same message as this action.
     * Newer messages are looked at first, so on a tie the more recent thing comes first.
     */
    private List<Pictogram> rankThingsFromHistory(Pictogram action) {
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

        List<Pictogram> ranked = new ArrayList<>(counts.keySet());
        // A stable sort, so ties keep the "most recent first" order.
        ranked.sort((a, b) -> counts.get(b) - counts.get(a));
        return ranked;
    }

    private void addSuggestion(Pictogram thing,
                               boolean fromHistory,
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
                thing.getId(), thing.getLabel(), thing.getImageLocation(), fromHistory));
    }
}