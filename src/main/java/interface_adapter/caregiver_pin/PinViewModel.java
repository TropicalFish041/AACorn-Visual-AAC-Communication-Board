package interface_adapter.caregiver_pin;

import interface_adapter.ViewModel;

/**
 * Holds the state of the caregiver PIN screen.
 */
public final class PinViewModel
        extends ViewModel<PinViewModel.State> {

    public PinViewModel() {
        super("pin");
        setState(new State(false, false, " "));
    }

    /**
     * Display state without storing the PIN itself.
     */
    public static final class State {

        public final boolean configured;
        public final boolean authenticated;
        public final String message;

        public State(boolean configured,
                     boolean authenticated,
                     String message) {
            this.configured = configured;
            this.authenticated = authenticated;
            this.message = message;
        }
    }
}
