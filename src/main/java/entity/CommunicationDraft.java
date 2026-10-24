package entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The temporary message currently being built by the child.
 */
public final class CommunicationDraft {

    private final String sentenceStarter;
    private final List<Pictogram> selectedPictograms;

    public CommunicationDraft(String sentenceStarter) {
        if (sentenceStarter == null || sentenceStarter.isBlank()) {
            throw new IllegalArgumentException("sentenceStarter cannot be blank");
        }

        this.sentenceStarter = sentenceStarter.trim();
        this.selectedPictograms = new ArrayList<>();
    }

    /**
     * Adds a pictogram to the end of the current draft.
     * Repeated pictograms are allowed.
     */
    public void addPictogram(Pictogram pictogram) {
        selectedPictograms.add(
                Objects.requireNonNull(pictogram, "pictogram cannot be null")
        );
    }

    /**
     * Removes the last selected pictogram.
     * Useful for a simple Delete button.
     */
    public Pictogram removeLastPictogram() {
        if (selectedPictograms.isEmpty()) {
            throw new IllegalStateException("cannot remove from an empty draft");
        }
        return selectedPictograms.remove(selectedPictograms.size() - 1);
    }

    /**
     * Removes a pictogram at a specific position.
     * Useful if the UI later allows removing a particular selected pictogram.
     */
    public Pictogram removePictogramAt(int index) {
        return selectedPictograms.remove(index);
    }

    /**
     * Repeats one already-selected pictogram.
     */
    public void repeatPictogramAt(int index) {
        Pictogram pictogram = selectedPictograms.get(index);
        selectedPictograms.add(pictogram);
    }

    /**
     * Clears the whole current draft.
     */
    public void clear() {
        selectedPictograms.clear();
    }

    public boolean isEmpty() {
        return selectedPictograms.isEmpty();
    }

    public int size() {
        return selectedPictograms.size();
    }

    public List<Pictogram> getSelectedPictograms() {
        return Collections.unmodifiableList(new ArrayList<>(selectedPictograms));
    }

    /**
     * Returns the text currently represented by the draft.
     * Example: "I want water apple."
     */
    public String previewText() {
        if (selectedPictograms.isEmpty()) {
            return sentenceStarter;
        }

        String labels = selectedPictograms.stream()
                .map(Pictogram::getLabel)
                .collect(Collectors.joining(" "));

        return sentenceStarter + " " + labels + ".";
    }

    /**
     * Builds the final immutable CommunicationMessage.
     */
    public CommunicationMessage buildMessage(LocalDateTime timestamp) {
        if (selectedPictograms.isEmpty()) {
            throw new IllegalStateException("cannot build an empty message");
        }

        return new CommunicationMessage(
                selectedPictograms,
                previewText(),
                timestamp
        );
    }
}