package view;

import interface_adapter.suggest_things.SuggestThingsController;
import interface_adapter.suggest_things.SuggestThingsState;
import interface_adapter.suggest_things.SuggestThingsViewModel;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;

/**
 * UC6: a row of suggested things for the selected action, e.g.
 * "Suggested for play: ball (Used 2 times), toy (Used once), park".
 * Tapping a suggestion hands it to {@code onPick}, so it is added through
 * the normal customize-board use case.
 */
public class SuggestionsStrip extends JPanel {

    private final SuggestThingsController controller;
    private final SuggestThingsViewModel viewModel;
    private final JLabel title = new JLabel(" ");
    private final JPanel cards = new JPanel(new GridLayout(1, 0, 6, 0));
    private Consumer<SuggestThingsState.SuggestionItem> onPick = item -> { };

    /** Increases with every load, so an older, slower load never replaces a newer one. */
    private int latestRequest;

    public SuggestionsStrip(SuggestThingsController controller, SuggestThingsViewModel viewModel) {
        this.controller = controller;
        this.viewModel = viewModel;

        setLayout(new BorderLayout(8, 0));
        setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        add(title, BorderLayout.WEST);
        add(cards, BorderLayout.CENTER);
        clear();
    }

    /** What to do when the caregiver taps a suggestion. */
    public void setOnPick(Consumer<SuggestThingsState.SuggestionItem> onPick) {
        this.onPick = onPick;
    }

    /** Shows nothing, e.g. when no action is selected. */
    public void clear() {
        latestRequest++;
        title.setText("Select an action to see suggested things.");
        cards.removeAll();
        revalidate();
        repaint();
    }

    /**
     * Loads suggestions for this action in the background, because the starter list
     * may search ARASAAC the first time.
     */
    public void showFor(int actionId) {
        int request = ++latestRequest;
        title.setText("Loading suggestions...");
        cards.removeAll();
        revalidate();
        repaint();

        new SwingWorker<Snapshot, Void>() {
            @Override
            protected Snapshot doInBackground() {
                // One load at a time, and read the result before another load can change it.
                synchronized (viewModel) {
                    controller.execute(actionId);
                    return new Snapshot(viewModel.getState());
                }
            }

            @Override
            protected void done() {
                if (request != latestRequest) {
                    return;   // the caregiver already picked another action
                }
                try {
                    show(get());
                } catch (Exception e) {
                    title.setText("Could not load suggestions.");
                }
            }
        }.execute();
    }

    private void show(Snapshot snapshot) {
        cards.removeAll();
        if (snapshot.errorMessage != null) {
            title.setText(snapshot.errorMessage);
        } else {
            title.setText(snapshot.title.replace("Suggestions for", "Suggested for") + ":");
            for (SuggestThingsState.SuggestionItem item : snapshot.suggestions) {
                JButton button = new JButton(card(item.getLabel(), item.getHint()));
                button.setToolTipText("Link " + item.getLabel() + " to this action");
                button.addActionListener(e -> onPick.accept(item));
                cards.add(button);
            }
        }
        revalidate();
        repaint();
    }

    /** A two-line button label: the word, and a small note under it if there is one. */
    private static String card(String word, String note) {
        String text = "<html><center><b>" + escape(word) + "</b>";
        if (!note.isEmpty()) {
            text += "<br><span style='font-size:9px'>" + escape(note) + "</span>";
        }
        return text + "</center></html>";
    }

    private static String escape(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    /** A copy of the view model state taken right after one load. */
    private static final class Snapshot {
        private final String title;
        private final String errorMessage;
        private final List<SuggestThingsState.SuggestionItem> suggestions;

        private Snapshot(SuggestThingsState state) {
            this.title = state.getTitle();
            this.errorMessage = state.getErrorMessage();
            this.suggestions = List.copyOf(state.getSuggestions());
        }
    }
}
