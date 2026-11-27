package use_case.speak_message;

public interface SpeakMessageInputBoundary {

    /**
     * Speaks the message currently being built and records it in history.
     * Does nothing but report an error if there is no message to speak.
     */
    void speak();
}