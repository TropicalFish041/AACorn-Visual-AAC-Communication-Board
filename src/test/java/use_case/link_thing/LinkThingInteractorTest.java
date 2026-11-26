package use_case.link_thing;

import entity.CommunicationBoard;
import entity.LinkedVocabulary;
import entity.Pictogram;
import entity.WordType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LinkThingInteractorTest {

    private final Pictogram play = new Pictogram(1, "play", "play.png", WordType.ACTION);
    private final Pictogram water = new Pictogram(2, "water", "water.png", WordType.THING);

    private CommunicationBoard board;
    private LinkedVocabulary links;
    private int saveCount;
    private RecordingPresenter presenter;
    private LinkThingInteractor interactor;

    @BeforeEach
    void setUp() {
        board = new CommunicationBoard();
        board.addPictogram(play);
        board.addPictogram(water);
        links = new LinkedVocabulary();
        saveCount = 0;
        presenter = new RecordingPresenter();

        LinkThingDataAccessInterface dataAccess = new LinkThingDataAccessInterface() {
            @Override
            public CommunicationBoard getBoard() {
                return board;
            }

            @Override
            public LinkedVocabulary getLinks() {
                return links;
            }

            @Override
            public void saveLinks(LinkedVocabulary savedLinks) {
                saveCount++;
            }
        };
        interactor = new LinkThingInteractor(dataAccess, presenter);
    }

    @Test
    void linksThingAndSaves() {
        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertEquals(List.of("ball"), labels(links.getThings(play)));
        assertTrue(links.getThings(play).get(0).isThing());
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
    void failsUnderAThing() {
        interactor.execute(new LinkThingInputData(water.getId(), 10, "ball", "ball.png"));

        assertEquals(LinkThingInteractor.NOT_AN_ACTION_MESSAGE, presenter.error);
        assertEquals(0, saveCount);
    }

    @Test
    void failsWhenThingIsAlreadyLinked() {
        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertEquals("ball is already under play.", presenter.error);
        assertEquals(1, links.getThings(play).size());
        assertEquals(1, saveCount);
    }

    @Test
    void failsWhenActionIsFull() {
        for (int i = 0; i < LinkedVocabulary.MAX_THINGS_PER_ACTION; i++) {
            links.link(play, new Pictogram(100 + i, "thing" + i, "t.png", WordType.THING));
        }

        interactor.execute(new LinkThingInputData(1, 10, "ball", "ball.png"));

        assertTrue(presenter.error.startsWith("play already has"));
        assertEquals(LinkedVocabulary.MAX_THINGS_PER_ACTION, links.getThings(play).size());
        assertEquals(0, saveCount);
    }

    @Test
    void failsWhenThingHasNoName() {
        interactor.execute(new LinkThingInputData(1, 10, " ", "ball.png"));

        assertNull(presenter.output);
        assertTrue(links.getThings(play).isEmpty());
    }

    private static List<String> labels(List<Pictogram> pictograms) {
        List<String> labels = new ArrayList<>();
        for (Pictogram pictogram : pictograms) {
            labels.add(pictogram.getLabel());
        }
        return labels;
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
