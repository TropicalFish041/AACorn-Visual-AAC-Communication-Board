package use_case.communicate_request;

import entity.CommunicationBoard;
import entity.CommunicationMessage;

public interface CommunicateRequestDataAccessInterface {

    /** Returns the child's current board, so the child can pick pictograms. */
    CommunicationBoard getBoard();

    /** Saves a completed message to the child's history. */
    void saveMessage(CommunicationMessage message);
}
