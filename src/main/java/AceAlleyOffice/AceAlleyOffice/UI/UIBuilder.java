// File: AceAlleyOffice/AceAlleyOffice/UI/UIBuilder.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.toedter.calendar.JCalendar;
import com.toedter.calendar.JDateChooser;
import com.toedter.calendar.JTextFieldDateEditor;

import AceAlleyOffice.AceAlleyOffice.Core.VenueConfig;
import AceAlleyOffice.AceAlleyOffice.Interfaces.CourtBookingListener;
import AceAlleyOffice.AceAlleyOffice.Main.Main;

public class UIBuilder {

	private final JFrame mainFrame;
	private final CourtBookingListener courtBookingListener;
	// Panels
	private JPanel leftPanel, rightPanel;
	private JPanel viewContainer;
	private CardLayout viewCardLayout;
	private JLabel[] gridTimeLabels;
	// Components
	private JDateChooser dateChooser;
	private JList<String> timeSlotsList;
	private JTextField searchField;
	private JButton searchButton, aboutButton, howToUseButton, cafeSaleButton, outgoingButton, membershipButton,
			gearButton, registryViewButton, saveChangesButton, changeViewButton, cancelBookingButton, startShiftButton,
			bulkBookingButton, extendBookingButton;
	private GridSlot[][] bookingSlots;
	// Info Panel Components
	private JTextField infoCourtNumberField, infoBookingDateField, infoStartTimeField, infoEndTimeField;
	private JTextField bookerNameField, bookerContactField, bookerEmailField, priceField;
	private JRadioButton regularRadioButton, playoRadioButton, khelomoreRadioButton;
	private JCheckBox discountCheckbox, cashCheckbox, upiCheckbox, normalDnsCheckbox, overlayDnsCheckbox;
	private JTextField discountAmountField, cashAmountField, upiAmountField;
	private JLabel racketCountDisplay, ballCountDisplay;
	private JButton increaseRacketsButton, decreaseRacketsButton, increaseBallsButton, decreaseBallsButton;
	private JComboBox<String> cafeItemSelectionBox;
	private JLabel cafeItemQuantityDisplay;
	private JButton increaseCafeItemButton, decreaseCafeItemButton;
	private JTextArea cafeItemsSummaryArea;
	private ButtonGroup platformButtonGroup;
	private JLabel currentTimeLabel;
	private final Font labelFont = new Font("Roboto", Font.BOLD, 14);
	private final Font fieldFont = new Font("Roboto", Font.PLAIN, 14);
	private final Border fieldBorder = BorderFactory.createCompoundBorder(new LineBorder(new Color(100, 100, 100), 1),
			new EmptyBorder(5, 10, 5, 10));
	private JPanel overlayPanel; // ADD this field for the new top layer
	private JPanel platformPanel;
	private Image logo;
	private JLabel connectivityStatusLabel;
	// In UIBuilder.java
	private JScrollPane timeSlotsScrollPane;
	// You need to add this new field at the top of the class:
	private List<Court> courts;
	private JPanel rightCardPanel;
	private CardLayout rightCardLayout;
	private JLabel dnsTitleLabel, dnsBookerNameLabel, dnsContactLabel, dnsDateTimeLabel, dnsPriceLabel;
	private final Color DNS_COLOR = new Color(66, 135, 245);
	private final Main mainApp; // <-- Add this field to access Main's methods

	public UIBuilder(Main mainFrame, VenueConfig config, CourtBookingListener courtBookingListener, String[] timeSlots,
			String[] cafeteriaItems) {
		this.mainApp = mainFrame; // <-- Store the reference to Main
		this.mainFrame = mainFrame;
		this.courtBookingListener = courtBookingListener;
		// --- NEW: Load the logo image when the UI is built ---
		try {
			URL logoUrl = getClass().getResource("/media/AceAlleyLogoCircle.png");
			if (logoUrl != null) {
				this.logo = new ImageIcon(logoUrl).getImage();
			}
		} catch (Exception e) {
			System.err.println("Main UI logo not found!");
			e.printStackTrace();
		}
		timeSlotsList = new JList<>(timeSlots);
		buildPanels();
		buildLeftPanel(config, timeSlots);
		buildRightPanel(cafeteriaItems);
		startClock();
	}

	// --- ADD a getter for the new overlay panel ---
	public JPanel getOverlayPanel() {
		return overlayPanel;
	}

	// --- ADD A GETTER for the new panel ---
	public JPanel getPlatformPanel() {
		return platformPanel;
	}

