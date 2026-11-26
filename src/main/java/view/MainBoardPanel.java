package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.List;
import java.util.function.Supplier;

public class MainBoardPanel extends JPanel {
    private final JTextField messageBar = new JTextField();

    public MainBoardPanel(Navigator nav, Supplier<List<String>> boardLabels) {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // top right: acorn opens caregiver mode
        ImageIcon icon = Icons.acorn(40);
        JButton acorn = new JButton(icon);
        if (icon == null) {
            acorn.setText("Caregiver");
        }
        acorn.addActionListener(e -> nav.show(ScreenNames.PIN));
        JPanel top = new JPanel(new BorderLayout());
        top.add(acorn, BorderLayout.EAST);

        // message bar
        messageBar.setEditable(false);
        messageBar.setFont(messageBar.getFont().deriveFont(20f));
        JPanel messageBlock = new JPanel(new BorderLayout());
        messageBlock.add(new JLabel("My message"), BorderLayout.NORTH);
        messageBlock.add(messageBar, BorderLayout.CENTER);

        JPanel grid = new JPanel(new GridLayout(2, 3, 8, 8));
        refreshTiles(grid, boardLabels.get());
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                refreshTiles(grid, boardLabels.get());
            }
        });

        JButton speak = new JButton("Speak");
        speak.addActionListener(e -> System.out.println("TODO speak: " + messageBar.getText()));

        JPanel centre = new JPanel(new BorderLayout(8, 8));
        centre.add(messageBlock, BorderLayout.NORTH);
        centre.add(grid, BorderLayout.CENTER);
        centre.add(speak, BorderLayout.SOUTH);

        // bottom: Help / Stop / Break show in the message bar
        JPanel bottom = new JPanel(new GridLayout(1, 3, 8, 8));
        for (String s : new String[]{"Help", "Stop", "Break"}) {
            JButton b = new JButton(s);
            // TODO core message use case: build and save the message
            b.addActionListener(e -> messageBar.setText(s));
            bottom.add(b);
        }

        add(top, BorderLayout.NORTH);
        add(centre, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
    }

    private void refreshTiles(JPanel grid, List<String> labels) {
        grid.removeAll();
        for (String label : labels) {
            JButton button = new JButton(label);
            button.addActionListener(event -> messageBar.setText(messageBar.getText() + label + " "));
            grid.add(button);
        }
        if (labels.isEmpty()) {
            grid.add(new JLabel("No pictograms. A caregiver can add them in Customize Board.",
                    SwingConstants.CENTER));
        }
        grid.revalidate();
        grid.repaint();
    }
}