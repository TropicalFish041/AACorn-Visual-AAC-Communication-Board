package view;

import interface_adapter.caregiver_pin.PinController;
import interface_adapter.caregiver_pin.PinViewModel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

/**
 * Caregiver PIN setup, login and reset screen.
 */
public final class PinPanel extends JPanel {

    private final Navigator nav;
    private final PinController controller;
    private final PinViewModel viewModel;

    private final JLabel pinLabel = new JLabel();
    private final JLabel newPinLabel = new JLabel("New PIN");
    private final JLabel status = new JLabel(" ");

    private final JPasswordField pinField =
            new JPasswordField(10);

    private final JPasswordField newPinField =
            new JPasswordField(10);

    private final JButton submit = new JButton();
    private final JButton reset = new JButton("Reset PIN");

    private boolean resetMode;

    public PinPanel(Navigator nav,
                    PinController controller,
                    PinViewModel viewModel) {

        this.nav = nav;
        this.controller = controller;
        this.viewModel = viewModel;

        buildUi();

        viewModel.addPropertyChangeListener(
                event -> render());

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent event) {
                resetMode = false;
                clearFields();
                controller.open();
            }
        });

        controller.open();
    }

    private void buildUi() {
        setLayout(new BorderLayout(0, 16));
        setBorder(BorderFactory.createEmptyBorder(
                16, 16, 16, 16));

        JButton back = new JButton("BACK", Icons.acorn(160));
        back.setHorizontalTextPosition(SwingConstants.CENTER);
        back.setVerticalTextPosition(SwingConstants.BOTTOM);
        back.setFont(back.getFont().deriveFont(Font.BOLD, 28f));
        back.setPreferredSize(new Dimension(0, 260));

        back.addActionListener(event -> {
            clearFields();
            resetMode = false;
            nav.show(ScreenNames.MAIN);
        });

        add(back, BorderLayout.CENTER);

        JPanel form = new JPanel();
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        pinLabel.setAlignmentX(CENTER_ALIGNMENT);
        newPinLabel.setAlignmentX(CENTER_ALIGNMENT);
        status.setAlignmentX(CENTER_ALIGNMENT);

        pinField.setAlignmentX(CENTER_ALIGNMENT);
        newPinField.setAlignmentX(CENTER_ALIGNMENT);

        pinField.setMaximumSize(new Dimension(200, 30));
        newPinField.setMaximumSize(new Dimension(200, 30));

        submit.setAlignmentX(CENTER_ALIGNMENT);
        reset.setAlignmentX(CENTER_ALIGNMENT);

        submit.addActionListener(event -> submitPin());

        reset.addActionListener(event -> {
            resetMode = !resetMode;
            clearFields();
            status.setText(" ");
            updateForm();
        });

        form.add(pinLabel);
        form.add(Box.createVerticalStrut(4));
        form.add(pinField);
        form.add(newPinLabel);
        form.add(newPinField);
        form.add(Box.createVerticalStrut(8));
        form.add(status);
        form.add(Box.createVerticalStrut(8));
        form.add(submit);
        form.add(Box.createVerticalStrut(4));
        form.add(reset);

        add(form, BorderLayout.SOUTH);
    }

    private void submitPin() {
        String pin = new String(pinField.getPassword());
        String newPin = new String(newPinField.getPassword());

        clearFields();

        if (resetMode) {
            controller.reset(pin, newPin);
        } else {
            controller.submit(pin);
        }
    }

    private void render() {
        PinViewModel.State state = viewModel.getState();

        if (state.authenticated) {
            clearFields();
            resetMode = false;
            nav.show(ScreenNames.CAREGIVER_MENU);
            return;
        }

        if (!state.configured) {
            resetMode = false;
        }

        status.setText(state.message);
        updateForm();
    }

    private void updateForm() {
        boolean configured = viewModel.getState().configured;

        if (resetMode) {
            pinLabel.setText("Current PIN");
            submit.setText("Reset PIN");
        } else if (configured) {
            pinLabel.setText("Enter PIN");
            submit.setText("Submit");
        } else {
            pinLabel.setText("Set a four-digit PIN");
            submit.setText("Set PIN");
        }

        newPinLabel.setVisible(resetMode);
        newPinField.setVisible(resetMode);

        reset.setVisible(configured);
        reset.setText(resetMode ? "Cancel reset" : "Reset PIN");

        revalidate();
        repaint();
    }

    private void clearFields() {
        pinField.setText("");
        newPinField.setText("");
    }
}
