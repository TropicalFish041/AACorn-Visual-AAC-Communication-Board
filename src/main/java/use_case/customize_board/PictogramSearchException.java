package use_case.customize_board;

/**
 * Thrown when pictograms cannot be searched, e.g. the ARASAAC API is unavailable.
 */
public class PictogramSearchException extends Exception {

    public PictogramSearchException(String message, Throwable cause) {
        super(message, cause);
    }
}
