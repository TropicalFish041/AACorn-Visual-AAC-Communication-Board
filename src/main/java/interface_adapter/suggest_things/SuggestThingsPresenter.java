package interface_adapter.suggest_things;

import use_case.suggest_things.SuggestThingsOutputBoundary;
import use_case.suggest_things.SuggestThingsOutputData;

import java.util.ArrayList;
import java.util.List;

/**
 * Turns the suggestions into text for the screen,
 * e.g. the title "Suggestions for play" and the hint "Used before".
 */
public class SuggestThingsPresenter implements SuggestThingsOutputBoundary {

    static final String USED_BEFORE_HINT = "Used before";

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
                    suggestion.isFromHistory() ? USED_BEFORE_HINT : ""));
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
}