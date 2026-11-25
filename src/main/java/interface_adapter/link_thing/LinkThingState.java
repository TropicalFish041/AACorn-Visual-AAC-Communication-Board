package interface_adapter.link_thing;

/**
 * The result of the last link: a confirmation message or an error message.
 */
public class LinkThingState {

    private String message;
    private String errorMessage;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
