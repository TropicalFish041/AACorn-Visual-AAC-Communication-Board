package use_case.suggest_things;

import entity.Pictogram;

import java.util.List;

/**
 * A built-in list of common things for common actions
 * (e.g. "play" -> ball, toy, park).
 */
public interface StarterThingsDataAccessInterface {

    /**
     * Returns the starter things for this action, or an empty list if there are none.
     */
    List<Pictogram> getStarterThings(String actionLabel);
}