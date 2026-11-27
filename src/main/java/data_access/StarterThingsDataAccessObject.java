package data_access;

import entity.Pictogram;
import entity.WordType;
import use_case.customize_board.PictogramSearchDataAccessInterface;
import use_case.customize_board.PictogramSearchException;
import use_case.suggest_things.StarterThingsDataAccessInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * A built-in list of common things for common actions.
 *
 * <p>Only the words are stored here. Each word is looked up with the pictogram search
 * (ARASAAC) the first time it is needed, and the result is remembered so the same word
 * is never searched twice.
 */
public class StarterThingsDataAccessObject implements StarterThingsDataAccessInterface {

    private static final Map<String, List<String>> STARTER_WORDS = Map.of(
            "play", List.of("ball", "toy", "blocks", "puzzle", "doll", "car"),
            "eat", List.of("apple", "banana", "cookie", "sandwich", "pasta", "yogurt"),
            "drink", List.of("water", "milk", "juice"),
            "go", List.of("park", "school", "home", "bathroom", "playground"),
            "read", List.of("book", "magazine"),
            "watch", List.of("television", "tablet", "movie"));

    private final PictogramSearchDataAccessInterface pictogramSearch;
    private final Map<String, Pictogram> cache = new HashMap<>();

    public StarterThingsDataAccessObject(PictogramSearchDataAccessInterface pictogramSearch) {
        this.pictogramSearch =
                Objects.requireNonNull(pictogramSearch, "pictogramSearch cannot be null");
    }

    @Override
    public List<Pictogram> getStarterThings(String actionLabel) {
        if (actionLabel == null) {
            return List.of();
        }
        List<String> words = STARTER_WORDS.getOrDefault(
                actionLabel.trim().toLowerCase(Locale.ROOT), List.of());

        List<Pictogram> things = new ArrayList<>();
        for (String word : words) {
            Pictogram thing = findThing(word);
            if (thing != null) {
                things.add(thing);
            }
        }
        return things;
    }

    /** Returns a THING pictogram for this word, or null if none could be found. */
    private Pictogram findThing(String word) {
        if (cache.containsKey(word)) {
            return cache.get(word);
        }

        List<Pictogram> results;
        try {
            results = pictogramSearch.searchPictograms(word);
        } catch (PictogramSearchException e) {
            // Offline or ARASAAC failed: skip this word, but do not remember the failure,
            // so it can be tried again next time.
            return null;
        }

        Pictogram best = pickBest(results, word);
        Pictogram thing = best == null ? null : new Pictogram(
                best.getId(), best.getLabel(), best.getImageLocation(), WordType.THING);
        cache.put(word, thing);
        return thing;
    }

    /** Prefers the result whose label is exactly the word; otherwise the first result. */
    private Pictogram pickBest(List<Pictogram> results, String word) {
        for (Pictogram result : results) {
            if (result.getLabel().equalsIgnoreCase(word)) {
                return result;
            }
        }
        return results.isEmpty() ? null : results.get(0);
    }
}