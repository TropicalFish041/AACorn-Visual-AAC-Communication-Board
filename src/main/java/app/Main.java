package app;

import data_access.ArasaacDataAccessObject;
import data_access.JsonProfileDataAccessObject;
import entity.ChildProfile;
import entity.CommunicationMessage;
import entity.Pictogram;
import interface_adapter.customize_board.CustomizeBoardController;
import interface_adapter.customize_board.CustomizeBoardPresenter;
import interface_adapter.customize_board.CustomizeBoardViewModel;
import interface_adapter.view_history.ViewHistoryController;
import interface_adapter.view_history.ViewHistoryPresenter;
import interface_adapter.view_history.ViewHistoryViewModel;
import use_case.customize_board.CustomizeBoardInteractor;
import use_case.view_history.ViewHistoryInteractor;
import view.AppFrame;
import view.CustomizeBoardPanel;
import view.HistoryPanel;
import view.ScreenNames;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the app: creates the data access, use cases and screens,
 * and connects them together.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JsonProfileDataAccessObject dataAccess;
            try {
                dataAccess = new JsonProfileDataAccessObject(
                        Path.of("data", "profile.json"), createSampleProfile());
            } catch (IllegalStateException exception) {
                JOptionPane.showMessageDialog(null, exception.getMessage(),
                        "Could not load profile", JOptionPane.ERROR_MESSAGE);
                return;
            }

            AppFrame frame = new AppFrame(() -> dataAccess.getBoard().getPictograms().stream()
                    .map(Pictogram::getLabel).collect(Collectors.toList()));
            addCustomizeBoardUseCase(frame, dataAccess);
            addViewHistoryUseCase(frame, dataAccess);
            frame.setVisible(true);
        });
    }

    private static void addViewHistoryUseCase(AppFrame frame,
                                              JsonProfileDataAccessObject dataAccess) {
        ViewHistoryViewModel viewModel = new ViewHistoryViewModel();
        ViewHistoryPresenter presenter = new ViewHistoryPresenter(viewModel);
        ViewHistoryInteractor interactor = new ViewHistoryInteractor(dataAccess, presenter);
        ViewHistoryController controller = new ViewHistoryController(interactor);

        frame.addScreen(new HistoryPanel(frame, controller, viewModel), ScreenNames.HISTORY);
    }

    private static void addCustomizeBoardUseCase(AppFrame frame,
                                                 JsonProfileDataAccessObject dataAccess) {
        CustomizeBoardViewModel viewModel = new CustomizeBoardViewModel();
        CustomizeBoardPresenter presenter = new CustomizeBoardPresenter(viewModel, SwingUtilities::invokeLater);
        CustomizeBoardInteractor interactor = new CustomizeBoardInteractor(
                dataAccess, new ArasaacDataAccessObject(), presenter);
        CustomizeBoardController controller = new CustomizeBoardController(interactor);
        frame.addScreen(new CustomizeBoardPanel(frame, controller, viewModel), ScreenNames.CUSTOMIZE);
    }

    /**
     * Sample data for development when no saved profile exists.
     */
    private static ChildProfile createSampleProfile() {
        ChildProfile profile = new ChildProfile();
        Pictogram water = new Pictogram(1, "water", "water.png");
        Pictogram apple = new Pictogram(2, "apple", "apple.png");
        LocalDateTime now = LocalDateTime.now();

        profile.addMessage(new CommunicationMessage(
                List.of(apple), "I want apple.", now.minusDays(1).withHour(15).withMinute(20)));
        profile.addMessage(new CommunicationMessage(
                List.of(water), "I want water.", now.withHour(10).withMinute(0)));
        profile.addMessage(new CommunicationMessage(
                List.of(water, water), "I want water water.", now.withHour(11).withMinute(30)));
        return profile;
    }
}