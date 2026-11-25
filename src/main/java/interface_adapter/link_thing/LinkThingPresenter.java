package interface_adapter.link_thing;

import use_case.link_thing.LinkThingOutputBoundary;
import use_case.link_thing.LinkThingOutputData;

/**
 * Turns the link result into a short message, e.g. "Added ball to play.".
 */
public class LinkThingPresenter implements LinkThingOutputBoundary {

    private final LinkThingViewModel viewModel;

    public LinkThingPresenter(LinkThingViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(LinkThingOutputData outputData) {
        LinkThingState state = viewModel.getState();
        state.setMessage("Added " + outputData.getThingLabel()
                + " to " + outputData.getActionLabel() + ".");
        state.setErrorMessage(null);
        viewModel.firePropertyChange();
    }

    @Override
    public void prepareFailView(String errorMessage) {
        LinkThingState state = viewModel.getState();
        state.setMessage(null);
        state.setErrorMessage(errorMessage);
        viewModel.firePropertyChange();
    }
}