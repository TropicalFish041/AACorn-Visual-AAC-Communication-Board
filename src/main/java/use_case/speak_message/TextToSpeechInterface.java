package use_case.speak_message;

public interface TextToSpeechInterface {

    /**
     * Speaks the text and returns once it has been spoken.
     *
     * @throws SpeechException if the text could not be spoken
     */
    void speak(String text) throws SpeechException;
}