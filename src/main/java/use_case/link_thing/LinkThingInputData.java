package use_case.link_thing;

/**
 * The thing the caregiver wants to link to an action.
 */
public class LinkThingInputData {

    private final int actionId;
    private final int thingId;
    private final String thingLabel;
    private final String thingImageLocation;

    public LinkThingInputData(int actionId, int thingId,
                              String thingLabel, String thingImageLocation) {
        this.actionId = actionId;
        this.thingId = thingId;
        this.thingLabel = thingLabel;
        this.thingImageLocation = thingImageLocation;
    }

    public int getActionId() {
        return actionId;
    }

    public int getThingId() {
        return thingId;
    }

    public String getThingLabel() {
        return thingLabel;
    }

    public String getThingImageLocation() {
        return thingImageLocation;
    }
}