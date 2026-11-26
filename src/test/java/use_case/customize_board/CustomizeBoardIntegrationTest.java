package use_case.customize_board;

import data_access.JsonProfileDataAccessObject;
import entity.ChildProfile;
import entity.CommunicationMessage;
import entity.Pictogram;
import entity.WordType;
import interface_adapter.customize_board.CustomizeBoardController;
import interface_adapter.customize_board.CustomizeBoardPresenter;
import interface_adapter.customize_board.CustomizeBoardViewModel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CustomizeBoardIntegrationTest {
    private static final PictogramData PLAY = new PictogramData(10, "play", "play.png", true);
    private static final PictogramData BALL = new PictogramData(20, "ball", "ball.png", false);

    @TempDir
    Path directory;
    private Path file;
    private JsonProfileDataAccessObject profile;
    private CustomizeBoardViewModel model;
    private CustomizeBoardController controller;

    @BeforeEach
    void setUp() {
        file = directory.resolve("data/profile.json");
        profile = new JsonProfileDataAccessObject(file, new ChildProfile());
        model = new CustomizeBoardViewModel();
        PictogramSearchDataAccessInterface search = keyword -> {
            if ("offline".equals(keyword)) {
                throw new PictogramSearchException("Network error", null);
            }
            if ("water".equals(keyword)) {
                return List.of(new Pictogram(30, "water", "water.png"));
            }
            return List.of();
        };
        controller = new CustomizeBoardController(new CustomizeBoardInteractor(
                profile, search, new CustomizeBoardPresenter(model, Runnable::run)));
        controller.open();
    }

    @Test
    void searchAddAndLinkSurviveRestartWithoutChangingHistory() {
        Pictogram ball = new Pictogram(20, "ball", "ball.png");
        LocalDateTime sent = LocalDateTime.of(2026, 11, 25, 12, 30);
        profile.saveMessage(new CommunicationMessage(List.of(ball), "I want ball.", sent));

        controller.search("  water ");
        PictogramData water = model.getState().searchResults.tiles.get(0);
        controller.addToBoard(water, false);
        controller.addToBoard(PLAY, true);
        controller.linkThing(PLAY.getId(), BALL);

        JsonProfileDataAccessObject reopened = new JsonProfileDataAccessObject(file, new ChildProfile());
        List<Pictogram> tiles = reopened.getBoard().getPictograms();
        assertEquals(List.of("water", "play"), List.of(tiles.get(0).getLabel(), tiles.get(1).getLabel()));
        assertEquals(WordType.THING, tiles.get(0).getType());
        assertEquals(WordType.ACTION, tiles.get(1).getType());
        assertEquals(List.of(ball), reopened.getLinks().getThings(tiles.get(1)));
        assertEquals("I want ball.", reopened.getHistory().get(0).getText());
        assertEquals(sent, reopened.getHistory().get(0).getTimestamp());
    }

    @Test
    void rejectsDuplicatesAndAnOverfullBoard() {
        controller.addToBoard(PLAY, true);
        controller.addToBoard(new PictogramData(10, "same ID", "other.png", false), false);
        assertTrue(model.getState().boardMessage.contains("already on the board"));
        for (int index = 0; index < 5; index++) {
            controller.addToBoard(new PictogramData(100 + index, "thing " + index, "thing.png", false), false);
        }
        controller.addToBoard(BALL, false);
        assertEquals("board is full", model.getState().boardMessage);
        assertEquals(6, profile.getBoard().size());
    }

    @Test
    void removingLinksAndActionsPreservesStandaloneTiles() {
        controller.addToBoard(PLAY, true);
        controller.addToBoard(BALL, false);
        controller.linkThing(PLAY.getId(), BALL);
        controller.linkThing(PLAY.getId(), BALL);
        assertTrue(model.getState().boardMessage.contains("already linked"));
        controller.unlinkThing(PLAY.getId(), BALL.getId());
        assertTrue(model.getState().board.linkedThings.get(PLAY.getId()).isEmpty());

        controller.linkThing(PLAY.getId(), BALL);
        controller.removeFromBoard(PLAY.getId());
        JsonProfileDataAccessObject reopened = new JsonProfileDataAccessObject(file, new ChildProfile());
        assertEquals(List.of(new Pictogram(20, "ball", "ball.png")), reopened.getBoard().getPictograms());
        assertTrue(reopened.getLinks().getLinkedActions().isEmpty());
    }

    @Test
    void distinguishesBlankNoResultsAndOfflineSearch() {
        controller.search(" ");
        assertEquals("Enter a word to search.", model.getState().searchResults.message);
        controller.search("unmatched");
        assertEquals("No pictograms found.", model.getState().searchResults.message);
        controller.search("offline");
        assertTrue(model.getState().searchResults.message.contains("ARASAAC search failed"));
        assertTrue(model.getState().searchResults.tiles.isEmpty());
        assertFalse(Files.exists(file));
    }

    @Test
    void aFailedWriteKeepsTheActiveBoardUnchanged() throws IOException {
        Files.createFile(file.getParent());
        controller.addToBoard(BALL, false);

        assertTrue(model.getState().boardMessage.contains("Could not save"));
        assertTrue(profile.getBoard().isEmpty());

        Files.delete(file.getParent());
        controller.addToBoard(BALL, false);
        assertEquals(1, profile.getBoard().size());
    }

    @Test
    void loadsAnExistingProfileWithoutTypesOrLinks() throws IOException {
        Files.createDirectories(file.getParent());
        Files.writeString(file, "{\"board\":[{\"id\":20,\"label\":\"ball\","
                + "\"imageLocation\":\"ball.png\"}],\"history\":[],\"pin\":\"1234\"}");

        JsonProfileDataAccessObject reopened = new JsonProfileDataAccessObject(file, new ChildProfile());
        assertEquals(WordType.THING, reopened.getBoard().getPictograms().get(0).getType());
        assertTrue(reopened.getLinks().getLinkedActions().isEmpty());
        reopened.saveBoard(reopened.getBoard());
        assertTrue(Files.readString(file).contains("\"1234\""));
    }
}
