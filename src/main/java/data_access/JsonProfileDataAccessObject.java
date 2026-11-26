package data_access;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import entity.ChildProfile;
import entity.CommunicationBoard;
import entity.CommunicationMessage;
import entity.LinkedVocabulary;
import entity.Pictogram;
import entity.WordType;
import use_case.communicate_request.CommunicateRequestDataAccessInterface;
import use_case.core_message.CoreMessageDataAccessInterface;
import use_case.customize_board.BoardCustomizationDataAccess;
import use_case.view_history.ViewHistoryDataAccessInterface;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Reads and saves the shared profile in the project's local JSON format. */
public final class JsonProfileDataAccessObject implements BoardCustomizationDataAccess,
        CommunicateRequestDataAccessInterface, CoreMessageDataAccessInterface,
        ViewHistoryDataAccessInterface {

    private final Path file;
    private final ObjectMapper mapper = new ObjectMapper();
    private ObjectNode document;
    private ChildProfile profile;

    public JsonProfileDataAccessObject(Path file, ChildProfile initialProfile) {
        this.file = file.toAbsolutePath();
        try {
            JsonNode root = mapper.readTree(Files.readAllBytes(this.file));
            if (!(root instanceof ObjectNode)) {
                throw new IOException("Expected a profile JSON object");
            }
            document = (ObjectNode) root;
            profile = parseProfile(document);
        } catch (NoSuchFileException exception) {
            document = mapper.createObjectNode();
            profile = initialProfile;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Could not read " + this.file, exception);
        }
    }

    @Override
    public synchronized CommunicationBoard getBoard() {
        return copyBoard(profile.getBoard());
    }

    @Override
    public synchronized LinkedVocabulary getLinks() {
        return linksForBoard(profile.getBoard(), profile.getLinks());
    }

    @Override
    public synchronized List<CommunicationMessage> getHistory() {
        return profile.getHistory();
    }

    @Override
    public synchronized void saveBoard(CommunicationBoard board) {
        saveVocabulary(board, profile.getLinks());
    }

    @Override
    public synchronized void saveVocabulary(CommunicationBoard board, LinkedVocabulary links) {
        store(new ChildProfile(copyBoard(board), linksForBoard(board, links), profile.getHistory()));
    }

    @Override
    public synchronized void saveMessage(CommunicationMessage message) {
        ChildProfile updated = new ChildProfile(profile.getBoard(), profile.getLinks(), profile.getHistory());
        updated.addMessage(message);
        store(updated);
    }

    private void store(ChildProfile updated) {
        ObjectNode next = document.deepCopy();
        next.put("boardCapacity", updated.getBoard().getCapacity());
        writePictograms(next.putArray("board"), updated.getBoard().getPictograms());
        ArrayNode links = next.putArray("linkedVocabulary");
        for (Pictogram action : updated.getLinks().getLinkedActions()) {
            ObjectNode entry = links.addObject();
            entry.put("actionId", action.getId());
            writePictograms(entry.putArray("things"), updated.getLinks().getThings(action));
        }

        ArrayNode history = next.putArray("history");
        for (CommunicationMessage message : updated.getHistory()) {
            ObjectNode entry = history.addObject();
            entry.put("text", message.getText());
            entry.put("timestamp", message.getTimestamp().toString());
            writePictograms(entry.putArray("pictograms"), message.getSelectedPictograms());
        }

        try {
            Files.createDirectories(file.getParent());
            Path temp = Files.createTempFile(file.getParent(), "aacorn-profile-", ".tmp");
            try {
                mapper.writerWithDefaultPrettyPrinter().writeValue(temp.toFile(), next);
                Files.move(temp, file, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(temp);
            }
        } catch (IOException exception) {
            throw new UncheckedIOException("Could not save " + file, exception);
        }
        document = next;
        profile = updated;
    }

    private ChildProfile parseProfile(ObjectNode source) throws IOException {
        CommunicationBoard board = new CommunicationBoard(
                source.path("boardCapacity").asInt(CommunicationBoard.DEFAULT_CAPACITY));
        for (JsonNode tile : readArray(source, "board")) {
            board.addPictogram(readPictogram(tile));
        }

        LinkedVocabulary links = new LinkedVocabulary();
        for (JsonNode entry : readArray(source, "linkedVocabulary")) {
            int actionId = entry.path("actionId").intValue();
            Pictogram action = board.getPictograms().stream()
                    .filter(tile -> tile.isAction() && tile.getId() == actionId)
                    .findFirst().orElseThrow(() -> new IOException("Missing linked action " + actionId));
            for (JsonNode thing : readArray(entry, "things")) {
                links.link(action, readPictogram(thing));
            }
        }

        List<CommunicationMessage> history = new ArrayList<>();
        for (JsonNode entry : readArray(source, "history")) {
            List<Pictogram> pictograms = new ArrayList<>();
            for (JsonNode tile : readArray(entry, "pictograms")) {
                pictograms.add(readPictogram(tile));
            }
            history.add(new CommunicationMessage(pictograms, entry.path("text").asText(),
                    LocalDateTime.parse(entry.path("timestamp").asText())));
        }
        return new ChildProfile(board, links, history);
    }

    private static Iterable<JsonNode> readArray(JsonNode parent, String field) throws IOException {
        JsonNode array = parent.get(field);
        if (array == null) {
            return List.of();
        }
        if (!array.isArray()) {
            throw new IOException("Expected an array for " + field);
        }
        return array;
    }

    private static Pictogram readPictogram(JsonNode tile) throws IOException {
        if (!tile.path("id").isIntegralNumber() || !tile.path("id").canConvertToInt()) {
            throw new IOException("Expected a pictogram ID");
        }
        return new Pictogram(tile.get("id").intValue(), tile.path("label").asText(),
                tile.path("imageLocation").asText(),
                WordType.valueOf(tile.path("type").asText(WordType.THING.name())));
    }

    private static void writePictograms(ArrayNode array, List<Pictogram> pictograms) {
        for (Pictogram tile : pictograms) {
            ObjectNode entry = array.addObject();
            entry.put("id", tile.getId());
            entry.put("label", tile.getLabel());
            entry.put("imageLocation", tile.getImageLocation());
            entry.put("type", tile.getType().name());
        }
    }

    private static CommunicationBoard copyBoard(CommunicationBoard board) {
        CommunicationBoard copy = new CommunicationBoard(board.getCapacity());
        board.getPictograms().forEach(copy::addPictogram);
        return copy;
    }

    private static LinkedVocabulary linksForBoard(CommunicationBoard board, LinkedVocabulary existing) {
        LinkedVocabulary copy = new LinkedVocabulary();
        for (Pictogram tile : board.getPictograms()) {
            if (tile.isAction()) {
                for (Pictogram thing : existing.getThings(tile)) {
                    copy.link(tile, thing);
                }
            }
        }
        return copy;
    }
}
