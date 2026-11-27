package use_case.speak_message;

import entity.CommunicationMessage;

public interface SpeakMessageDataAccessInterface {

    /** Saves a completed message to the child's history. */
    void saveMessage(CommunicationMessage message);
}