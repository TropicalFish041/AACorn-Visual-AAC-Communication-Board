package use_case.speak_message;

import entity.CommunicationDraft;
import entity.CommunicationMessage;
import entity.Pictogram;
import entity.WordType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SpeakMessageInteractorTest {

    private static final Clock CLOCK =
            Clock.fixed(Instant.parse("2026-11-26T15:00:00Z"), ZoneOffset.UTC);

    private Pictogram water;
    private Pictogram play;
    private CommunicationDraft draft;
    private FakeSpeech speech;
    private FakeDataAccess dataAccess;
    private RecordingPresenter presenter;
    private SpeakMessageInteractor interactor;

    @BeforeEach
    void setUp() {
        water = new Pictogram(1, "water", "water.png");
        play = new Pictogram(2, "play", "play.png", WordType.ACTION);
        draft = new CommunicationDraft("I want");
        speech = new FakeSpeech();
        dataAccess = new FakeDataAccess();
        presenter = new RecordingPresenter();
        interactor = new SpeakMessageInteractor(dataAccess, speech, presenter, draft, CLOCK);
    }

    @Test
    void speaksTheMessageAndRecordsItWithATimestamp() {
        draft.addPictogram(water);

        interactor.speak();

        assertEquals(List.of("I want water."), speech.spoken);
        assertEquals(1, dataAccess.saved.size());
        CommunicationMessage saved = dataAccess.saved.get(0);
        assertEquals("I want water.", saved.getText());
        assertEquals(List.of(water), saved.getSelectedPictograms());
        assertEquals(LocalDateTime.of(2026, 11, 26, 15, 0), saved.getTimestamp());
        assertEquals("I want water.", presenter.success.getSpokenText());
        assertNull(presenter.error);
    }

    @Test
    void repeatedPictogramIsSpokenAndRecordedEachTime() {
        draft.addPictogram(water);
        draft.addPictogram(water);

        interactor.speak();

        assertEquals(List.of("I want water water."), speech.spoken);
        assertEquals(2, dataAccess.saved.get(0).getSelectedPictograms().size());
    }

    @Test
    void actionIsSpokenAsAnInfinitive() {
        draft.addPictogram(play);

        interactor.speak();

        assertEquals(List.of("I want to play."), speech.spoken);
    }

    @Test
    void draftIsClearedAfterSpeaking() {
        draft.addPictogram(water);

        interactor.speak();

        assertTrue(draft.isEmpty());
    }

    @Test
    void emptyDraftSpeaksAndRecordsNothing() {
        interactor.speak();

        assertTrue(speech.spoken.isEmpty());
        assertTrue(dataAccess.saved.isEmpty());
        assertEquals(SpeakMessageInteractor.NO_MESSAGE, presenter.error);
        assertNull(presenter.success);
    }

    @Test
    void speechFailureRecordsNothingAndKeepsTheDraft() {
        speech.failure = new SpeechException("no voice", null);
        draft.addPictogram(water);

        interactor.speak();

        assertTrue(dataAccess.saved.isEmpty());
        assertFalse(draft.isEmpty());
        assertEquals(SpeakMessageInteractor.SPEECH_FAILED, presenter.error);
        assertNull(presenter.success);
    }

    @Test
    void saveFailureIsReportedAfterTheMessageWasSpoken() {
        dataAccess.failure = new IllegalStateException("disk full");
        draft.addPictogram(water);

        interactor.speak();

        assertEquals(List.of("I want water."), speech.spoken);
        // It was already said, so it is not left in the draft to be spoken twice.
        assertTrue(draft.isEmpty());
        assertEquals(SpeakMessageInteractor.SAVE_FAILED, presenter.error);
        assertNull(presenter.success);
    }

    private static final class FakeSpeech implements TextToSpeechInterface {
        private final List<String> spoken = new ArrayList<>();
        private SpeechException failure;

        @Override
        public void speak(String text) throws SpeechException {
            if (failure != null) {
                throw failure;
            }
            spoken.add(text);
        }
    }

    private static final class FakeDataAccess implements SpeakMessageDataAccessInterface {
        private final List<CommunicationMessage> saved = new ArrayList<>();
        private RuntimeException failure;

        @Override
        public void saveMessage(CommunicationMessage message) {
            if (failure != null) {
                throw failure;
            }
            saved.add(message);
        }
    }

    private static final class RecordingPresenter implements SpeakMessageOutputBoundary {
        private SpeakMessageOutputData success;
        private String error;

        @Override
        public void prepareSuccessView(SpeakMessageOutputData outputData) {
            success = outputData;
        }

        @Override
        public void prepareFailView(String errorMessage) {
            error = errorMessage;
        }
    }
}