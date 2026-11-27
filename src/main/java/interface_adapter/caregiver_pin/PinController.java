package interface_adapter.caregiver_pin;

import use_case.caregiver_pin.PinInputBoundary;

/**
 * Translates caregiver PIN actions into use-case calls.
 */
public final class PinController {

    private final PinInputBoundary interactor;

    public PinController(PinInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void open() {
        interactor.open();
    }

    public void submit(String pin) {
        interactor.submit(pin);
    }

    public void reset(String currentPin, String newPin) {
        interactor.reset(currentPin, newPin);
    }
}
