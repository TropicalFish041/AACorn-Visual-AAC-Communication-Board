package interface_adapter.link_thing;

import use_case.link_thing.LinkThingInputBoundary;
import use_case.link_thing.LinkThingInputData;

/**
 * Called by a screen when the caregiver picks a thing for an action.
 */
public class LinkThingController {

    private final LinkThingInputBoundary interactor;

    public LinkThingController(LinkThingInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void execute(int actionId, int thingId, String thingLabel, String thingImageLocation) {
        interactor.execute(
                new LinkThingInputData(actionId, thingId, thingLabel, thingImageLocation));
    }
}