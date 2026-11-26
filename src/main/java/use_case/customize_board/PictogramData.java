package use_case.customize_board;

/** Pictogram information exchanged across the Customize Board boundaries. */
public final class PictogramData {
    private final int id;
    private final String label;
    private final String imageLocation;
    private final boolean action;

    public PictogramData(int id, String label, String imageLocation, boolean action) {
        this.id = id;
        this.label = label;
        this.imageLocation = imageLocation;
        this.action = action;
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

    public boolean isAction() {
        return action;
    }
}
