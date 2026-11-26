package use_case.customize_board;

public interface CustomizeBoardInputBoundary {
    void loadBoard();

    void search(String keyword);

    void addToBoard(PictogramData result, boolean action);

    void removeFromBoard(int pictogramId);

    void linkThing(int actionId, PictogramData result);

    void unlinkThing(int actionId, int thingId);
}
