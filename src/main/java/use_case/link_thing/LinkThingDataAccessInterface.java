package use_case.link_thing;

import entity.CommunicationBoard;

/**
 * What the link-thing use case needs: read the board and save it after a change.
 */
public interface LinkThingDataAccessInterface {

    CommunicationBoard getBoard();

    void saveBoard(CommunicationBoard board);
}