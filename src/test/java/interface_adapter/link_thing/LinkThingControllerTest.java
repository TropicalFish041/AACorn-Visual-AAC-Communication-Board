package interface_adapter.link_thing;

import org.junit.jupiter.api.Test;
import use_case.link_thing.LinkThingInputData;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinkThingControllerTest {

    @Test
    void passesThingToInteractor() {
        LinkThingInputData[] received = new LinkThingInputData[1];
        LinkThingController controller =
                new LinkThingController(inputData -> received[0] = inputData);

        controller.execute(1, 10, "ball", "ball.png");

        assertEquals(1, received[0].getActionId());
        assertEquals(10, received[0].getThingId());
        assertEquals("ball", received[0].getThingLabel());
        assertEquals("ball.png", received[0].getThingImageLocation());
    }
}