package use_case.customize_board;

import java.util.List;
import java.util.Map;

public interface CustomizeBoardOutputBoundary {
    void showBoard(List<PictogramData> tiles, Map<Integer, List<PictogramData>> linkedThings,
                   int capacity, String message);

    void showValidationError(String message);

    void showSearchResults(List<PictogramData> results, String message);
}
