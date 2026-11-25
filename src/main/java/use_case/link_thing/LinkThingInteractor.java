package use_case.link_thing;

import entity.CommunicationBoard;
import entity.Pictogram;
import entity.WordType;

import java.util.List;
import java.util.Objects;

/**
 * Links a thing to an action on the board (e.g. "ball" under "play") and saves the board.
 * Used when the caregiver picks a suggestion, and can also be used after a search.
 */
public class LinkThingInteractor implements LinkThingInputBoundary {

    static final String ACTION_NOT_FOUND_MESSAGE = "This action is not on the board.";

    private final LinkThingDataAccessInterface dataAccess;
    private final LinkThingOutputBoundary presenter;

    public LinkThingInteractor(LinkThingDataAccessInterface dataAccess,
                               LinkThingOutputBoundary presenter) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "presenter cannot be null");
    }

    @Override
    public void execute(LinkThingInputData inputData) {
        CommunicationBoard board = dataAccess.getBoard();

        Pictogram action = findAction(board, inputData.getActionId());
        if (action == null) {
            presenter.prepareFailView(ACTION_NOT_FOUND_MESSAGE);
            return;
        }

        Pictogram thing;
        try {
            thing = new Pictogram(inputData.getThingId(), inputData.getThingLabel(),
                    inputData.getThingImageLocation(), WordType.THING);
        } catch (IllegalArgumentException e) {
            presenter.prepareFailView("This picture is missing its name or image.");
            return;
        }

        // Check the rules first so the caregiver gets a friendly message
        // instead of an exception from the board.
        List<Pictogram> things = board.getThings(action);
        if (things.contains(thing)) {
            presenter.prepareFailView(
                    thing.getLabel() + " is already under " + action.getLabel() + ".");
            return;
        }
        if (things.size() >= CommunicationBoard.MAX_THINGS_PER_ACTION) {
            presenter.prepareFailView(action.getLabel() + " already has "
                    + CommunicationBoard.MAX_THINGS_PER_ACTION + " things. Remove one first.");
            return;
        }

        board.addThing(action, thing);
        dataAccess.saveBoard(board);
        presenter.prepareSuccessView(
                new LinkThingOutputData(action.getLabel(), thing.getLabel()));
    }

    /** Returns the action on the board with this id, or null if there is none. */
    private Pictogram findAction(CommunicationBoard board, int actionId) {
        for (Pictogram action : board.getActions()) {
            if (action.getId() == actionId) {
                return action;
            }
        }
        return null;
    }
}