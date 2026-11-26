package use_case.customize_board;

import entity.CommunicationBoard;
import entity.LinkedVocabulary;

/** Stores both parts of the child's vocabulary together. */
public interface BoardCustomizationDataAccess extends CustomizeBoardDataAccessInterface {
    LinkedVocabulary getLinks();

    void saveVocabulary(CommunicationBoard board, LinkedVocabulary links);
}
