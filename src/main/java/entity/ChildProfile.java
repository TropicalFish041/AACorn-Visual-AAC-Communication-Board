package entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * All saved data for the child using this app:
 * their customized communication board and their communication history.
 */
public final class ChildProfile {

    private final CommunicationBoard board;
    private final List<CommunicationMessage> history;

    /**
     * Creates a new profile with an empty board and no history.
     */
    public ChildProfile() {
        this(new CommunicationBoard(), new ArrayList<>());
    }

    /**
     * Creates a profile from existing data, e.g. when loading from storage.
     */
    public ChildProfile(CommunicationBoard board, List<CommunicationMessage> history) {
        this.board = Objects.requireNonNull(board, "board cannot be null");
        this.history = new ArrayList<>(
                Objects.requireNonNull(history, "history cannot be null"));
    }

    /**
     * Adds a completed message to the end of the history.
     */
    public void addMessage(CommunicationMessage message) {
        history.add(Objects.requireNonNull(message, "message cannot be null"));
    }

    /**
     * Returns the history in the order messages were sent (oldest first).
     */
    public List<CommunicationMessage> getHistory() {
        return Collections.unmodifiableList(new ArrayList<>(history));
    }

    public boolean hasHistory() {
        return !history.isEmpty();
    }

    public CommunicationBoard getBoard() {
        return board;
    }
}
