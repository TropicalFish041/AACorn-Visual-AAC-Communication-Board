package entity;

import java.util.Objects;

/**
 * A pictogram available on a child's communication board.
 */
public final class Pictogram {

    private final int id;
    private final String label;
    private final String imageLocation;

    public Pictogram(int id, String label, String imageLocation) {
        if (label == null || label.isBlank()) {
            throw new IllegalArgumentException("label cannot be blank");
        }
        if (imageLocation == null || imageLocation.isBlank()) {
            throw new IllegalArgumentException("imageLocation cannot be blank");
        }

        this.id = id;
        this.label = label;
        this.imageLocation = imageLocation;
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

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof Pictogram)) {
            return false;
        }
        Pictogram pictogram = (Pictogram) other;
        return id == pictogram.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return label;
    }
}