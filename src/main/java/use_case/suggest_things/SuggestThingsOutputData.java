package use_case.suggest_things;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The suggested things for one action, in the order they should be shown.
 * Only plain data (no entities) leaves the use case.
 */
public class SuggestThingsOutputData {

    private final String actionLabel;
    private final List<Suggestion> suggestions;

    public SuggestThingsOutputData(String actionLabel, List<Suggestion> suggestions) {
        this.actionLabel = actionLabel;
        this.suggestions = Collections.unmodifiableList(new ArrayList<>(suggestions));
    }

    public String getActionLabel() {
        return actionLabel;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

    /**
     * One suggested thing.
     */
    public static class Suggestion {

        private final int id;
        private final String label;
        private final String imageLocation;
        private final int timesUsed;

        public Suggestion(int id, String label, String imageLocation, int timesUsed) {
            this.id = id;
            this.label = label;
            this.imageLocation = imageLocation;
            this.timesUsed = timesUsed;
        }

        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getImageLocation() {
            return imageLocation;
        }

        /**
         * How many past messages used this thing together with the action.
         * 0 means it comes from the starter list.
         */
        public int getTimesUsed() {
            return timesUsed;
        }
    }
}
