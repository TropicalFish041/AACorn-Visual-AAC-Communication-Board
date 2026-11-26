package app;

import data_access.ArasaacDataAccessObject;
import data_access.JsonProfileDataAccessObject;
import entity.ChildProfile;
import entity.CommunicationBoard;
import entity.CommunicationDraft;
import entity.CommunicationMessage;
import entity.Pictogram;
import entity.WordType;
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
import use_case.customize_board.PictogramSearchException;
import use_case.view_history.ViewHistoryInteractor;
import view.AppFrame;
import view.CustomizeBoardPanel;
import view.HistoryPanel;
import view.MainBoardPanel;
import view.ScreenNames;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Builds the application and connects dependencies. */
public class Main {

    public static void main(String[] args) {
        Path path = Path.of("data", "profile.json");
        ArasaacDataAccessObject arasaac = new ArasaacDataAccessObject();
        // Initial API requests run on worker threads, before Swing is constructed.
        // An existing profile always takes precedence; never replace a caregiver's edits.
        boolean firstRun = Files.notExists(path);
        ChildProfile initial = firstRun ? createSampleProfile(arasaac) : new ChildProfile();

        JsonProfileDataAccessObject dataAccess;
        try {
            dataAccess = new JsonProfileDataAccessObject(path, initial);
        } catch (IllegalStateException exception) {
            SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(null, exception.getMessage(),
                    "Could not load profile", JOptionPane.ERROR_MESSAGE));
            return;
        }

        // Persist a fully resolved starter board once, not on every launch.
        // If ARASAAC was unavailable, leave the temporary fallback unsaved.
        if (firstRun && Files.notExists(path)
                && initial.getBoard().getPictograms().stream().allMatch(tile -> tile.getId() > 0)) {
            try {
                dataAccess.saveBoard(initial.getBoard());
            } catch (UncheckedIOException exception) {
                System.err.println("Could not save starter board: " + exception.getMessage());
            }
        }

        SwingUtilities.invokeLater(() -> {
            CommunicationDraft draft = new CommunicationDraft("I want");
            AppFrame frame = new AppFrame();
            addCommunicateRequestUseCase(frame, dataAccess, draft);
            addViewHistoryUseCase(frame, dataAccess);
            addCustomizeBoardUseCase(frame, dataAccess, arasaac);
            frame.show(ScreenNames.MAIN);
            frame.setVisible(true);
        });
    }

    private static void addCommunicateRequestUseCase(
            AppFrame frame, JsonProfileDataAccessObject dataAccess, CommunicationDraft draft) {
        CommunicateRequestViewModel viewModel = new CommunicateRequestViewModel();
        CommunicateRequestPresenter presenter = new CommunicateRequestPresenter(viewModel);
        CommunicateRequestInteractor interactor = new CommunicateRequestInteractor(dataAccess, presenter, draft);
        CommunicateRequestController controller = new CommunicateRequestController(interactor);
        frame.addScreen(new MainBoardPanel(frame, controller, viewModel), ScreenNames.MAIN);
    }

    private static void addViewHistoryUseCase(AppFrame frame, JsonProfileDataAccessObject dataAccess) {
        ViewHistoryViewModel viewModel = new ViewHistoryViewModel();
        ViewHistoryPresenter presenter = new ViewHistoryPresenter(viewModel);
        ViewHistoryInteractor interactor = new ViewHistoryInteractor(dataAccess, presenter);
        ViewHistoryController controller = new ViewHistoryController(interactor);
        frame.addScreen(new HistoryPanel(frame, controller, viewModel), ScreenNames.HISTORY);
    }

    private static void addCustomizeBoardUseCase(AppFrame frame, JsonProfileDataAccessObject dataAccess,
                                                 ArasaacDataAccessObject arasaac) {
        CustomizeBoardViewModel viewModel = new CustomizeBoardViewModel();
        CustomizeBoardPresenter presenter = new CustomizeBoardPresenter(viewModel, SwingUtilities::invokeLater);
        CustomizeBoardInteractor interactor = new CustomizeBoardInteractor(dataAccess, arasaac, presenter);
        CustomizeBoardController controller = new CustomizeBoardController(interactor);
        frame.addScreen(new CustomizeBoardPanel(frame, controller, viewModel), ScreenNames.CUSTOMIZE);
    }

    /** Six starter tiles, with actual IDs and image URLs from ARASAAC. */
    private static ChildProfile createSampleProfile(ArasaacDataAccessObject arasaac) {
        String[] labels = {"water", "mom", "food", "play", "toilet", "sleep"};
        String[] queries = {"water", "mother", "food", "play", "toilet", "sleep"};
        WordType[] types = {WordType.THING, WordType.THING, WordType.THING,
                WordType.ACTION, WordType.THING, WordType.ACTION};
        ExecutorService workers = Executors.newFixedThreadPool(labels.length);
        CommunicationBoard board = new CommunicationBoard();
        try {
            List<CompletableFuture<List<Pictogram>>> lookups = new ArrayList<>();
            for (String query : queries) {
                lookups.add(CompletableFuture.supplyAsync(() -> searchStarter(arasaac, query), workers));
            }
            for (int i = 0; i < labels.length; i++) {
                // Keep board order and capacity. Do not substitute unrelated search results.
                Pictogram selected = null;
                for (Pictogram candidate : lookups.get(i).join()) {
                    String name = candidate.getLabel();
                    boolean matches = name.equalsIgnoreCase(queries[i])
                            || name.equalsIgnoreCase(labels[i])
                            || (types[i] == WordType.ACTION && name.equalsIgnoreCase("to " + queries[i]));
                    if (matches && !board.contains(candidate)) {
                        if (selected == null || candidate.getType() == types[i]) {
                            selected = candidate;
                        }
                        if (candidate.getType() == types[i]) {
                            break;
                        }
                    }
                }
                if (selected == null) {
                    // Text-only offline fallback; never use a fabricated ARASAAC image ID.
                    System.err.println("No matching ARASAAC starter pictogram for " + labels[i]);
                    board.addPictogram(new Pictogram(-i - 1, labels[i], labels[i] + ".png", types[i]));
                } else {
                    board.addPictogram(new Pictogram(selected.getId(), labels[i],
                            selected.getImageLocation(), types[i]));
                }
            }
        } finally {
            workers.shutdown();
        }

        ChildProfile profile = new ChildProfile(board, new ArrayList<>());
        LocalDateTime now = LocalDateTime.now();
        Pictogram water = board.getPictograms().get(0);
        Pictogram mom = board.getPictograms().get(1);
        profile.addMessage(new CommunicationMessage(List.of(mom), "I want mom.",
                now.minusDays(1).withHour(15).withMinute(20)));
        profile.addMessage(new CommunicationMessage(List.of(water), "I want water.",
                now.withHour(10).withMinute(0)));
        return profile;
    }

    private static List<Pictogram> searchStarter(ArasaacDataAccessObject arasaac, String query) {
        try {
            return arasaac.searchPictograms(query);
        } catch (PictogramSearchException exception) {
            System.err.println("ARASAAC starter search failed for " + query + ": " + exception.getMessage());
            return List.of();
        }
    }
}
