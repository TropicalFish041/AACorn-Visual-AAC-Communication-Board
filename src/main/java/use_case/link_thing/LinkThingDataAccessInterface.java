package use_case.link_thing;

import entity.CommunicationBoard;
import entity.LinkedVocabulary;

/**
 * What the link-thing use case needs: read the board and the links,
 * and save the links after a change.
 */
public interface LinkThingDataAccessInterface {

    CommunicationBoard getBoard();

    LinkedVocabulary getLinks();

    void saveLinks(LinkedVocabulary links);
}