	// --- THIS METHOD IS NOW CHANGED ---
	private void buildPanels() {
		leftPanel = new JPanel();
		leftPanel.setBackground(new Color(45, 45, 45));
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		leftPanel.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height));
		rightPanel = new JPanel(new BorderLayout());
		rightPanel.setBackground(new Color(30, 30, 30));
		mainFrame.add(leftPanel, BorderLayout.WEST);
		mainFrame.add(rightPanel, BorderLayout.CENTER);
	}

	// --- THIS METHOD HAS BEEN MODIFIED ---
	private void buildLeftPanel(VenueConfig config, String[] timeSlots) { // <-- Accepts config
		leftPanel.setLayout(new BorderLayout());

		JPanel topControlsPanel = new JPanel(null);
		topControlsPanel.setPreferredSize(new Dimension(0, 180));
		topControlsPanel.setBackground(new Color(45, 45, 45));

		// Get the screen size once at the top
		Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
		int leftPanelWidth = screenSize.width / 2;

		if (logo != null) {
			JLabel logoLabel = new JLabel();
			logoLabel.setIcon(new ImageIcon(logo.getScaledInstance(40, 40, Image.SCALE_SMOOTH)));
			logoLabel.setBounds(20, 15, 40, 40);
			topControlsPanel.add(logoLabel);
		}

		JLabel titleLabel = new JLabel("Ace Alley Court Manager");
		titleLabel.setFont(new Font("Tahoma", Font.BOLD, 20));
		titleLabel.setForeground(new Color(255, 200, 0));
		titleLabel.setBounds(70, 20, 550, 30);
		topControlsPanel.add(titleLabel);

		// --- FIX: The clock now uses the correct width for positioning ---
		currentTimeLabel = new JLabel();
		currentTimeLabel.setFont(new Font("Arial", Font.BOLD, 16));
		currentTimeLabel.setForeground(Color.WHITE);
		currentTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		currentTimeLabel.setBounds(leftPanelWidth - 170, 20, 150, 30);
		topControlsPanel.add(currentTimeLabel);

		// Position the connectivity label relative to the clock
		connectivityStatusLabel = new JLabel();
		connectivityStatusLabel.setFont(new Font("Arial", Font.BOLD, 16));
		connectivityStatusLabel.setForeground(Color.CYAN);
		connectivityStatusLabel.setBounds(leftPanelWidth - 200, 20, 30, 30);
		topControlsPanel.add(connectivityStatusLabel);

		// The rest of the buttons and components remain the same
		dateChooser = new JDateChooser();
		dateChooser.setDateFormatString("dd-MM-yyyy");
		UIBuilder.styleDateChooser(dateChooser, fieldBorder);

		dateChooser.setBounds(20, 60, 150, 30);
		topControlsPanel.add(dateChooser);

		searchField = new JTextField("Search Contact or Name...");
		searchField.setBackground(new Color(50, 50, 50));
		searchField.setForeground(Color.WHITE);
		searchField.setBorder(fieldBorder);
		searchField.setBounds(200, 60, 200, 30);
		topControlsPanel.add(searchField);

		// --- THIS IS THE CORRECTED LINE ---
		searchButton = new ModernButton("Search", new Color(0, 123, 255));
		searchButton.setBounds(410, 60, 100, 30); // Adjusted width
		topControlsPanel.add(searchButton);

		aboutButton = createLeftPanelButton("About", 20, 100, new Color(100, 149, 237));
		topControlsPanel.add(aboutButton);
		howToUseButton = createLeftPanelButton("How to Use", 20, 140, new Color(100, 149, 237));
		topControlsPanel.add(howToUseButton);
		cafeSaleButton = createLeftPanelButton("Cafe Sale", 180, 100, new Color(255, 165, 0));
		topControlsPanel.add(cafeSaleButton);
		outgoingButton = createLeftPanelButton("Outgoing", 180, 140, new Color(220, 53, 69));
		topControlsPanel.add(outgoingButton);
		membershipButton = createLeftPanelButton("Memberships", 340, 100, new Color(23, 162, 184));
		topControlsPanel.add(membershipButton);
		gearButton = createLeftPanelButton("Gear", 340, 140, new Color(108, 117, 125));
		topControlsPanel.add(gearButton);
		registryViewButton = createLeftPanelButton("Registry View", 500, 100, new Color(255, 193, 7));
		registryViewButton.setForeground(Color.BLACK);
		topControlsPanel.add(registryViewButton);
		changeViewButton = createLeftPanelButton("Change View", 660, 100, new Color(110, 110, 110));
		topControlsPanel.add(changeViewButton);
		bulkBookingButton = createLeftPanelButton("Bulk Booking", 500, 140, new Color(28, 184, 92));
		topControlsPanel.add(bulkBookingButton);
		leftPanel.add(topControlsPanel, BorderLayout.NORTH);

		// This section builds the view container
		viewCardLayout = new CardLayout();
		viewContainer = new JPanel(viewCardLayout);
		JPanel classicView = buildClassicViewPanel(config);
		JPanel gridView = buildGridViewPanel(timeSlots, config);
		viewContainer.add(classicView, "CLASSIC");
		viewContainer.add(gridView, "GRID");

		// This section correctly adds the time slots list back to the UI
		timeSlotsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		timeSlotsList.setBackground(new Color(60, 60, 60));
		timeSlotsList.setForeground(Color.WHITE);// AFTER (Correct - assigns to the class field)
		timeSlotsList.setCellRenderer(new CurrentTimeListCellRenderer());
		this.timeSlotsScrollPane = new JScrollPane(timeSlotsList);
		timeSlotsScrollPane.setBorder(BorderFactory.createTitledBorder("Time Slots"));
		timeSlotsScrollPane.setPreferredSize(new Dimension(150, 0));
		JPanel centerPanel = new JPanel(new BorderLayout(15, 0));
		centerPanel.setOpaque(false);
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		centerPanel.add(timeSlotsScrollPane, BorderLayout.WEST);
		centerPanel.add(viewContainer, BorderLayout.CENTER);

		leftPanel.add(centerPanel, BorderLayout.CENTER);
	}

	public JLabel getConnectivityStatusLabel() {
		return connectivityStatusLabel;
	}

	// In UIBuilder.java, replace your buildClassicViewPanel method
	public JScrollPane getTimeSlotsScrollPane() {
		return timeSlotsScrollPane;
	}

	// It now takes the VenueConfig as a parameter
	// In UIBuilder.java

	private JPanel buildClassicViewPanel(VenueConfig config) {
		int numCourts = config.getNumberOfCourts();
		int rows = (numCourts <= 2) ? 1 : 2;
		int cols = (int) Math.ceil(numCourts / (double) rows);

		// 1. Create the panel with a GridLayout to arrange the courts
		JPanel courtGridPanel = new JPanel(new GridLayout(rows, cols, 20, 20));
		courtGridPanel.setOpaque(false); // Make it transparent

		// 2. Create the courts in a loop (this part is the same)
		this.courts = new ArrayList<>();
		for (int i = 1; i <= numCourts; i++) {
			Court court = new Court(i, courtBookingListener);
			this.courts.add(court);
			courtGridPanel.add(court);
		}

		// --- THIS IS THE FIX ---
		// 3. Create a wrapper panel that uses FlowLayout.
		// FlowLayout will center the grid and respect its preferred height.
		JPanel wrapperPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 20)); // Centered with a 20px top margin
		wrapperPanel.setBackground(new Color(45, 45, 45));
		wrapperPanel.add(courtGridPanel); // Add our grid of courts to the wrapper.

		// 4. Return the wrapper panel instead of the grid panel directly
		return wrapperPanel;
	}

	// --- NEW: Add this method to run the clock ---
	private void startClock() {
		Timer timer = new Timer(1000, e -> {
			// Format the current time
			SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
			// Update the label's text
			currentTimeLabel.setText(sdf.format(new Date()));
		});
		timer.setInitialDelay(0); // Start immediately
		timer.start();
	}

	// --- ADDED: Getters for the main layout panels ---
	public JPanel getLeftPanel() {
		return leftPanel;
	}

	public JPanel getRightPanel() {
		return rightPanel;
	}

	// In UIBuilder.java

	// In UIBuilder.java

	private JPanel buildGridViewPanel(String[] timeSlots, VenueConfig config) {
		JPanel panel = new JPanel(new BorderLayout());
		JPanel gridPanel = new JPanel(new GridBagLayout());
		gridPanel.setBackground(new Color(50, 50, 50));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 0, 0, 0);
		gbc.fill = GridBagConstraints.BOTH;

		// --- Build Header Row ---
		gbc.gridy = 0;
		gbc.gridx = 0;
		gbc.weightx = 0.4;
		gridPanel.add(createHeaderLabel("Time"), gbc);
		gbc.weightx = 1.0;
		for (int j = 0; j < config.getNumberOfCourts(); j++) {
			gbc.gridx = j + 1;
			gridPanel.add(createHeaderLabel("Court " + (j + 1)), gbc);
		}

		// --- Initialize Arrays ---
		bookingSlots = new GridSlot[timeSlots.length][config.getNumberOfCourts()];
		gridTimeLabels = new JLabel[timeSlots.length];

		// --- Build Grid Rows ---
		for (int i = 0; i < timeSlots.length; i++) {
			gbc.gridy = i + 1;

			// Add time label for the row
			gbc.gridx = 0;
			gbc.weightx = 0.4;
			JLabel timeLabel = new JLabel(timeSlots[i], SwingConstants.CENTER);
			timeLabel.setForeground(Color.LIGHT_GRAY);
			timeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
			timeLabel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(70, 70, 70)));
			gridTimeLabels[i] = timeLabel;
			gridPanel.add(timeLabel, gbc);

			// Add grid slots for the row
			gbc.weightx = 1.0;
			for (int j = 0; j < config.getNumberOfCourts(); j++) {

				// --- THIS IS THE FIX: Cast the listener to (Main) ---
				GridSlot slotPanel = new GridSlot((Main) courtBookingListener, i, j);

				bookingSlots[i][j] = slotPanel;
				gbc.gridx = j + 1;
				gbc.weighty = 1.0;
				gridPanel.add(slotPanel, gbc);
			}
		}

		JScrollPane scrollPane = new JScrollPane(gridPanel);
		scrollPane.setBorder(null);
		panel.add(scrollPane, BorderLayout.CENTER);
		return panel;
	}

	public JLabel[] getGridTimeLabels() {
		return gridTimeLabels;
	}

	// --- THIS IS THE CORRECTED METHOD ---
	public static void styleDateChooser(JDateChooser dateChooser, Border fieldBorder) {
		JTextFieldDateEditor editor = (JTextFieldDateEditor) dateChooser.getDateEditor();
		JCalendar jcal = dateChooser.getJCalendar();

		// Define colors
		Color darkBackground = new Color(45, 45, 45);
		Color mediumBackground = new Color(60, 60, 60);
		Color weekdayColor = new Color(150, 200, 255);
        Color todayColor = new Color(230, 126, 34);

		// Style the main text field part
		editor.setBackground(mediumBackground);
		editor.setForeground(Color.WHITE);
		editor.setBorder(fieldBorder);

        // Style the popup calendar directly
        jcal.setBackground(darkBackground);
        
        // Style the main panel with the day numbers
        jcal.getDayChooser().setBackground(darkBackground);
        jcal.getDayChooser().setForeground(Color.WHITE);
        jcal.getDayChooser().setWeekdayForeground(weekdayColor);
        jcal.getDayChooser().setSundayForeground(Color.WHITE);
        jcal.getDayChooser().setDecorationBackgroundColor(todayColor);
        
        // Style the Month and Year choosers at the top
        jcal.getMonthChooser().getComboBox().setBackground(mediumBackground);
        jcal.getMonthChooser().getComboBox().setForeground(Color.WHITE);
        
        JSpinner yearSpinner = (JSpinner) jcal.getYearChooser().getSpinner();
        JComponent yearEditor = yearSpinner.getEditor();
        if (yearEditor instanceof JSpinner.DefaultEditor) {
            JTextField yearTextField = ((JSpinner.DefaultEditor) yearEditor).getTextField();
            yearTextField.setBackground(mediumBackground);
            yearTextField.setForeground(Color.WHITE);
        }

        // --- THIS IS THE FIX ---
        // Find the panel that holds the week numbers and style its labels.
        // This is necessary because there is no direct method to set this color.
        for (Component outerComp : jcal.getDayChooser().getComponents()) {
            if (outerComp instanceof JPanel) {
                JPanel weekPanel = (JPanel) outerComp;
                for (Component innerComp : weekPanel.getComponents()) {
                    if (innerComp instanceof JLabel) {
                        ((JLabel) innerComp).setForeground(Color.WHITE);
                    }
                }
            }
        }
        // --- END OF FIX ---
	}

	private void buildRightPanel(String[] cafeteriaItems) {
		// This panel holds all the regular input fields
		JPanel infoContainerPanel = createInfoContainerPanel(cafeteriaItems);

		// This panel is the new, simplified overlay for DNS bookings
		JPanel dnsInfoPanel = createDnsInfoPanel();

		// This card panel will manage switching between the normal view and the DNS
		// overlay
		rightCardLayout = new CardLayout();
		rightCardPanel = new JPanel(rightCardLayout);
		rightCardPanel.setOpaque(false);

		rightCardPanel.add(infoContainerPanel, "NORMAL");
		rightCardPanel.add(dnsInfoPanel, "DNS");

		rightPanel.add(rightCardPanel, BorderLayout.CENTER);
	}

	private JPanel createInfoContainerPanel(String[] cafeteriaItems) {
		JPanel infoContainerPanel = new JPanel(new GridBagLayout());
		infoContainerPanel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 8, 8, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.anchor = GridBagConstraints.NORTH;
		gbc.gridy = 0;
		infoContainerPanel.add(createBookingInfoPanel(), gbc);
		gbc.gridy = 1;
		infoContainerPanel.add(createCustomerDetailsPanel(), gbc);
		gbc.gridy = 2;
		infoContainerPanel.add(createPricingPanel(), gbc);
		gbc.gridy = 3;
		infoContainerPanel.add(createCafePanel(cafeteriaItems), gbc);
		gbc.gridy = 4;
		infoContainerPanel.add(createRentalsPanel(), gbc);

		saveChangesButton = new ModernButton("Save Changes", new Color(40, 167, 69));
		startShiftButton = new ModernButton("Start Shift", new Color(23, 162, 184));
		cancelBookingButton = new ModernButton("Cancel Booking", new Color(220, 53, 69));
		extendBookingButton = new ModernButton("Extend", new Color(0, 123, 255)); // <-- NEW: Create the button

		saveChangesButton.setEnabled(false);
		startShiftButton.setEnabled(false);
		cancelBookingButton.setEnabled(false);
		extendBookingButton.setEnabled(false); // <-- NEW

		JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
		actionPanel.setOpaque(false);
		actionPanel.add(saveChangesButton);
		actionPanel.add(startShiftButton);
		actionPanel.add(cancelBookingButton);
		actionPanel.add(extendBookingButton); // <-- NEW: Add button to the panel

		gbc.gridy = 5;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		infoContainerPanel.add(actionPanel, gbc);
		return infoContainerPanel;
	}

	public JButton getExtendBookingButton() {
		return extendBookingButton;
	}

	public JButton getStartShiftButton() {
		return startShiftButton;
	}

	private JPanel createBookingInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Booking Information"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		int y = 0;

		normalDnsCheckbox = createStyledCheckbox("DNS (Did Not Show)");
		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(normalDnsCheckbox, gbc);
		gbc.anchor = GridBagConstraints.WEST; // Reset anchor
		gbc.gridwidth = 1;

		infoCourtNumberField = createReadOnlyTextField(15);
		addLabelAndField(panel, "Court Number:", infoCourtNumberField, gbc, y++);
		infoBookingDateField = createReadOnlyTextField(15);
		addLabelAndField(panel, "Booking Date:", infoBookingDateField, gbc, y++);
		infoStartTimeField = createReadOnlyTextField(15);
		addLabelAndField(panel, "Start Time:", infoStartTimeField, gbc, y++);
		infoEndTimeField = createReadOnlyTextField(15);
		addLabelAndField(panel, "End Time:", infoEndTimeField, gbc, y++);
		return panel;
	}

	// --- NEW: This method builds the simplified DNS overlay panel ---
	private JPanel createDnsInfoPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Booking Information"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 8, 10, 8);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridwidth = 2;
		gbc.gridx = 0;
		int y = 0;

		dnsTitleLabel = new JLabel("DID NOT SHOW", SwingConstants.CENTER);
		dnsTitleLabel.setFont(new Font("Roboto", Font.BOLD, 24));
		dnsTitleLabel.setForeground(DNS_COLOR);
		gbc.gridy = y++;
		panel.add(dnsTitleLabel, gbc);
		overlayDnsCheckbox = createStyledCheckbox("DNS (Did Not Show)");
		gbc.gridx = 1;
		gbc.anchor = GridBagConstraints.EAST;
		panel.add(overlayDnsCheckbox, gbc);

		gbc.gridwidth = 2; // Reset for other components
		gbc.gridx = 0;
		gbc.anchor = GridBagConstraints.CENTER;
		y++;
		gbc.gridy = y++;
		panel.add(Box.createRigidArea(new Dimension(0, 20)), gbc);

		dnsBookerNameLabel = createDnsInfoLabel();
		addDnsLabelAndField(panel, "Booker:", dnsBookerNameLabel, gbc, y++);
		dnsContactLabel = createDnsInfoLabel();
		addDnsLabelAndField(panel, "Contact:", dnsContactLabel, gbc, y++);
		dnsDateTimeLabel = createDnsInfoLabel();
		addDnsLabelAndField(panel, "Slot:", dnsDateTimeLabel, gbc, y++);
		dnsPriceLabel = createDnsInfoLabel();
		addDnsLabelAndField(panel, "Amount Paid:", dnsPriceLabel, gbc, y++);

		return panel;
	}

	private JLabel createDnsInfoLabel() {
		JLabel label = new JLabel();
		label.setFont(new Font("Roboto", Font.PLAIN, 16));
		label.setForeground(Color.WHITE);
		return label;
	}

	private void addDnsLabelAndField(JPanel p, String labelText, JLabel fieldLabel, GridBagConstraints g, int y) {
		g.gridwidth = 1;
		g.gridx = 0;
		g.gridy = y;
		g.weightx = 0;
		g.anchor = GridBagConstraints.EAST;
		JLabel title = new JLabel(labelText);
		title.setFont(new Font("Roboto", Font.BOLD, 16));
		title.setForeground(new Color(200, 200, 200));
		p.add(title, g);
		g.gridx = 1;
		g.weightx = 1.0;
		g.anchor = GridBagConstraints.WEST;
		p.add(fieldLabel, g);
	}

	// --- NEW: Getters for all the new DNS components ---
	public JCheckBox getNormalDnsCheckbox() {
		return normalDnsCheckbox;
	}

	public JCheckBox getOverlayDnsCheckbox() {
		return overlayDnsCheckbox;
	}

	public JPanel getRightCardPanel() {
		return rightCardPanel;
	}

	public CardLayout getRightCardLayout() {
		return rightCardLayout;
	}

	public JLabel getDnsBookerNameLabel() {
		return dnsBookerNameLabel;
	}

	public JLabel getDnsContactLabel() {
		return dnsContactLabel;
	}

	public JLabel getDnsDateTimeLabel() {
		return dnsDateTimeLabel;
	}

	public JLabel getDnsPriceLabel() {
		return dnsPriceLabel;
	}

	private JPanel createCustomerDetailsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Customer Details"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		int y = 0;
		bookerNameField = createEditableTextField(15);
		addLabelAndField(panel, "Booker Name:", bookerNameField, gbc, y++);
		bookerContactField = createEditableTextField(15);
		addLabelAndField(panel, "Phone Number:", bookerContactField, gbc, y++);
		bookerEmailField = createEditableTextField(15);
		addLabelAndField(panel, "Email (Optional):", bookerEmailField, gbc, y++);
		return panel;
	}

	private JPanel createPricingPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Pricing & Discounts"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		int y = 0;

		regularRadioButton = new JRadioButton("Regular");
		styleStyledRadioButton(regularRadioButton);
		playoRadioButton = new JRadioButton("Playo");
		styleStyledRadioButton(playoRadioButton);
		khelomoreRadioButton = new JRadioButton("Khelomore");
		styleStyledRadioButton(khelomoreRadioButton);

		platformButtonGroup = new ButtonGroup();
		platformButtonGroup.add(regularRadioButton);
		platformButtonGroup.add(playoRadioButton);
		platformButtonGroup.add(khelomoreRadioButton);

		// --- MODIFIED: The panel is now assigned to the class field ---
		platformPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		platformPanel.setOpaque(false);
		platformPanel.add(regularRadioButton);
		platformPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		platformPanel.add(playoRadioButton);
		platformPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		platformPanel.add(khelomoreRadioButton);

		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		panel.add(platformPanel, gbc);
		gbc.gridwidth = 1;

		discountCheckbox = createStyledCheckbox("Discount:");
		discountAmountField = createEditableTextField(10);
		addCheckboxAndField(panel, discountCheckbox, discountAmountField, gbc, y++);

		cashCheckbox = createStyledCheckbox("Cash");
		cashAmountField = createEditableTextField(15);
		addCheckboxAndField(panel, cashCheckbox, cashAmountField, gbc, y++);

		upiCheckbox = createStyledCheckbox("UPI");
		upiAmountField = createEditableTextField(15);
		addCheckboxAndField(panel, upiCheckbox, upiAmountField, gbc, y++);

		priceField = createReadOnlyTextField(15);
		addLabelAndField(panel, "Price:", priceField, gbc, y++);

		return panel;
	}

	private JPanel createCafePanel(String[] cafeteriaItems) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Cafe Items"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		int y = 0;
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		panel.add(createStyledLabel("Cafe Item:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		cafeItemSelectionBox = new JComboBox<>(cafeteriaItems);
		cafeItemSelectionBox.setFont(fieldFont);
		panel.add(cafeItemSelectionBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.fill = GridBagConstraints.NONE;
		gbc.weightx = 0;
		panel.add(createStyledLabel("Quantity:"), gbc);
		gbc.gridx = 1;
		gbc.gridy = y++;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		JPanel cafeItemControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		cafeItemControlPanel.setOpaque(false);
		decreaseCafeItemButton = createSmallControlButton("-");
		cafeItemQuantityDisplay = createCountDisplay();
		increaseCafeItemButton = createSmallControlButton("+");
		cafeItemControlPanel.add(decreaseCafeItemButton);
		cafeItemControlPanel.add(cafeItemQuantityDisplay);
		cafeItemControlPanel.add(increaseCafeItemButton);
		panel.add(cafeItemControlPanel, gbc);

		gbc.gridx = 0;
		gbc.gridy = y++;
		gbc.gridwidth = 2;
		gbc.fill = GridBagConstraints.BOTH;
		panel.add(createStyledLabel("Summary:"), gbc);
		gbc.gridy = y++;
		cafeItemsSummaryArea = new JTextArea(3, 15);
		cafeItemsSummaryArea.setFont(fieldFont);
		cafeItemsSummaryArea.setEditable(false);
		panel.add(new JScrollPane(cafeItemsSummaryArea), gbc);

		return panel;
	}

	private JPanel createRentalsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		panel.setBorder(createTitledBorder("Rentals"));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		int y = 0;
		racketCountDisplay = createCountDisplay();
		decreaseRacketsButton = createSmallControlButton("-");
		increaseRacketsButton = createSmallControlButton("+");
		addControlPanel(panel, createStyledLabel("Rackets:"), decreaseRacketsButton, racketCountDisplay,
				increaseRacketsButton, gbc, y++);

		ballCountDisplay = createCountDisplay();
		decreaseBallsButton = createSmallControlButton("-");
		increaseBallsButton = createSmallControlButton("+");
		addControlPanel(panel, createStyledLabel("Balls:"), decreaseBallsButton, ballCountDisplay, increaseBallsButton,
				gbc, y++);

		return panel;
	}

	// --- THIS IS THE FIRST CORRECTED HELPER METHOD ---
	private void addLabelAndField(JPanel p, String l, JTextField f, GridBagConstraints g, int y) {
		g.gridx = 0;
		g.gridy = y;
		g.weightx = 0; // The label should not stretch
		g.fill = GridBagConstraints.NONE;
		p.add(createStyledLabel(l), g);

		g.gridx = 1;
		g.weightx = 1.0; // The text field will take up the remaining horizontal space
		g.fill = GridBagConstraints.HORIZONTAL;
		p.add(f, g);
	}

	// --- THIS IS THE SECOND CORRECTED HELPER METHOD ---
	private void addCheckboxAndField(JPanel p, JCheckBox c, JTextField f, GridBagConstraints g, int y) {
		g.gridx = 0;
		g.gridy = y;
		g.weightx = 0; // The checkbox should not stretch
		g.fill = GridBagConstraints.NONE;
		p.add(c, g);

		g.gridx = 1;
		g.weightx = 1.0; // The text field will take up the remaining horizontal space
		g.fill = GridBagConstraints.HORIZONTAL;
		p.add(f, g);
	}

	private void addControlPanel(JPanel p, JLabel l, JButton d, JLabel c, JButton i, GridBagConstraints g, int y) {
		g.gridx = 0;
		g.gridy = y;
		p.add(l, g);
		JPanel cp = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		cp.setOpaque(false);
		cp.add(d);
		cp.add(c);
		cp.add(i);
		g.gridx = 1;
		p.add(cp, g);
	}

	private Border createTitledBorder(String t) {
		TitledBorder b = BorderFactory.createTitledBorder(new LineBorder(new Color(150, 150, 150)), t);
		b.setTitleFont(new Font("Arial", Font.BOLD, 18));
		b.setTitleColor(new Color(255, 200, 0));
		return new CompoundBorder(b, new EmptyBorder(10, 10, 10, 10));
	}

	private JTextField createReadOnlyTextField(int c) {
		JTextField f = new JTextField(c);
		f.setFont(fieldFont);
		f.setEditable(false);
		f.setBackground(new Color(50, 50, 50));
		f.setForeground(Color.WHITE);
		f.setBorder(fieldBorder);
		return f;
	}

	private JTextField createEditableTextField(int c) {
		JTextField f = new JTextField(c);
		f.setFont(fieldFont);
		f.setBackground(new Color(50, 50, 50));
		f.setForeground(Color.WHITE);
		f.setBorder(fieldBorder);
		return f;
	}

	private JLabel createStyledLabel(String t) {
		JLabel l = new JLabel(t);
		l.setFont(labelFont);
		l.setForeground(new Color(200, 200, 200));
		return l;
	}

	private JCheckBox createStyledCheckbox(String t) {
		JCheckBox c = new JCheckBox(t);
		c.setFont(labelFont);
		c.setForeground(new Color(200, 200, 200));
		c.setOpaque(false);
		return c;
	}

	// --- ADD THIS NEW INNER CLASS AT THE BOTTOM OF UIBuilder.java ---
	/**
	 * A custom JButton that paints itself with rounded corners.
	 */
	private static class RoundButton extends JButton {
		private Shape shape;
		private final Color hoverBackgroundColor = new Color(100, 100, 100);
		private final Color pressedBackgroundColor = new Color(120, 120, 120);

		public RoundButton(String text) {
			super(text);
			// This is crucial! It allows us to paint our own background.
			setContentAreaFilled(false);
		}

		@Override
		protected void paintComponent(Graphics g) {
			Graphics2D g2 = (Graphics2D) g.create();
			// Enable anti-aliasing for smooth curves
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

			// Set the color based on the button's state (pressed, hover, or normal)
			if (getModel().isPressed()) {
				g2.setColor(pressedBackgroundColor);
			} else if (getModel().isRollover()) {
				g2.setColor(hoverBackgroundColor);
			} else {
				g2.setColor(getBackground());
			}

			// Create the rounded rectangle shape. The last two numbers (15, 15) control the
			// roundness.
			shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
			g2.fill(shape);

			g2.dispose();

			// Let the parent JButton class paint the text ("+" or "-") on top of our shape
			super.paintComponent(g);
		}

		@Override
		protected void paintBorder(Graphics g) {
			// We paint our own background, so we don't need a separate border.
		}

		@Override
		public boolean contains(int x, int y) {
			// This makes sure that clicks in the corners (outside the rounded shape) are
			// ignored.
			if (shape == null || !shape.getBounds().equals(getBounds())) {
				shape = new RoundRectangle2D.Float(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
			}
			return shape.contains(x, y);
		}
	}

	// --- THIS METHOD IS REPLACED to use the new RoundButton ---
	private JButton createSmallControlButton(String text) {
		JButton button = new RoundButton(text); // Create an instance of our new custom class
		button.setBackground(new Color(80, 80, 80));
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setFont(new Font("Arial", Font.BOLD, 16));
		return button;
	}

	private JLabel createCountDisplay() {
		JLabel l = new JLabel("0");
		l.setFont(fieldFont);
		l.setForeground(Color.WHITE);
		l.setPreferredSize(new Dimension(40, 25));
		l.setHorizontalAlignment(JLabel.CENTER);
		return l;
	}

	// --- THIS IS THE REPLACED METHOD ---
	private JButton createLeftPanelButton(String text, int x, int y, Color color) {
		// It now creates an instance of our ModernButton class to get the curved style
		JButton button = new ModernButton(text, color);
		button.setBounds(x, y, 150, 30);
		return button;
	}

	private void styleStyledRadioButton(JRadioButton r) {
		r.setFont(labelFont);
		r.setForeground(new Color(200, 200, 200));
		r.setOpaque(false);
	}

	private JLabel createHeaderLabel(String text) {
		JLabel label = new JLabel(text, SwingConstants.CENTER);
		label.setFont(labelFont);
		label.setForeground(Color.WHITE);
		label.setOpaque(true);
		label.setBackground(new Color(30, 30, 30));
		label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, new Color(90, 90, 90)));
		return label;
	}

	public void updateCafeItemChoices(String[] items) {
		cafeItemSelectionBox.setModel(new DefaultComboBoxModel<>(items));
	}

	public JDateChooser getDateChooser() {
		return dateChooser;
	}

	public JList<String> getTimeSlotsList() {
		return timeSlotsList;
	}

	public JTextField getSearchField() {
		return searchField;
	}

	public JButton getSearchButton() {
		return searchButton;
	}

	public JButton getAboutButton() {
		return aboutButton;
	}

	public JButton getHowToUseButton() {
		return howToUseButton;
	}

	public JButton getCafeSaleButton() {
		return cafeSaleButton;
	}

	public JButton getOutgoingButton() {
		return outgoingButton;
	}

	public JButton getMembershipButton() {
		return membershipButton;
	}

	public JButton getGearButton() {
		return gearButton;
	}

	public JButton getRegistryViewButton() {
		return registryViewButton;
	}

	public Court getCourt(int courtNumber) {
		int index = courtNumber - 1;
		if (courts != null && index >= 0 && index < courts.size()) {
			return courts.get(index);
		}
		return null;
	}

	public JTextField getInfoCourtNumberField() {
		return infoCourtNumberField;
	}

	public JTextField getInfoBookingDateField() {
		return infoBookingDateField;
	}

	public JTextField getInfoStartTimeField() {
		return infoStartTimeField;
	}

	public JTextField getInfoEndTimeField() {
		return infoEndTimeField;
	}

	public JTextField getBookerNameField() {
		return bookerNameField;
	}

	public JTextField getBookerContactField() {
		return bookerContactField;
	}

	public JTextField getBookerEmailField() {
		return bookerEmailField;
	}

	public JTextField getPriceField() {
		return priceField;
	}

	public JButton getSaveChangesButton() {
		return saveChangesButton;
	}

	public JLabel getRacketCountDisplay() {
		return racketCountDisplay;
	}

	public JButton getIncreaseRacketsButton() {
		return increaseRacketsButton;
	}

	public JButton getDecreaseRacketsButton() {
		return decreaseRacketsButton;
	}

	public JLabel getBallCountDisplay() {
		return ballCountDisplay;
	}

	public JButton getIncreaseBallsButton() {
		return increaseBallsButton;
	}

	public JButton getDecreaseBallsButton() {
		return decreaseBallsButton;
	}

	public JCheckBox getDiscountCheckbox() {
		return discountCheckbox;
	}

	public JTextField getDiscountAmountField() {
		return discountAmountField;
	}

	public JComboBox<String> getCafeItemSelectionBox() {
		return cafeItemSelectionBox;
	}

	public JLabel getCafeItemQuantityDisplay() {
		return cafeItemQuantityDisplay;
	}

	public JButton getIncreaseCafeItemButton() {
		return increaseCafeItemButton;
	}

	public JButton getDecreaseCafeItemButton() {
		return decreaseCafeItemButton;
	}

	public JTextArea getCafeItemsSummaryArea() {
		return cafeItemsSummaryArea;
	}

	public JRadioButton getPlayoRadioButton() {
		return playoRadioButton;
	}

	public JRadioButton getKhelomoreRadioButton() {
		return khelomoreRadioButton;
	}

	public JCheckBox getCashCheckbox() {
		return cashCheckbox;
	}

	public JTextField getCashAmountField() {
		return cashAmountField;
	}

	public JCheckBox getUpiCheckbox() {
		return upiCheckbox;
	}

	public JTextField getUpiAmountField() {
		return upiAmountField;
	}

	public ButtonGroup getPlatformButtonGroup() {
		return platformButtonGroup;
	}

	public JButton getCancelBookingButton() {
		return cancelBookingButton;
	}

	public JPanel getViewContainer() {
		return viewContainer;
	}

	public CardLayout getViewCardLayout() {
		return viewCardLayout;
	}

	public JButton getChangeViewButton() {
		return changeViewButton;
	}

	public JRadioButton getRegularRadioButton() {
		return regularRadioButton;
	}

	public GridSlot[][] getBookingSlots() {
		return bookingSlots;
	}

	public JButton getBulkBookingButton() {
		return bulkBookingButton;
	}

	private class CurrentTimeListCellRenderer extends DefaultListCellRenderer {
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			// Get the default component (a JLabel)
			Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			// If this cell's time matches the current hour, give it a highlight border
			if (value != null && value.toString().equals(mainApp.getCurrentHourSlot())) {
				((JComponent) c).setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createLineBorder(Color.YELLOW, 2), BorderFactory.createEmptyBorder(2, 2, 2, 2) // Add
																														// padding
				));
			} else {
				// Otherwise, give it normal padding
				((JComponent) c).setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

			}

			// Keep the default selection color
			if (isSelected) {
				Color originalColor = Color.orange;

				// 2. Define the alpha value for transparency (0-255)
				int alpha = 128; // 50% transparent

				// 3. Create the new transparent color
				Color transparentOrange = new Color(originalColor.getRed(), originalColor.getGreen(),
						originalColor.getBlue(), alpha);

				// 4. Apply the new color to your component
				c.setBackground(transparentOrange);
			} else {
				c.setBackground(list.getBackground());
			}

			return c;
		}
	}
}