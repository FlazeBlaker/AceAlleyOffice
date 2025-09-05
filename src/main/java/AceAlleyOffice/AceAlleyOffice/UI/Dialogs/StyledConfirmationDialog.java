// File: UI/Dialogs/StyledConfirmationDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

public class StyledConfirmationDialog extends JDialog {
    private boolean confirmed = false;

    /**
     * Default constructor for a "Yes" / "No" confirmation.
     * @param owner The parent component.
     * @param title The dialog title.
     * @param message The message to display.
     */
    public StyledConfirmationDialog(Component owner, String title, String message) {
        this(owner, title, message, "Yes", "No");
    }

    /**
     * Constructor for custom button labels.
     * @param owner The parent component.
     * @param title The dialog title.
     * @param message The message to display.
     * @param confirmText The text for the confirmation button (e.g., "OK", "Yes").
     * @param cancelText The text for the cancellation button (e.g., "Cancel", "No").
     */
    public StyledConfirmationDialog(Component owner, String title, String message, String confirmText, String cancelText) {
        super(owner instanceof Frame ? (Frame) owner : null, title, true);
        setupUI(owner, "<html><div style='text-align: center;'>" + message + "</div></html>", confirmText, cancelText);
    }

    /**
     * Constructor that accepts a Component as the message area.
     * @param owner The parent component.
     * @param title The dialog title.
     * @param messageComponent The custom component to display as the message.
     * @param confirmText The text for the confirmation button.
     * @param cancelText The text for the cancellation button.
     */
    public StyledConfirmationDialog(Component owner, String title, Component messageComponent, String confirmText, String cancelText) {
        super(owner instanceof Frame ? (Frame) owner : null, title, true);
        setupUI(owner, messageComponent, confirmText, cancelText);
    }

    private void setupUI(Component owner, Object message, String confirmText, String cancelText) {
        // Main Panel
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBackground(new Color(45, 45, 45));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Message Component
        Component messageComponent;
        if (message instanceof Component) {
            messageComponent = (Component) message;
        } else {
            messageComponent = new JLabel((String) message);
            ((JLabel)messageComponent).setForeground(Color.WHITE);
            ((JLabel)messageComponent).setFont(new Font("Arial", Font.PLAIN, 16));
            ((JLabel)messageComponent).setHorizontalAlignment(SwingConstants.CENTER);
        }
        mainPanel.add(messageComponent, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);

        JButton confirmButton = new JButton(confirmText);
        styleButton(confirmButton, new Color(60, 179, 113)); // Green for confirm
        confirmButton.addActionListener(e -> {
            confirmed = true;
            dispose();
        });

        JButton cancelButton = new JButton(cancelText);
        styleButton(cancelButton, new Color(220, 53, 69)); // Red for cancel
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(confirmButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        pack();
        setResizable(false);
        setLocationRelativeTo(owner);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));
        button.setOpaque(true);
        button.setContentAreaFilled(true);
    }

    public boolean showDialog() {
        setVisible(true);
        return confirmed;
    }
}