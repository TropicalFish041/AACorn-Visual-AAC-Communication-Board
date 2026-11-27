package use_case.caregiver_pin;

/**
 * Input boundary for caregiver PIN management.
 */
public interface PinInputBoundary {

    void open();

    void submit(String pin);

    void reset(String currentPin, String newPin);
}
