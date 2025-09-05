// File: Dialogs/AboutDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class AboutDialog extends JDialog {

    // --- Styling Constants ---
    private static final Color COLOR_BACKGROUND = new Color(30, 30, 30);
    private static final Color COLOR_GOLD = new Color(255, 200, 0);
    private static final Color COLOR_TEXT = new Color(220, 220, 220);
    private static final Color COLOR_CLOSE_DEFAULT = new Color(80, 80, 80);
    private static final Color COLOR_CLOSE_HOVER = new Color(220, 53, 69);
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_CONTENT = new Font("Arial", Font.PLAIN, 16);
    private static final Border DIALOG_BORDER = BorderFactory.createLineBorder(COLOR_GOLD, 3);

    public AboutDialog(JFrame parent) {
        super(parent, "About the Creator", true);
        setUndecorated(true);
        setSize(500, 650);
        setLocationRelativeTo(parent);

        // --- Main Panel Setup ---
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(DIALOG_BORDER);

        // --- Build and Add Components ---
        mainPanel.add(createTitleBar(), BorderLayout.NORTH);
        mainPanel.add(createImagePanel(), BorderLayout.CENTER);
        mainPanel.add(createContentPanel(), BorderLayout.SOUTH);

        add(mainPanel);
    }

    /**
     * Creates the top title bar with a title and a custom close button.
     */
    private JPanel createTitleBar() {
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setOpaque(false);
        titleBarPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));

        JLabel titleLabel = new JLabel("About the Creator: Jay Pawar");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_GOLD);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = createCloseButton();
        titleBarPanel.add(closeButton, BorderLayout.EAST);

        return titleBarPanel;
    }

    /**
     * Creates a modern, borderless close button with a hover effect.
     */
    private JButton createCloseButton() {
        JButton closeButton = new JButton("X");
        closeButton.setFont(new Font("Arial", Font.BOLD, 16));
        closeButton.setBackground(COLOR_CLOSE_DEFAULT);
        closeButton.setForeground(Color.WHITE);
        closeButton.setFocusPainted(false);
        closeButton.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        closeButton.addActionListener(e -> dispose());
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setBackground(COLOR_CLOSE_HOVER);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setBackground(COLOR_CLOSE_DEFAULT);
            }
        });
        return closeButton;
    }

    // --- THIS IS THE CORRECTED METHOD ---
    private JPanel createImagePanel() {
        JPanel imageContainerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imageContainerPanel.setOpaque(false);

        JLabel imageLabel = new JLabel();
        try {
            // Load the image as a resource from the classpath
            // The path starts with "/" to signify the root of the resources folder
            URL imageUrl = getClass().getResource("/media/about.jpg");

            if (imageUrl != null) {
                ImageIcon originalIcon = new ImageIcon(imageUrl);
                Image scaledImage = originalIcon.getImage().getScaledInstance(300, 300, Image.SCALE_SMOOTH);
                imageLabel.setIcon(new ImageIcon(scaledImage));
            } else {
                imageLabel.setText("Image not found in resources: /media/about.jpg");
                imageLabel.setForeground(Color.RED);
            }
        } catch (Exception e) {
            imageLabel.setText("Error loading image");
            imageLabel.setForeground(Color.RED);
            e.printStackTrace();
        }
        imageContainerPanel.add(imageLabel);
        return imageContainerPanel;
    }

    /**
     * Creates the text area with the descriptive content.
     */
    // --- MODIFIED: This method now returns the JTextArea directly ---
    private JTextArea createContentPanel() {
        JTextArea infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setForeground(COLOR_TEXT);
        infoArea.setFont(FONT_CONTENT);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        infoArea.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20)); // Adjusted padding
        infoArea.setText(
            "Welcome to the Court Booking Management System!\n\n" +
            "This software was meticulously crafted by Jay Pawar, a visionary developer passionate about creating intuitive and efficient solutions.\n\n" +
            "His dedication to excellence and keen eye for detail are evident in every feature of this application, transforming intricate requirements into elegant, functional software.\n\n" +
            "With this system, Jay aims to empower sports facilities with a robust tool that simplifies operations, minimizes errors, and enhances overall productivity."
        );
        return infoArea;
    }
}