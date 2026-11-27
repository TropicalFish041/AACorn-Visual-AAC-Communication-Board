package use_case.caregiver_pin;

/**
 * Output boundary for caregiver PIN management.
 */
public interface PinOutputBoundary {

    void showForm(boolean configured, String message);

    void prepareSuccessView();
}
