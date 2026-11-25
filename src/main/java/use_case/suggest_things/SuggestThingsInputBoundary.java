package use_case.suggest_things;

/**
 * Called by the controller when the caregiver asks for things related to an action.
 */
public interface SuggestThingsInputBoundary {

    void execute(SuggestThingsInputData inputData);
}
