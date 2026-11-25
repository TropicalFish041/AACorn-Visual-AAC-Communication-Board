package use_case.suggest_things;

/**
 * The action the caregiver wants suggestions for.
 */
public class SuggestThingsInputData {

    private final int actionId;

    public SuggestThingsInputData(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }
}