package use_case.customize_board;

import entity.CommunicationBoard;

public interface CustomizeBoardDataAccessInterface {

    /** Returns the child's current board. */
    CommunicationBoard getBoard();

    /** Saves the updated board. */
    void saveBoard(CommunicationBoard board);
}
