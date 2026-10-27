package use_case.view_history;

import entity.CommunicationMessage;

import java.util.List;

public interface ViewHistoryDataAccessInterface {

    /** Returns all saved messages, oldest first. */
    List<CommunicationMessage> getHistory();
}
