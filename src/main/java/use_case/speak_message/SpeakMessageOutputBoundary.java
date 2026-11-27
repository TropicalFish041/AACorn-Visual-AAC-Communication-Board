package use_case.speak_message;

/**
 * Output boundary for UC3 - Speak a Message.
 */
public interface SpeakMessageOutputBoundary {

    void prepareSuccessView(SpeakMessageOutputData outputData);

    void prepareFailView(String errorMessage);
}