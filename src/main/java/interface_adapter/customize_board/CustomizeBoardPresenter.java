package interface_adapter.customize_board;

import interface_adapter.customize_board.CustomizeBoardViewModel.Board;
import interface_adapter.customize_board.CustomizeBoardViewModel.SearchResults;
import interface_adapter.customize_board.CustomizeBoardViewModel.State;
import use_case.customize_board.CustomizeBoardOutputBoundary;
import use_case.customize_board.PictogramData;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/** Publishes use-case results, dispatching background search results to the UI thread. */
public final class CustomizeBoardPresenter implements CustomizeBoardOutputBoundary {
    private final CustomizeBoardViewModel viewModel;
    private final Executor uiExecutor;

    public CustomizeBoardPresenter(CustomizeBoardViewModel viewModel, Executor uiExecutor) {
        this.viewModel = viewModel;
        this.uiExecutor = uiExecutor;
    }

    @Override
    public void showBoard(List<PictogramData> tiles, Map<Integer, List<PictogramData>> linkedThings,
                          int capacity, String message) {
        publish(new State(new Board(tiles, linkedThings, capacity),
                viewModel.getState().searchResults, message));
    }

    @Override
    public void showValidationError(String message) {
        State state = viewModel.getState();
        publish(new State(state.board, state.searchResults, message));
    }

    @Override
    public void showSearchResults(List<PictogramData> tiles, String message) {
        SearchResults results = new SearchResults(tiles, message);
        uiExecutor.execute(() -> {
            State state = viewModel.getState();
            publish(new State(state.board, results, state.boardMessage));
        });
    }

    private void publish(State state) {
        viewModel.setState(state);
        viewModel.firePropertyChange();
    }
}
