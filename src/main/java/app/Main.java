package app;

import data_access.ArasaacDataAccessObject;
import data_access.JsonProfileDataAccessObject;
import entity.ChildProfile;
import entity.CommunicationBoard;
import entity.CommunicationDraft;
import entity.CommunicationMessage;
import entity.Pictogram;
import interface_adapter.communicate_request.CommunicateRequestController;
import interface_adapter.communicate_request.CommunicateRequestPresenter;
import interface_adapter.communicate_request.CommunicateRequestViewModel;
import interface_adapter.customize_board.CustomizeBoardController;
import interface_adapter.customize_board.CustomizeBoardPresenter;
import interface_adapter.customize_board.CustomizeBoardViewModel;
import interface_adapter.view_history.ViewHistoryController;
import interface_adapter.view_history.ViewHistoryPresenter;
import interface_adapter.view_history.ViewHistoryViewModel;
import use_case.communicate_request.CommunicateRequestInteractor;
import use_case.customize_board.CustomizeBoardInteractor;
import use_case.view_history.ViewHistoryInteractor;
import view.AppFrame;
import view.CustomizeBoardPanel;
import view.HistoryPanel;
import view.MainBoardPanel;
import view.ScreenNames;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Builds the application and connects dependencies.
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

            CommunicationDraft draft =
                    new CommunicationDraft("I want");

            AppFrame frame = new AppFrame();

            addCommunicateRequestUseCase(
                    frame,
                    dataAccess,
                    draft);

            addViewHistoryUseCase(
                    frame,
                    dataAccess);

            addCustomizeBoardUseCase(frame, dataAccess);

            frame.show(ScreenNames.MAIN);
            frame.setVisible(true);
        });
    }

    private static void addCommunicateRequestUseCase(
            AppFrame frame,
            JsonProfileDataAccessObject dataAccess,
            CommunicationDraft draft) {

        CommunicateRequestViewModel viewModel =
                new CommunicateRequestViewModel();

        CommunicateRequestPresenter presenter =
                new CommunicateRequestPresenter(viewModel);

        CommunicateRequestInteractor interactor =
                new CommunicateRequestInteractor(
                        dataAccess,
                        presenter,
                        draft);

        CommunicateRequestController controller =
                new CommunicateRequestController(interactor);

        frame.addScreen(
                new MainBoardPanel(
                        frame,
                        controller,
                        viewModel),
                ScreenNames.MAIN);
    }

    private static void addViewHistoryUseCase(
            AppFrame frame,
            JsonProfileDataAccessObject dataAccess) {

        ViewHistoryViewModel viewModel =
                new ViewHistoryViewModel();

        ViewHistoryPresenter presenter =
                new ViewHistoryPresenter(viewModel);

        ViewHistoryInteractor interactor =
                new ViewHistoryInteractor(
                        dataAccess,
                        presenter);

        ViewHistoryController controller =
                new ViewHistoryController(interactor);

        frame.addScreen(
                new HistoryPanel(
                        frame,
                        controller,
                        viewModel),
                ScreenNames.HISTORY);
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
        CommunicationBoard board = new CommunicationBoard();

        Pictogram water =
                new Pictogram(1, "water", "water.png");

        Pictogram mom =
                new Pictogram(2, "mom", "mom.png");

        Pictogram food =
                new Pictogram(3, "food", "food.png");

        Pictogram play =
                new Pictogram(4, "play", "play.png");

        Pictogram toilet =
                new Pictogram(5, "toilet", "toilet.png");

        Pictogram sleep =
                new Pictogram(6, "sleep", "sleep.png");

        board.addPictogram(water);
        board.addPictogram(mom);
        board.addPictogram(food);
        board.addPictogram(play);
        board.addPictogram(toilet);
        board.addPictogram(sleep);

        ChildProfile profile =
                new ChildProfile(board, new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();

        profile.addMessage(
                new CommunicationMessage(
                        List.of(mom),
                        "I want mom.",
                        now.minusDays(1)
                                .withHour(15)
                                .withMinute(20)));

        profile.addMessage(
                new CommunicationMessage(
                        List.of(water),
                        "I want water.",
                        now.withHour(10)
                                .withMinute(0)));

        return profile;
    }
}