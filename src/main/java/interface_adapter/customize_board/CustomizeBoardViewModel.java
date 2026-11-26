package interface_adapter.customize_board;

import interface_adapter.ViewModel;
import use_case.customize_board.PictogramData;

import java.util.List;
import java.util.Map;

/** Holds the independent board and search state for the caregiver screen. */
public final class CustomizeBoardViewModel extends ViewModel<CustomizeBoardViewModel.State> {
    public CustomizeBoardViewModel() {
        super("Customize Board");
        setState(new State(null, null, " "));
    }

    public static final class State {
        public final Board board;
        public final SearchResults searchResults;
        public final String boardMessage;

        State(Board board, SearchResults searchResults, String boardMessage) {
            this.board = board;
            this.searchResults = searchResults;
            this.boardMessage = boardMessage;
        }
    }

    public static final class Board {
        public final List<PictogramData> tiles;
        public final Map<Integer, List<PictogramData>> linkedThings;
        public final int capacity;

        Board(List<PictogramData> tiles, Map<Integer, List<PictogramData>> linkedThings, int capacity) {
            this.tiles = List.copyOf(tiles);
            this.linkedThings = Map.copyOf(linkedThings);
            this.capacity = capacity;
        }
    }

    public static final class SearchResults {
        public final List<PictogramData> tiles;
        public final String message;

        SearchResults(List<PictogramData> tiles, String message) {
            this.tiles = List.copyOf(tiles);
            this.message = message;
        }
    }
}
