package view;

import interface_adapter.customize_board.CustomizeBoardController;
import interface_adapter.customize_board.CustomizeBoardViewModel;
import interface_adapter.customize_board.CustomizeBoardViewModel.Board;
import interface_adapter.customize_board.CustomizeBoardViewModel.SearchResults;
import interface_adapter.customize_board.CustomizeBoardViewModel.State;
import use_case.customize_board.PictogramData;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;

/** Caregiver controls for the main board, linked things and ARASAAC search. */
public final class CustomizeBoardPanel extends JPanel {
    private final CustomizeBoardController controller;
    private final PictogramThumbnails thumbnails = new PictogramThumbnails(this);
    private final JLabel boardTitle = new JLabel("Main board");
    private final JLabel linkTitle = new JLabel("Select an action to see its linked things.");
    private final JLabel boardMessage = new JLabel(" ");
    private final JLabel searchMessage = new JLabel(" ");
    private final JList<PictogramData> boardTiles = createTileList();
    private final JList<PictogramData> linkedTiles = createTileList();
    private final JList<PictogramData> results = createTileList();
    private final JTextField keyword = new JTextField();
    private final JButton search = new JButton("Search");
    private final JButton remove = new JButton("Remove tile");
    private final JButton unlink = new JButton("Remove link");
    private final JButton add = new JButton("Add to board");
    private final JButton link = new JButton("Link to action");
    private final JComboBox<String> type = new JComboBox<>(new String[]{"Thing", "Action"});
    private Board shownBoard;
    private SearchResults shownResults;

