package use_case.caregiver_pin;

import java.io.UncheckedIOException;

/**
 * Implements UC5: setting, verifying and resetting a caregiver PIN.
 */
public final class PinInteractor implements PinInputBoundary {

    private static final String INVALID_PIN =
            "PIN must contain exactly four digits.";

    private static final String INCORRECT_PIN =
            "Incorrect PIN.";

    private final PinDataAccessInterface dataAccess;
    private final PinOutputBoundary presenter;

    public PinInteractor(PinDataAccessInterface dataAccess,
                         PinOutputBoundary presenter) {
        this.dataAccess = dataAccess;
        this.presenter = presenter;
    }

    @Override
    public void open() {
        presenter.showForm(dataAccess.getPin() != null, " ");
    }

    @Override
    public void submit(String pin) {
        String savedPin = dataAccess.getPin();

        if (!isValid(pin)) {
            presenter.showForm(savedPin != null, INVALID_PIN);
        } else if (savedPin == null) {
            saveAndEnter(pin, false);
        } else if (savedPin.equals(pin)) {
            presenter.prepareSuccessView();
        } else {
            presenter.showForm(true, INCORRECT_PIN);
        }
    }

    @Override
    public void reset(String currentPin, String newPin) {
        String savedPin = dataAccess.getPin();

        if (savedPin == null
                || !isValid(currentPin)
                || !savedPin.equals(currentPin)) {
            presenter.showForm(savedPin != null, INCORRECT_PIN);
        } else if (!isValid(newPin)) {
            presenter.showForm(true, INVALID_PIN);
        } else {
            saveAndEnter(newPin, true);
        }
    }

    private void saveAndEnter(String pin, boolean configured) {
        try {
            dataAccess.savePin(pin);
        } catch (UncheckedIOException exception) {
            presenter.showForm(configured,
                    "Could not save PIN. Try again.");
            return;
        }

        presenter.prepareSuccessView();
    }

    private static boolean isValid(String pin) {
        return pin != null && pin.matches("[0-9]{4}");
    }
}
