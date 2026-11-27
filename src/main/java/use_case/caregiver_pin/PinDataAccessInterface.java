package use_case.caregiver_pin;

/**
 * Data access required by UC5.
 */
public interface PinDataAccessInterface {

    /**
     * Returns the saved PIN, or null if no PIN has been set.
     */
    String getPin();

    void savePin(String pin);
}