    public CustomizeBoardPanel(Navigator nav, CustomizeBoardController controller,
                               CustomizeBoardViewModel viewModel) {
        this.controller = controller;
        setLayout(new BorderLayout());
        add(new HeaderBar(nav, ScreenNames.CAREGIVER_MENU, "Customize Board"), BorderLayout.NORTH);

        JPanel editor = new JPanel(new GridLayout(3, 1, 0, 8));
        editor.setBorder(BorderFactory.createEmptyBorder(0, 12, 8, 12));
        editor.add(listSection(boardTitle, boardTiles, remove));
        editor.add(listSection(linkTitle, linkedTiles, unlink));
        editor.add(searchSection());
        add(editor, BorderLayout.CENTER);
        add(boardMessage, BorderLayout.SOUTH);

        boardTiles.addListSelectionListener(event -> refreshLinks(null));
        linkedTiles.addListSelectionListener(event -> updateButtons());
        results.addListSelectionListener(event -> {
            PictogramData result = results.getSelectedValue();
            if (result != null) {
                type.setSelectedIndex(result.isAction() ? 1 : 0);
            }
            updateButtons();
        });
        type.addActionListener(event -> updateButtons());
        search.addActionListener(event -> search());
        keyword.addActionListener(event -> search());
        add.addActionListener(event ->
                controller.addToBoard(results.getSelectedValue(), type.getSelectedIndex() == 1));
        link.addActionListener(event ->
                controller.linkThing(boardTiles.getSelectedValue().getId(), results.getSelectedValue()));
        remove.addActionListener(event -> removeTile());
        unlink.addActionListener(event -> controller.unlinkThing(boardTiles.getSelectedValue().getId(),
                linkedTiles.getSelectedValue().getId()));

        viewModel.addPropertyChangeListener(event -> render(viewModel.getState()));
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                controller.open();
            }
        });
        render(viewModel.getState());
    }

    private JPanel listSection(JLabel title, JList<PictogramData> list, JButton button) {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.add(title, BorderLayout.NORTH);
        panel.add(new JScrollPane(list), BorderLayout.CENTER);
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        controls.add(button);
        panel.add(controls, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel searchSection() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        JPanel input = new JPanel(new BorderLayout(4, 0));
        JLabel title = new JLabel("Search ARASAAC");
        title.setLabelFor(keyword);
        input.add(title, BorderLayout.WEST);
        input.add(keyword, BorderLayout.CENTER);
        input.add(search, BorderLayout.EAST);
        panel.add(input, BorderLayout.NORTH);
        panel.add(new JScrollPane(results), BorderLayout.CENTER);

        JPanel footer = new JPanel(new BorderLayout());
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 0));
        JLabel typeLabel = new JLabel("Type");
        typeLabel.setLabelFor(type);
        controls.add(typeLabel);
        controls.add(type);
        controls.add(add);
        controls.add(link);
        footer.add(searchMessage, BorderLayout.NORTH);
        footer.add(controls, BorderLayout.SOUTH);
        panel.add(footer, BorderLayout.SOUTH);
        return panel;
    }

    private JList<PictogramData> createTileList() {
        JList<PictogramData> list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellHeight(42);
        list.setFixedCellWidth(260);
        DefaultListCellRenderer renderer = new DefaultListCellRenderer();
        list.setCellRenderer((items, tile, index, selected, focused) -> {
            String label = tile.getLabel() + (tile.isAction() ? " (action)" : " (thing)");
            JLabel row = (JLabel) renderer.getListCellRendererComponent(items, label, index, selected, focused);
            row.setIcon(thumbnails.get(tile.getImageLocation()));
            return row;
        });
        return list;
    }

    private void search() {
        if (!search.isEnabled()) {
            return;
        }
        String query = keyword.getText();
        search.setEnabled(false);
        searchMessage.setText("Searching ARASAAC…");
        results.setListData(new PictogramData[0]);
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                controller.search(query);
                return null;
            }

            @Override
            protected void done() {
                try {
                    get();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    searchFailed();
                } catch (java.util.concurrent.ExecutionException exception) {
                    searchFailed();
                }
            }
        }.execute();
    }

    private void searchFailed() {
        searchMessage.setText("Search failed. Try again in a moment.");
        search.setEnabled(true);
    }

    private void removeTile() {
        PictogramData tile = boardTiles.getSelectedValue();
        List<PictogramData> things = shownBoard.linkedThings.getOrDefault(tile.getId(), List.of());
        if (things.isEmpty() || JOptionPane.showConfirmDialog(this,
                "Remove " + tile.getLabel() + " and its linked things?", "Remove action",
                JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
            controller.removeFromBoard(tile.getId());
        }
    }

    private void render(State state) {
        boardMessage.setText(state.boardMessage);
        boardMessage.setToolTipText(state.boardMessage);
        if (state.board != shownBoard) {
            PictogramData selection = boardTiles.getSelectedValue();
            PictogramData linkedSelection = linkedTiles.getSelectedValue();
            shownBoard = state.board;
            boardTitle.setText("Main board (" + shownBoard.tiles.size() + "/" + shownBoard.capacity + ")");
            showTiles(boardTiles, shownBoard.tiles, selection);
            refreshLinks(linkedSelection);
        }
        if (state.searchResults != shownResults) {
            shownResults = state.searchResults;
            showTiles(results, shownResults.tiles, null);
            searchMessage.setText(shownResults.message);
            search.setEnabled(true);
        }
        updateButtons();
    }

    private void refreshLinks(PictogramData selection) {
        PictogramData tile = boardTiles.getSelectedValue();
        boolean actionSelected = tile != null && tile.isAction();
        linkTitle.setText(actionSelected
                ? "Things linked to " + tile.getLabel() : "Select an action to see its linked things.");
        List<PictogramData> things = actionSelected
                ? shownBoard.linkedThings.getOrDefault(tile.getId(), List.of()) : List.of();
        showTiles(linkedTiles, things, selection);
        updateButtons();
    }

    private static void showTiles(JList<PictogramData> list, List<PictogramData> tiles,
                                  PictogramData selection) {
        list.setListData(tiles.toArray(new PictogramData[0]));
        if (selection != null) {
            tiles.stream().filter(tile -> tile.getId() == selection.getId()).findFirst()
                    .ifPresent(tile -> list.setSelectedValue(tile, true));
        }
    }

    private void updateButtons() {
        PictogramData tile = boardTiles.getSelectedValue();
        boolean hasResult = results.getSelectedValue() != null;
        remove.setEnabled(tile != null);
        unlink.setEnabled(tile != null && tile.isAction() && linkedTiles.getSelectedValue() != null);
        add.setEnabled(hasResult);
        type.setEnabled(hasResult);
        link.setEnabled(hasResult && type.getSelectedIndex() == 0 && tile != null && tile.isAction());
    }
}
