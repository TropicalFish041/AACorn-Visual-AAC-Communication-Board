package use_case.suggest_things;

import entity.CommunicationBoard;
import entity.CommunicationMessage;
import entity.Pictogram;
import entity.WordType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuggestThingsInteractorTest {

    private final Pictogram play = new Pictogram(1, "play", "play.png", WordType.ACTION);
    private final Pictogram eat = new Pictogram(2, "eat", "eat.png", WordType.ACTION);
    private final Pictogram ball = new Pictogram(10, "ball", "ball.png", WordType.THING);
    private final Pictogram toy = new Pictogram(11, "toy", "toy.png", WordType.THING);
    private final Pictogram park = new Pictogram(12, "park", "park.png", WordType.THING);
    private final Pictogram apple = new Pictogram(13, "apple", "apple.png", WordType.THING);

    private CommunicationBoard board;
    private List<CommunicationMessage> history;
    private List<Pictogram> starterThings;
    private RecordingPresenter presenter;
    private SuggestThingsInteractor interactor;

    @BeforeEach
    void setUp() {
        board = new CommunicationBoard();
        board.addAction(play);
        history = new ArrayList<>();
        starterThings = new ArrayList<>();
        presenter = new RecordingPresenter();

        SuggestThingsDataAccessInterface profile = new SuggestThingsDataAccessInterface() {
            @Override
            public CommunicationBoard getBoard() {
                return board;
            }

            @Override
            public List<CommunicationMessage> getHistory() {
                return history;
            }
        };
        // A lambda works here because the starter interface has only one method.
        StarterThingsDataAccessInterface starter = actionLabel -> starterThings;

        interactor = new SuggestThingsInteractor(profile, starter, presenter);
    }

    @Test
    void suggestsStarterThings() {
        starterThings.addAll(List.of(ball, toy, park));

        interactor.execute(new SuggestThingsInputData(play.getId()));

        assertEquals("play", presenter.output.getActionLabel());
        assertEquals(List.of("ball", "toy", "park"), labels(presenter.output));
        assertFalse(presenter.output.getSuggestions().get(0).isFromHistory());
    }

    @Test
    void historyComesFirstMostUsedFirst() {
        history.add(message(play, park));
        history.add(message(play, toy));
        history.add(message(play, toy));
        starterThings.addAll(List.of(ball, toy));

        interactor.execute(new SuggestThingsInputData(play.getId()));

        // toy used twice, park once, then ball from the starter list (toy not repeated)
        assertEquals(List.of("toy", "park", "ball"), labels(presenter.output));
        assertTrue(presenter.output.getSuggestions().get(0).isFromHistory());
        assertFalse(presenter.output.getSuggestions().get(2).isFromHistory());
    }

    @Test
    void ignoresMessagesWithoutThisAction() {
        board.addAction(eat);
        history.add(message(eat, apple));
        starterThings.add(ball);

        interactor.execute(new SuggestThingsInputData(play.getId()));

        assertEquals(List.of("ball"), labels(presenter.output));
    }

    @Test
    void skipsThingsAlreadyLinked() {
        board.addThing(play, ball);
        starterThings.addAll(List.of(ball, toy));

        interactor.execute(new SuggestThingsInputData(play.getId()));

        assertEquals(List.of("toy"), labels(presenter.output));
    }

    @Test
    void returnsAtMostSixSuggestions() {
        for (int i = 0; i < 10; i++) {
            starterThings.add(new Pictogram(100 + i, "thing" + i, "t.png", WordType.THING));
        }

        interactor.execute(new SuggestThingsInputData(play.getId()));

        assertEquals(SuggestThingsInteractor.MAX_SUGGESTIONS,
                presenter.output.getSuggestions().size());
    }

    @Test
    void failsWhenActionIsNotOnBoard() {
        interactor.execute(new SuggestThingsInputData(eat.getId()));

        assertNull(presenter.output);
        assertEquals(SuggestThingsInteractor.ACTION_NOT_FOUND_MESSAGE, presenter.error);
    }

    @Test
    void failsWhenThereAreNoSuggestions() {
        interactor.execute(new SuggestThingsInputData(play.getId()));

        assertNull(presenter.output);
        assertEquals(SuggestThingsInteractor.NO_SUGGESTIONS_MESSAGE, presenter.error);
    }

    private static CommunicationMessage message(Pictogram... pictograms) {
        return new CommunicationMessage(List.of(pictograms), "msg", LocalDateTime.now());
    }

    private static List<String> labels(SuggestThingsOutputData output) {
        List<String> labels = new ArrayList<>();
        for (SuggestThingsOutputData.Suggestion suggestion : output.getSuggestions()) {
            labels.add(suggestion.getLabel());
        }
        return labels;
    }

    private static class RecordingPresenter implements SuggestThingsOutputBoundary {
        private SuggestThingsOutputData output;
        private String error;

        @Override
        public void prepareSuccessView(SuggestThingsOutputData outputData) {
            output = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}