package interface_adapter.suggest_things;

import interface_adapter.ViewModel;

/**
 * The view model for the screen that shows suggested things for an action.
 */
public class SuggestThingsViewModel extends ViewModel<SuggestThingsState> {

    public static final String VIEW_NAME = "suggest things";

    public SuggestThingsViewModel() {
        super(VIEW_NAME);
        setState(new SuggestThingsState());
    }
}