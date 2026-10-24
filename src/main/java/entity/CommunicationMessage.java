package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * An immutable completed message sent by the child.
 */
public final class CommunicationMessage {

    private final List<Pictogram> selectedPictograms;
    private final String text;
    private final LocalDateTime timestamp;

    public CommunicationMessage(List<Pictogram> selectedPictograms,
                                String text,
                                LocalDateTime timestamp) {
        Objects.requireNonNull(selectedPictograms, "selectedPictograms cannot be null");
        Objects.requireNonNull(timestamp, "timestamp cannot be null");

        if (selectedPictograms.isEmpty()) {
            throw new IllegalArgumentException("message must contain at least one pictogram");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("text cannot be blank");
        }

        this.selectedPictograms =
                Collections.unmodifiableList(new ArrayList<>(selectedPictograms));
        this.text = text;
        this.timestamp = timestamp;
    }

    public List<Pictogram> getSelectedPictograms() {
        return selectedPictograms;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}