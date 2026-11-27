package use_case.speak_message;

import entity.CommunicationDraft;
import entity.CommunicationMessage;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UC3 - Speak a Message.
 *
 * The message is spoken first and recorded after, so history only
 * contains messages that were actually said. If speech fails, nothing is
 * recorded and the draft is kept so the child can try again.
 */
public class SpeakMessageInteractor implements SpeakMessageInputBoundary {

    static final String NO_MESSAGE = "Choose a picture before pressing Speak.";
    static final String SPEECH_FAILED = "The message could not be spoken. Please try again.";
    static final String SAVE_FAILED = "The message was spoken but could not be saved to history.";

    private final SpeakMessageDataAccessInterface dataAccess;
    private final TextToSpeechInterface speech;
    private final SpeakMessageOutputBoundary presenter;
    private final CommunicationDraft draft;
    private final Clock clock;

    public SpeakMessageInteractor(SpeakMessageDataAccessInterface dataAccess,
                                  TextToSpeechInterface speech,
                                  SpeakMessageOutputBoundary presenter,
                                  CommunicationDraft draft) {
        this(dataAccess, speech, presenter, draft, Clock.systemDefaultZone());
    }

    SpeakMessageInteractor(SpeakMessageDataAccessInterface dataAccess,
                           TextToSpeechInterface speech,
                           SpeakMessageOutputBoundary presenter,
                           CommunicationDraft draft,
                           Clock clock) {
        this.dataAccess = Objects.requireNonNull(dataAccess, "dataAccess cannot be null");
        this.speech = Objects.requireNonNull(speech, "speech cannot be null");
        this.presenter = Objects.requireNonNull(presenter, "presenter cannot be null");
        this.draft = Objects.requireNonNull(draft, "draft cannot be null");
        this.clock = Objects.requireNonNull(clock, "clock cannot be null");
    }

    @Override
    public void speak() {
        if (draft.isEmpty()) {
            presenter.prepareFailView(NO_MESSAGE);
            return;
        }

        CommunicationMessage message = draft.buildMessage(LocalDateTime.now(clock));

        try {
            speech.speak(message.getText());
        } catch (SpeechException exception) {
            presenter.prepareFailView(SPEECH_FAILED);
            return;
        }

        // The message has been said, so the draft is finished.
        draft.clear();

        try {
            dataAccess.saveMessage(message);
        } catch (RuntimeException exception) {
            presenter.prepareFailView(SAVE_FAILED);
            return;
        }

        presenter.prepareSuccessView(new SpeakMessageOutputData(message.getText()));
    }
}
