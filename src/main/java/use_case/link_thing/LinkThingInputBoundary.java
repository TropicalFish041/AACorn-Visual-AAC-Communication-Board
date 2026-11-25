package use_case.link_thing;

/**
 * Called by the controller when the caregiver links a thing to an action.
 */
public interface LinkThingInputBoundary {

    void execute(LinkThingInputData inputData);
}
