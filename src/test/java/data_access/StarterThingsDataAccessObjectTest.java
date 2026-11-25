package data_access;

import entity.Pictogram;
import entity.WordType;
import org.junit.jupiter.api.Test;
import use_case.customize_board.PictogramSearchDataAccessInterface;
import use_case.customize_board.PictogramSearchException;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StarterThingsDataAccessObjectTest {

    /** A fake search: every word returns one pictogram with that word as its label. */
    private final List<String> searchedWords = new ArrayList<>();
    private final PictogramSearchDataAccessInterface fakeSearch = word -> {
        searchedWords.add(word);
        return List.of(new Pictogram(word.length(), word, word + ".png"));
    };

    @Test
    void returnsStarterThingsInOrder() {
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(fakeSearch);

        List<Pictogram> things = dataAccess.getStarterThings("drink");

        assertEquals(List.of("water", "milk", "juice"), labels(things));
    }

    @Test
    void resultsAreAlwaysThings() {
        PictogramSearchDataAccessInterface searchReturningAction = word ->
                List.of(new Pictogram(1, word, word + ".png", WordType.ACTION));
        StarterThingsDataAccessObject dataAccess =
                new StarterThingsDataAccessObject(searchReturningAction);

        for (Pictogram thing : dataAccess.getStarterThings("drink")) {
            assertEquals(WordType.THING, thing.getType());
        }
    }

    @Test
    void actionLabelIgnoresCaseAndSpaces() {
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(fakeSearch);

        assertEquals(3, dataAccess.getStarterThings("  Drink ").size());
    }

    @Test
    void unknownActionHasNoStarterThings() {
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(fakeSearch);

        assertTrue(dataAccess.getStarterThings("jump").isEmpty());
        assertTrue(searchedWords.isEmpty());
    }

    @Test
    void prefersResultWithExactLabel() {
        PictogramSearchDataAccessInterface search = word -> List.of(
                new Pictogram(1, "glass of " + word, "a.png"),
                new Pictogram(2, word, "b.png"));
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(search);

        assertEquals(2, dataAccess.getStarterThings("drink").get(0).getId());
    }

    @Test
    void eachWordIsSearchedOnlyOnce() {
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(fakeSearch);

        dataAccess.getStarterThings("drink");
        dataAccess.getStarterThings("drink");

        assertEquals(List.of("water", "milk", "juice"), searchedWords);
    }

    @Test
    void failedSearchSkipsThatWord() {
        PictogramSearchDataAccessInterface search = word -> {
            if (word.equals("milk")) {
                throw new PictogramSearchException("offline", null);
            }
            return List.of(new Pictogram(1, word, word + ".png"));
        };
        StarterThingsDataAccessObject dataAccess = new StarterThingsDataAccessObject(search);

        assertEquals(List.of("water", "juice"), labels(dataAccess.getStarterThings("drink")));
    }

    private static List<String> labels(List<Pictogram> pictograms) {
        List<String> labels = new ArrayList<>();
        for (Pictogram pictogram : pictograms) {
            labels.add(pictogram.getLabel());
        }
        return labels;
    }
}