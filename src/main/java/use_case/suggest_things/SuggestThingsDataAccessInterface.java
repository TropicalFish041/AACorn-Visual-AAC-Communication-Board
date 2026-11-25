package use_case.suggest_things;

import entity.CommunicationBoard;
import entity.CommunicationMessage;

import java.util.List;

/**
 * What the suggest-things use case needs from the child's saved profile.
 */
public interface SuggestThingsDataAccessInterface {

    CommunicationBoard getBoard();

    List<CommunicationMessage> getHistory();
}