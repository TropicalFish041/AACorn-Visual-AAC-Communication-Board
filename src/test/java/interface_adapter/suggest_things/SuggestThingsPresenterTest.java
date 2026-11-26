package interface_adapter.suggest_things;

import org.junit.jupiter.api.Test;
import use_case.suggest_things.SuggestThingsOutputData;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SuggestThingsPresenterTest {

    @Test
    void successShowsTitleAndSuggestions() {
        SuggestThingsViewModel viewModel = new SuggestThingsViewModel();
        int[] changes = {0};
        viewModel.addPropertyChangeListener(event -> changes[0]++);
        SuggestThingsPresenter presenter = new SuggestThingsPresenter(viewModel);

        presenter.prepareSuccessView(new SuggestThingsOutputData("play", List.of(
                new SuggestThingsOutputData.Suggestion(10, "toy", "toy.png", 3),
                new SuggestThingsOutputData.Suggestion(11, "ball", "ball.png", 0))));

        SuggestThingsState state = viewModel.getState();
        assertEquals("Suggestions for play", state.getTitle());
        assertEquals(2, state.getSuggestions().size());

        SuggestThingsState.SuggestionItem first = state.getSuggestions().get(0);
        assertEquals(10, first.getId());
        assertEquals("toy", first.getLabel());
        assertEquals("toy.png", first.getImageLocation());
        assertEquals("Used 3 times", first.getHint());
        assertEquals("", state.getSuggestions().get(1).getHint());

        assertNull(state.getErrorMessage());
        assertEquals(1, changes[0]);
    }

    @Test
    void usageHintWording() {
        assertEquals("", SuggestThingsPresenter.usageHint(0));
        assertEquals("Used once", SuggestThingsPresenter.usageHint(1));
        assertEquals("Used 2 times", SuggestThingsPresenter.usageHint(2));
    }

    @Test
    void failShowsErrorAndClearsOldSuggestions() {
        SuggestThingsViewModel viewModel = new SuggestThingsViewModel();
        SuggestThingsPresenter presenter = new SuggestThingsPresenter(viewModel);
        presenter.prepareSuccessView(new SuggestThingsOutputData("play", List.of(
                new SuggestThingsOutputData.Suggestion(10, "toy", "toy.png", 0))));

        presenter.prepareFailView("No suggestions yet.");

        SuggestThingsState state = viewModel.getState();
        assertEquals("No suggestions yet.", state.getErrorMessage());
        assertTrue(state.getSuggestions().isEmpty());
        assertEquals("", state.getTitle());
    }
}
