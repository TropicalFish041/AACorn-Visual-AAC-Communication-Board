package entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommunicationMessageTest {

    @Test
    void messageMakesDefensiveCopy() {
        Pictogram water = new Pictogram(1, "water", "water.png");
        List<Pictogram> original = new ArrayList<>();
        original.add(water);

        CommunicationMessage message =
                new CommunicationMessage(original, "I want water.", LocalDateTime.now());

        original.clear();

        assertEquals(List.of(water), message.getSelectedPictograms());
    }

    @Test
    void messageCannotBeEmpty() {
        assertThrows(IllegalArgumentException.class,
                () -> new CommunicationMessage(List.of(), "I want.", LocalDateTime.now()));
    }

    @Test
    void returnedPictogramListCannotBeModified() {
        Pictogram water = new Pictogram(1, "water", "water.png");

        CommunicationMessage message =
                new CommunicationMessage(List.of(water), "I want water.", LocalDateTime.now());

        assertThrows(UnsupportedOperationException.class,
                () -> message.getSelectedPictograms().clear());
    }
}