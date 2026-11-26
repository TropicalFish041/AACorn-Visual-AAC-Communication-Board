package interface_adapter.customize_board;

import use_case.customize_board.CustomizeBoardInputBoundary;
import use_case.customize_board.PictogramData;

/** Translates caregiver actions into calls to the use case. */
public final class CustomizeBoardController {
    private final CustomizeBoardInputBoundary interactor;

    public CustomizeBoardController(CustomizeBoardInputBoundary interactor) {
        this.interactor = interactor;
    }

    public void open() {
        interactor.loadBoard();
    }

    public void search(String keyword) {
        interactor.search(keyword);
    }

    public void addToBoard(PictogramData tile, boolean action) {
        interactor.addToBoard(tile, action);
    }

    public void removeFromBoard(int id) {
        interactor.removeFromBoard(id);
    }

    public void linkThing(int actionId, PictogramData tile) {
        interactor.linkThing(actionId, tile);
    }

    public void unlinkThing(int actionId, int thingId) {
        interactor.unlinkThing(actionId, thingId);
    }
}
