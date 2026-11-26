package interface_adapter.suggest_things;

import use_case.suggest_things.SuggestThingsOutputBoundary;
import use_case.suggest_things.SuggestThingsOutputData;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the suggestions into text for the screen,
 * e.g. the title "Suggestions for play" and the hint "Used 3 times".
 */
public class SuggestThingsPresenter implements SuggestThingsOutputBoundary {

    private final SuggestThingsViewModel viewModel;

    public SuggestThingsPresenter(SuggestThingsViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(SuggestThingsOutputData outputData) {
        List<SuggestThingsState.SuggestionItem> items = new ArrayList<>();
        for (SuggestThingsOutputData.Suggestion suggestion : outputData.getSuggestions()) {
            items.add(new SuggestThingsState.SuggestionItem(
                    suggestion.getId(),
                    suggestion.getLabel(),
                    suggestion.getImageLocation(),
                    usageHint(suggestion.getTimesUsed())));
        }

        SuggestThingsState state = viewModel.getState();
        state.setTitle("Suggestions for " + outputData.getActionLabel());
        state.setSuggestions(items);
        state.setErrorMessage(null);
        viewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        SuggestThingsState state = viewModel.getState();
        state.setTitle("");
        state.setSuggestions(new ArrayList<>());
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChange();
    }

    /** "Used once", "Used 3 times", or "" for starter suggestions. */
    static String usageHint(int timesUsed) {
        if (timesUsed <= 0) {
            return "";
        }
        if (timesUsed == 1) {
            return "Used once";
        }
        return "Used " + timesUsed + " times";
    }
}
