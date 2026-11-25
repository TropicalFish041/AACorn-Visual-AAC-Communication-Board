package interface_adapter.link_thing;

import org.junit.jupiter.api.Test;
import use_case.link_thing.LinkThingOutputData;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class LinkThingPresenterTest {

    @Test
    void successShowsConfirmation() {
        LinkThingViewModel viewModel = new LinkThingViewModel();
        new LinkThingPresenter(viewModel)
                .prepareSuccessView(new LinkThingOutputData("play", "ball"));

        assertEquals("Added ball to play.", viewModel.getState().getMessage());
        assertNull(viewModel.getState().getErrorMessage());
    }

    @Test
    void failShowsError() {
        LinkThingViewModel viewModel = new LinkThingViewModel();
        new LinkThingPresenter(viewModel).prepareFailView("Something went wrong.");

        assertEquals("Something went wrong.", viewModel.getState().getErrorMessage());
        assertNull(viewModel.getState().getMessage());
    }
}