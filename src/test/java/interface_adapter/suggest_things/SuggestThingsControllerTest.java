package interface_adapter.suggest_things;

import org.junit.jupiter.api.Test;
import use_case.suggest_things.SuggestThingsInputData;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SuggestThingsControllerTest {

    @Test
    void passesActionIdToInteractor() {
        SuggestThingsInputData[] received = new SuggestThingsInputData[1];
        SuggestThingsController controller =
                new SuggestThingsController(inputData -> received[0] = inputData);

        controller.execute(42);

        assertEquals(42, received[0].getActionId());
    }
}