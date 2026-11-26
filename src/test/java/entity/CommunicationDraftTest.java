package entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationDraftTest {

    private Pictogram water;
    private Pictogram apple;
    private CommunicationDraft draft;

    @BeforeEach
    void setUp() {
        water = new Pictogram(1, "water", "water.png");
        apple = new Pictogram(2, "apple", "apple.png");
        draft = new CommunicationDraft("I want");
    }

    @Test
    void newDraftStartsEmpty() {
        assertTrue(draft.isEmpty());
        assertEquals(0, draft.size());
        assertEquals("I want", draft.previewText());
    }

    @Test
    void addPictogramAddsToSelection() {
        draft.addPictogram(water);

        assertEquals(1, draft.size());
        assertEquals(List.of(water), draft.getSelectedPictograms());
        assertEquals("I want water.", draft.previewText());
    }

    @Test
    void samePictogramCanBeAddedTwice() {
        draft.addPictogram(water);
        draft.addPictogram(water);

        assertEquals(2, draft.size());
        assertEquals(List.of(water, water), draft.getSelectedPictograms());
        assertEquals("I want water water.", draft.previewText());
    }

    @Test
    void removeLastPictogramRemovesMostRecentOne() {
        draft.addPictogram(water);
        draft.addPictogram(apple);

        Pictogram removed = draft.removeLastPictogram();

        assertEquals(apple, removed);
        assertEquals(List.of(water), draft.getSelectedPictograms());
        assertEquals("I want water.", draft.previewText());
    }

    @Test
    void removePictogramAtRemovesSpecificIndex() {
        draft.addPictogram(water);
        draft.addPictogram(apple);
        draft.addPictogram(water);

        Pictogram removed = draft.removePictogramAt(1);

        assertEquals(apple, removed);
        assertEquals(List.of(water, water), draft.getSelectedPictograms());
    }

    @Test
    void clearRemovesEverything() {
        draft.addPictogram(water);
        draft.addPictogram(apple);

        draft.clear();

        assertTrue(draft.isEmpty());
        assertEquals("I want", draft.previewText());
    }

    @Test
    void buildMessageCreatesFinalMessage() {
        LocalDateTime time = LocalDateTime.of(2026, 10, 23, 21, 0);

        draft.addPictogram(water);

        CommunicationMessage message = draft.buildMessage(time);

        assertEquals("I want water.", message.getText());
        assertEquals(List.of(water), message.getSelectedPictograms());
        assertEquals(time, message.getTimestamp());
    }

    @Test
    void cannotBuildEmptyMessage() {
        assertThrows(IllegalStateException.class,
                () -> draft.buildMessage(LocalDateTime.now()));
    }

    @Test
    void completedMessageDoesNotChangeAfterDraftChanges() {
        draft.addPictogram(water);
        CommunicationMessage message =
                draft.buildMessage(LocalDateTime.now());

        draft.addPictogram(apple);

        assertEquals("I want water.", message.getText());
        assertEquals(List.of(water), message.getSelectedPictograms());
    }

    @Test
    void firstActionAddsTo() {
        Pictogram sleep = new Pictogram(3, "sleep", "sleep.png", WordType.ACTION);
        draft.addPictogram(sleep);

        assertEquals("I want to sleep.", draft.previewText());
    }

    @Test
    void actionFollowedByThingUsesSameTextInCompletedMessage() {
        Pictogram play = new Pictogram(4, "play", "play.png", WordType.ACTION);
        Pictogram ball = new Pictogram(5, "ball", "ball.png", WordType.THING);
        draft.addPictogram(play);
        draft.addPictogram(ball);

        assertEquals("I want to play ball.", draft.previewText());
        CommunicationMessage message = draft.buildMessage(LocalDateTime.now());
        assertEquals(draft.previewText(), message.getText());
        assertEquals(List.of(play, ball), message.getSelectedPictograms());

        draft.removeLastPictogram();
        assertEquals("I want to play.", draft.previewText());
        assertEquals("I want to play ball.", message.getText());
    }

    @Test
    void thingFirstDoesNotAddToBeforeLaterAction() {
        Pictogram sleep = new Pictogram(3, "sleep", "sleep.png", WordType.ACTION);
        draft.addPictogram(water);
        draft.addPictogram(sleep);

        assertEquals("I want water sleep.", draft.previewText());

        draft.removePictogramAt(0);
        assertEquals("I want to sleep.", draft.previewText());
        draft.clear();
        assertEquals("I want", draft.previewText());
    }

    @Test
    void repeatingAnActionDoesNotInsertAnotherTo() {
        Pictogram sleep = new Pictogram(3, "sleep", "sleep.png", WordType.ACTION);
        draft.addPictogram(sleep);
        draft.repeatPictogramAt(0);

        assertEquals("I want to sleep sleep.", draft.previewText());
        assertEquals(List.of(sleep, sleep), draft.getSelectedPictograms());
    }

    @Test
    void otherSentenceStartersKeepExistingFormatting() {
        CommunicationDraft otherDraft = new CommunicationDraft("I can");
        otherDraft.addPictogram(new Pictogram(3, "sleep", "sleep.png", WordType.ACTION));

        assertEquals("I can sleep.", otherDraft.previewText());
    }
}
