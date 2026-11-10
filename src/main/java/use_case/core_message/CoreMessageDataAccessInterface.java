package use_case.core_message;

import entity.CommunicationMessage;

public interface CoreMessageDataAccessInterface {

    /** Saves a completed Help / Stop / Break message to the child's history. */
    void saveMessage(CommunicationMessage message);
}