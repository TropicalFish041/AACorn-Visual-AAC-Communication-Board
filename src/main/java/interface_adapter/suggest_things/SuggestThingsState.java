package interface_adapter.suggest_things;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What the suggestions screen currently shows: a title and a list of suggested things,
 * or an error message (e.g. when there are no suggestions).
 */
public class SuggestThingsState {

    private String title = "";
    private List<SuggestionItem> suggestions = new ArrayList<>();
    private String errorMessage;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<SuggestionItem> getSuggestions() {
        return Collections.unmodifiableList(suggestions);
    }

    public void setSuggestions(List<SuggestionItem> suggestions) {
        this.suggestions = new ArrayList<>(suggestions);
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * One suggested thing, ready to be shown as a card.
     */
    public static final class SuggestionItem {

        private final int id;
        private final String label;
        private final String imageLocation;
        private final String hint;

        public SuggestionItem(int id, String label, String imageLocation, String hint) {
            this.id = id;
            this.label = label;
            this.imageLocation = imageLocation;
            this.hint = hint;
        }

        /** Needed later, when the caregiver adds this thing to the board. */
        public int getId() {
            return id;
        }

        public String getLabel() {
            return label;
        }

        public String getImageLocation() {
            return imageLocation;
        }

        /** A small note under the card, e.g. "Used 3 times", or "" for none. */
        public String getHint() {
            return hint;
        }
    }
}