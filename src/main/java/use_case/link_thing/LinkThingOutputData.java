package use_case.link_thing;

/**
 * What was linked, so the screen can confirm it.
 */
public class LinkThingOutputData {

    private final String actionLabel;
    private final String thingLabel;

    public LinkThingOutputData(String actionLabel, String thingLabel) {
        this.actionLabel = actionLabel;
        this.thingLabel = thingLabel;
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public String getThingLabel() {
        return thingLabel;
    }
}