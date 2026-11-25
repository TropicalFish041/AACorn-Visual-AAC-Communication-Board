package interface_adapter.link_thing;

import interface_adapter.ViewModel;

/**
 * The view model for linking a thing to an action.
 */
public class LinkThingViewModel extends ViewModel<LinkThingState> {

    public static final String VIEW_NAME = "link thing";

    public LinkThingViewModel() {
        super(VIEW_NAME);
        setState(new LinkThingState());
    }
}