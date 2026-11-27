package interface_adapter.caregiver_pin;

import use_case.caregiver_pin.PinOutputBoundary;

/**
 * Publishes caregiver PIN results to the view model.
 */
public final class PinPresenter implements PinOutputBoundary {

    private final PinViewModel viewModel;

    public PinPresenter(PinViewModel viewModel) {
        this.viewModel = viewModel;
    }

    @Override
    public void showForm(boolean configured, String message) {
        publish(new PinViewModel.State(
                configured, false, message));
    }

    @Override
    public void prepareSuccessView() {
        publish(new PinViewModel.State(
                true, true, " "));
    }

    private void publish(PinViewModel.State state) {
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}
