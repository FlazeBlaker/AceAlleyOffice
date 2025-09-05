// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/HowToUseDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

public class HowToUseDialog extends JDialog {

    // --- Styling Constants ---
    private static final Color COLOR_BACKGROUND = new Color(30, 30, 30);
    private static final Color COLOR_PANEL = new Color(45, 45, 45);
    private static final Color COLOR_HEADER = new Color(100, 180, 255);
    private static final Color COLOR_CATEGORY_TITLE = new Color(255, 200, 0);
    private static final Color COLOR_TEXT = new Color(220, 220, 220);
    private static final Color COLOR_CLOSE_DEFAULT = new Color(80, 80, 80);
    private static final Color COLOR_CLOSE_HOVER = new Color(220, 53, 69);
    private static final Font FONT_TITLE = new Font("Arial", Font.BOLD, 24);
    private static final Font FONT_CATEGORY_TITLE = new Font("Arial", Font.BOLD, 20);
    private static final Font FONT_FEATURE = new Font("Arial", Font.BOLD, 16);
    private static final Font FONT_DESCRIPTION = new Font("Arial", Font.PLAIN, 14);
    private static final Border DIALOG_BORDER = BorderFactory.createLineBorder(COLOR_HEADER, 2);

    public HowToUseDialog(JFrame parent) {
        super(parent, "How to Use the Software", true);
        setUndecorated(true);
        
        // --- THIS IS THE FIX ---
        // Increased the height from 750 to 950 to fit all content.
        setSize(850, 950);
        
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(DIALOG_BORDER);

        mainPanel.add(createTitleBar(), BorderLayout.NORTH);
        mainPanel.add(createContentScrollPane(), BorderLayout.CENTER);

        add(mainPanel);
    }

    private JPanel createTitleBar() {
        JPanel titleBarPanel = new JPanel(new BorderLayout());
        titleBarPanel.setOpaque(false);
        titleBarPanel.setBorder(new EmptyBorder(5, 10, 5, 5));

        JLabel titleLabel = new JLabel("How to Use Ace Alley Office");
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(COLOR_HEADER);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleBarPanel.add(titleLabel, BorderLayout.CENTER);

        JButton closeButton = createCloseButton();
        titleBarPanel.add(closeButton, BorderLayout.EAST);

        return titleBarPanel;
    }

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
            public void mouseEntered(MouseEvent e) { closeButton.setBackground(COLOR_CLOSE_HOVER); }
            @Override
            public void mouseExited(MouseEvent e) { closeButton.setBackground(COLOR_CLOSE_DEFAULT); }
        });
        return closeButton;
    }

    private JScrollPane createContentScrollPane() {
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(COLOR_PANEL);
        contentPanel.setBorder(new EmptyBorder(15, 20, 15, 20));

        // --- Booking Category ---
        JPanel bookingCategory = createCategoryPanel("📅 Booking");
        addFeature(bookingCategory, "Switching Views:", "Use the 'Change View' button to toggle between the detailed 'Classic View' and the modern 'Grid View'.");
        addFeature(bookingCategory, "Single Hour Booking:", "Click any available slot. The booking dialog will appear. For regular bookings in an open hour, you can choose a 30-min or full-hour slot.");
        addFeature(bookingCategory, "Block Booking (Grid View):", "In the Grid View, click and drag across empty slots to select a multi-court, multi-hour block. Release to book the entire selection at once.");
        addFeature(bookingCategory, "Adding a Second Booking:", "If a slot has one 30-min booking, click the '+' button on the court panel to book the other available 30-min slot.");
        contentPanel.add(bookingCategory);
        contentPanel.add(Box.createVerticalStrut(20));

        // --- Management Category ---
        JPanel managementCategory = createCategoryPanel("⚙️ Management");
        addFeature(managementCategory, "Viewing & Editing Details:", "Click a booked slot to load its details in the Info Panel on the right. You can edit details and click 'Save Changes'.");
        addFeature(managementCategory, "Memberships:", "Click the 'Memberships' button. You can add new members or search existing ones. For members with zero hours, staff can add a new package.");
        addFeature(managementCategory, "Sales (Cafe & Gear):", "Use the 'Cafe Sale' and 'Gear' buttons to open dedicated dialogs for recording sales and managing inventory.");
        addFeature(managementCategory, "Searching:", "Select a date, then type a name or phone number in the top search bar and click 'Search' to find all matching bookings for that day.");
        addFeature(managementCategory, "Reports (Registry View):", "Click 'Registry View' for a detailed, table-based summary of all financial transactions and bookings for the selected date.");
        contentPanel.add(managementCategory);
        contentPanel.add(Box.createVerticalStrut(20));

        // --- Advanced Actions Category ---
        JPanel advancedCategory = createCategoryPanel("✨ Advanced Actions");
        addFeature(advancedCategory, "Did Not Show (DNS):", "If a customer doesn't show up, select their booking in the Info Panel and check the 'Did Not Show' box. This marks the booking as complete but allows another booking to be added to the same slot.");
        addFeature(advancedCategory, "Extend Booking:", "Select a booking and click 'Extend Booking' in the Info Panel. You can extend by 30 or 60 minutes if the next slot is available.");
        addFeature(advancedCategory, "Shift Booking:", "Select a booking and click 'Start Shift'. The UI will enter Shift Mode. Then, use the grid view to drag and select a new, available time slot for the booking.");
        addFeature(advancedCategory, "Cancel Booking:", "Select any booking and click the 'Cancel Booking' button in the Info Panel (or the 'X' button in the Classic View) to remove it.");
        contentPanel.add(advancedCategory);

        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        scrollPane.setBorder(null);
        return scrollPane;
    }

    private JPanel createCategoryPanel(String title) {
        JPanel categoryPanel = new JPanel();
        categoryPanel.setLayout(new BoxLayout(categoryPanel, BoxLayout.Y_AXIS));
        categoryPanel.setBackground(COLOR_PANEL);
        categoryPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(COLOR_CATEGORY_TITLE),
                title,
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                FONT_CATEGORY_TITLE,
                COLOR_CATEGORY_TITLE
        ));
        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return categoryPanel;
    }

    private void addFeature(JPanel parentPanel, String featureTitle, String description) {
        JPanel featurePanel = new JPanel(new BorderLayout(10, 2));
        featurePanel.setOpaque(false);
        featurePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JLabel iconLabel = new JLabel("●");
        iconLabel.setFont(new Font("Arial", Font.BOLD, 16));
        iconLabel.setForeground(COLOR_HEADER);
        featurePanel.add(iconLabel, BorderLayout.WEST);

        JPanel textPanel = new JPanel();
        textPanel.setOpaque(false);
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(featureTitle);
        titleLabel.setFont(FONT_FEATURE);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel descLabel = new JLabel("<html><p style='width:600px;'>" + description + "</p></html>");
        descLabel.setFont(FONT_DESCRIPTION);
        descLabel.setForeground(COLOR_TEXT);
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(3));
        textPanel.add(descLabel);

        featurePanel.add(textPanel, BorderLayout.CENTER);
        parentPanel.add(featurePanel);
    }
}