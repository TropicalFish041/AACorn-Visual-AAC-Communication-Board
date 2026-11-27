package interface_adapter.suggest_things;

import use_case.suggest_things.SuggestThingsInputBoundary;
import use_case.suggest_things.SuggestThingsInputData;

/**
 * Called by the customize screen when the caregiver asks for suggestions for an action.
 */
public class SuggestThingsController {

    private final SuggestThingsInputBoundary interactor;

    public SuggestThingsController(SuggestThingsInputBoundary interactor) {
        this.interactor = interactor;
    }

    /**
     * Loads suggestions for the action with this id.
     */
    public void execute(int actionId) {
        interactor.execute(new SuggestThingsInputData(actionId));
    }
}