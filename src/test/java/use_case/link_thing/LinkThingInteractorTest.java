package use_case.link_thing;

import entity.CommunicationBoard;
import entity.Pictogram;
import entity.WordType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkThingInteractorTest {

    private final Pictogram play = new Pictogram(1, "play", "play.png", WordType.ACTION);

    private CommunicationBoard board;
    private int saveCount;
    private RecordingPresenter presenter;
    private LinkThingInteractor interactor;

    @BeforeEach
    void setUp() {
        board = new CommunicationBoard();
        board.addAction(play);
        saveCount = 0;
        presenter = new RecordingPresenter();

        LinkThingDataAccessInterface dataAccess = new LinkThingDataAccessInterface() {
            @Override
            public CommunicationBoard getBoard() {
                return board;
            }

            @Override
            public void saveBoard(CommunicationBoard savedBoard) {
                saveCount++;
            }
        };
        interactor = new LinkThingInteractor(dataAccess, presenter);
    }

    @Test
    void linksThingAndSavesBoard() {
        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertEquals(List.of("ball"), labels(board.getThings(play)));
        assertTrue(board.getThings(play).get(0).isThing());
        assertEquals(1, saveCount);
        assertEquals("play", presenter.output.getActionLabel());
        assertEquals("ball", presenter.output.getThingLabel());
    }

    @Test
    void failsWhenActionIsNotOnBoard() {
        interactor.execute(new LinkThingInputData(99, 10, "ball", "ball.png"));

        assertEquals(LinkThingInteractor.ACTION_NOT_FOUND_MESSAGE, presenter.error);
        assertEquals(0, saveCount);
    }

    @Test
    void failsWhenThingIsAlreadyLinked() {
        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertEquals("ball is already under play.", presenter.error);
        assertEquals(1, board.getThings(play).size());
        assertEquals(1, saveCount);
    }

    @Test
    void failsWhenActionIsFull() {
        for (int i = 0; i < CommunicationBoard.MAX_THINGS_PER_ACTION; i++) {
            board.addThing(play, new Pictogram(100 + i, "thing" + i, "t.png", WordType.THING));
        }

        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertTrue(presenter.error.startsWith("play already has"));
        assertFalse(board.getThings(play).contains(
                new Pictogram(10, "ball", "ball.png", WordType.THING)));
        assertEquals(0, saveCount);
    }

    @Test
    void failsWhenThingHasNoName() {
        interactor.execute(new LinkThingInputData(1, 10, " ", "ball.png"));

        assertNull(presenter.output);
        assertTrue(board.getThings(play).isEmpty());
    }

    private static List<String> labels(List<Pictogram> pictograms) {
        return pictograms.stream().map(Pictogram::getLabel).collect(
                java.util.stream.Collectors.toList());
    }

    private static class RecordingPresenter implements LinkThingOutputBoundary {
        private LinkThingOutputData output;
        private String error;

        @Override
        public void prepareSuccessView(LinkThingOutputData outputData) {
            output = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}