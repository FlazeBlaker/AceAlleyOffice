// File: AceAlleyOffice/AceAlleyOffice/Main/Main.java
package AceAlleyOffice.AceAlleyOffice.Main;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

import AceAlleyOffice.AceAlleyOffice.Action.ActionListeners;
import AceAlleyOffice.AceAlleyOffice.Core.BookingManager;
import AceAlleyOffice.AceAlleyOffice.Core.ConnectivityManager;
import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.MembershipManager;
import AceAlleyOffice.AceAlleyOffice.Core.User;
import AceAlleyOffice.AceAlleyOffice.Core.VenueConfig;
import AceAlleyOffice.AceAlleyOffice.Core.strategy.DefaultPricing;
import AceAlleyOffice.AceAlleyOffice.Core.strategy.DynamicPricing;
import AceAlleyOffice.AceAlleyOffice.Core.strategy.PricingStrategy;
import AceAlleyOffice.AceAlleyOffice.Interfaces.CourtBookingListener;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.CafeItem;
import AceAlleyOffice.AceAlleyOffice.UI.Court;
import AceAlleyOffice.AceAlleyOffice.UI.GearItem;
import AceAlleyOffice.AceAlleyOffice.UI.GridSlot;
import AceAlleyOffice.AceAlleyOffice.UI.Membership;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPackage;
import AceAlleyOffice.AceAlleyOffice.UI.PricingSettings;
import AceAlleyOffice.AceAlleyOffice.UI.UIBuilder;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.BulkBookingDialog;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.DialogFactory;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.LoadingDialog;
import mdlaf.MaterialLookAndFeel;
import mdlaf.themes.MaterialOceanicTheme;

public class Main extends JFrame implements CourtBookingListener {

	private final String[] timeSlots = { "12:00 AM", "01:00 AM", "02:00 AM", "03:00 AM", "04:00 AM", "05:00 AM",
			"06:00 AM", "07:00 AM", "08:00 AM", "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "01:00 PM", "02:00 PM",
			"03:00 PM", "04:00 PM", "05:00 PM", "06:00 PM", "07:00 PM", "08:00 PM", "09:00 PM", "10:00 PM",
			"11:00 PM" };
	private String[] CAFETERIA_ITEMS = {};
	private String currentHourSlot;
	private Map<String, Double> cafeItemPrices = new HashMap<>();
	private Map<String, Double> gearItemPrices = new HashMap<>();
	private final Map<String, Map<String, Map<Integer, List<BookingDetails>>>> allBookings = new HashMap<>();
	private FirebaseDataManager firebaseDataManager;
	private MembershipManager membershipManager;
	private BookingManager bookingManager;
	private UIBuilder uiBuilder;
	private DialogFactory dialogFactory;
	private boolean isGridView = false;
	private BookingDetails currentInfoBookingDetails;
	private boolean isDragging = false;
	private int dragStartRow, dragStartCol, dragEndRow, dragEndCol;
	private BookingDetails tempBookingDetails;
	private boolean isShiftModeActive = false;
	private BookingDetails bookingBeingShifted = null;
	private List<MembershipPackage> membershipPackages = new ArrayList<>();
	private boolean isBulkBookingModeActive = false;
	private Date bulkBookingStartDate;
	private Date bulkBookingEndDate;
	private PricingSettings pricingSettings = new PricingSettings();
	private JLayeredPane layeredPane;
	private JPanel offlinePanel;
	private ConnectivityManager connectivityManager;
	private Timer connectivityAnimationTimer;
	private int animationFrame = 0;
	private final String[] animationFrames = { "⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏" };
	private final VenueConfig venueConfig;
	private final PricingStrategy pricingStrategy;
	private final User currentUser;
	private final Map<Integer, Integer> courtDisplayIndex = new HashMap<>();
	private Preferences prefs;
	private final Map<String, Integer> gridSlotDisplayIndex = new HashMap<>();

	public Main(CountDownLatch initialDataLatch, FirebaseDataManager dataManager, VenueConfig config, User currentUser,
			String venueIdForSession, PricingSettings settings) throws IOException {

		this.currentUser = currentUser;
		this.firebaseDataManager = dataManager;
		this.venueConfig = config;
		this.pricingSettings = settings;
		this.prefs = Preferences.userNodeForPackage(Main.class);

		if ("VimanNagarDynamic".equals(config.getPricingStrategy())) {
			this.pricingStrategy = new DynamicPricing(getPricingSettings());
		} else {
			this.pricingStrategy = new DefaultPricing(getPricingSettings());
		}

		this.membershipManager = new MembershipManager(venueIdForSession);
		this.bookingManager = new BookingManager(this, this.firebaseDataManager, this.membershipManager,
				venueIdForSession);

		initWindow();
		uiBuilder = new UIBuilder(this, venueConfig, this, timeSlots, CAFETERIA_ITEMS);
		dialogFactory = new DialogFactory(this, this.allBookings, firebaseDataManager);
		new ActionListeners(this, uiBuilder, firebaseDataManager, membershipManager, dialogFactory, bookingManager);

		MouseAdapter gridMouseListener = createGridMouseListener();
		GridSlot[][] slots = uiBuilder.getBookingSlots();
		if (slots != null) {
			for (GridSlot[] slotRow : slots) {
				for (GridSlot slot : slotRow) {
					slot.addMouseListener(gridMouseListener);
					slot.getInfoLabel().addMouseListener(gridMouseListener);
				}
			}
		}

		uiBuilder.getDateChooser().addPropertyChangeListener("date", evt -> {
			loadSavedViewForDate();
			updateActiveView();
		});
		// The time slot listener simply triggers a redraw of the classic view.
		uiBuilder.getTimeSlotsList().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				updateActiveView();
			}
		});

		attachRealtimeListeners(initialDataLatch);
		setupOverlay();
		connectivityManager = new ConnectivityManager(this);
		connectivityManager.start();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (connectivityManager != null) {
					connectivityManager.stop();
				}
				System.exit(0);
			}
		});

		uiBuilder.getDateChooser().setDate(new Date());
		SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:00 a");
		String currentTimeSlot = timeFormatter.format(new Date());
		uiBuilder.getTimeSlotsList().setSelectedValue(currentTimeSlot, true);

		updateCurrentTimeHighlight();
		Timer clockTimer = new Timer(60000, e -> updateCurrentTimeHighlight());
		clockTimer.setInitialDelay(0);
		clockTimer.start();
		loadSavedViewForDate();
	}

	private void loadSavedViewForDate() {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null)
			return;
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);

		for (int row = 0; row < timeSlots.length; row++) {
			for (int col = 0; col < venueConfig.getNumberOfCourts(); col++) {
				int courtNum = col + 1;
				List<BookingDetails> bookings = findBookingsForHour(dateStr, timeSlots[row], courtNum);

				if (!bookings.isEmpty()) {
					int targetIndex = 0;
					String savedBookingId = getLastViewedBooking(dateStr, timeSlots[row], courtNum);
					if (savedBookingId != null) {
						for (int j = 0; j < bookings.size(); j++) {
							if (savedBookingId.equals(bookings.get(j).getDocumentId())) {
								targetIndex = j;
								break;
							}
						}
					}
					// Update both maps
					courtDisplayIndex.put(courtNum, targetIndex);
					gridSlotDisplayIndex.put(row + ":" + col, targetIndex);
				}
			}
		}
	}

	private void loadAndApplySavedViewForCurrentSlot() {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();
		if (selectedDate == null || selectedTime == null)
			return;

		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		int timeIndex = uiBuilder.getTimeSlotsList().getSelectedIndex();

		for (int i = 1; i <= venueConfig.getNumberOfCourts(); i++) {
			List<BookingDetails> bookings = findBookingsForHour(dateStr, selectedTime, i);
			if (!bookings.isEmpty()) {
				int targetIndex = 0;
				String savedBookingId = getLastViewedBooking(dateStr, selectedTime, i);
				if (savedBookingId != null) {
					for (int j = 0; j < bookings.size(); j++) {
						if (savedBookingId.equals(bookings.get(j).getDocumentId())) {
							targetIndex = j;
							break;
						}
					}
				}
				// Update both maps
				courtDisplayIndex.put(i, targetIndex);
				gridSlotDisplayIndex.put(timeIndex + ":" + (i - 1), targetIndex);
			}
		}
	}

	public void showConnectivityCheckIndicator() {
		if (connectivityAnimationTimer == null) {
			connectivityAnimationTimer = new Timer(80, e -> {
				animationFrame = (animationFrame + 1) % animationFrames.length;
				uiBuilder.getConnectivityStatusLabel().setText(animationFrames[animationFrame]);
			});
			connectivityAnimationTimer.setInitialDelay(0);
		}
		if (!connectivityAnimationTimer.isRunning()) {
			connectivityAnimationTimer.start();
		}
	}

	public void hideConnectivityCheckIndicator() {
		if (connectivityAnimationTimer != null && connectivityAnimationTimer.isRunning()) {
			connectivityAnimationTimer.stop();
		}
		uiBuilder.getConnectivityStatusLabel().setText("");
	}

	private void saveLastViewedBooking(String dateStr, String timeStr, int courtNum, String bookingId) {
		if (dateStr == null || timeStr == null || bookingId == null)
			return;
		// Create a unique key for the slot, e.g., "01-09-2025_03:00 PM_1"
		String key = dateStr + "_" + timeStr + "_" + courtNum;
		prefs.put(key, bookingId);
	}

	private String getLastViewedBooking(String dateStr, String timeStr, int courtNum) {
		if (dateStr == null || timeStr == null)
			return null;
		String key = dateStr + "_" + timeStr + "_" + courtNum;
		return prefs.get(key, null); // Return the saved ID, or null if not found
	}

	public void navigateGridSlot(int row, int col, int direction) {
		String key = row + ":" + col;
		int currentIndex = gridSlotDisplayIndex.getOrDefault(key, 0);
		int newIndex = currentIndex + direction;

		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(uiBuilder.getDateChooser().getDate());
		List<BookingDetails> bookings = findBookingsForHour(dateStr, timeSlots[row], col + 1);

		if (newIndex >= 0 && newIndex < bookings.size()) {
			gridSlotDisplayIndex.put(key, newIndex);

			// --- THIS IS THE FIX (Part 2) ---
			// Also update the classic view's index to keep it in sync.
			int courtNumber = col + 1;
			courtDisplayIndex.put(courtNumber, newIndex);
			// --- END OF FIX ---

			updateActiveView();

			BookingDetails bookingToShow = bookings.get(newIndex);
			currentInfoBookingDetails = bookingToShow;
			tempBookingDetails = new BookingDetails(bookingToShow);
			refreshInfoPanelDisplay();
			saveLastViewedBooking(dateStr, timeSlots[row], col + 1, bookingToShow.getDocumentId());
		}

	}

	private void updateCurrentTimeHighlight() {
		SimpleDateFormat sdf = new SimpleDateFormat("hh:00 a");
		String newCurrentSlot = sdf.format(new Date());

		if (!newCurrentSlot.equals(this.currentHourSlot)) {
			this.currentHourSlot = newCurrentSlot;
			updateActiveView();
		}
	}

	public String getCurrentHourSlot() {
		return currentHourSlot;
	}

	public void displayNewBooking(BookingDetails newBooking) {
		if (newBooking != null) {
			showBookingDetails(newBooking);
		}
	}

	public List<BookingDetails> findBookingsForHour(String selectedDateStr, String selectedTime, int courtNumber) {
		List<BookingDetails> overlappingBookings = new ArrayList<>();
		double targetHour = convertTimeSlotToHour(selectedTime);
		if (targetHour == -1) {
			return overlappingBookings;
		}

		Map<String, Map<Integer, List<BookingDetails>>> dayBookings = allBookings.getOrDefault(selectedDateStr,
				Collections.emptyMap());
		if (dayBookings.isEmpty()) {
			return overlappingBookings;
		}

		Set<BookingDetails> allUniqueBookingsForDay = new HashSet<>();
		dayBookings.values().stream().flatMap(courtMap -> courtMap.values().stream()).flatMap(List::stream)
				.forEach(allUniqueBookingsForDay::add);

		for (BookingDetails booking : allUniqueBookingsForDay) {
			boolean courtMatch = booking.getCourts() != null && booking.getCourts().contains(courtNumber);
			boolean timeMatch = booking.getStartTime() < (targetHour + 1) && booking.getEndTime() > targetHour;
			if (courtMatch && timeMatch) {
				overlappingBookings.add(booking);
			}
		}
		overlappingBookings.sort(Comparator.comparing(BookingDetails::getStartTime));

		return overlappingBookings;
	}

	@Override
	public void onNavigateBooking(int courtNumber, int direction) {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null)
			return;

		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();
		if (selectedTime == null)
			return;

		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		int timeIndex = uiBuilder.getTimeSlotsList().getSelectedIndex();

		int currentIndex = courtDisplayIndex.getOrDefault(courtNumber, 0);
		int newIndex = currentIndex + direction;

		List<BookingDetails> bookings = findBookingsForHour(dateStr, selectedTime, courtNumber);
		if (newIndex >= 0 && newIndex < bookings.size()) {
			courtDisplayIndex.put(courtNumber, newIndex);

			// --- THIS IS THE FIX (Part 1) ---
			// Also update the grid view's index to keep it in sync.
			if (timeIndex != -1) {
				int col = courtNumber - 1;
				String key = timeIndex + ":" + col;
				gridSlotDisplayIndex.put(key, newIndex);
			}
			// --- END OF FIX ---

			updateClassicViewDisplay();
			onInfoCourt(courtNumber);
		}
	}

	private void setupOverlay() {
		layeredPane = new JLayeredPane();
		final JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(uiBuilder.getLeftPanel(), BorderLayout.WEST);
		contentPanel.add(uiBuilder.getRightPanel(), BorderLayout.CENTER);

		offlinePanel = new JPanel();
		offlinePanel.setLayout(new GridBagLayout());
		offlinePanel.setBackground(new Color(0, 0, 0, 150));

		JLabel offlineLabel = new JLabel("Connection Lost. Reconnecting...");
		offlineLabel.setFont(new Font("Arial", Font.BOLD, 24));
		offlineLabel.setForeground(Color.WHITE);
		offlinePanel.add(offlineLabel);

		layeredPane.add(contentPanel, JLayeredPane.DEFAULT_LAYER);
		layeredPane.add(offlinePanel, JLayeredPane.MODAL_LAYER);
		offlinePanel.setVisible(false);

		layeredPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int width = layeredPane.getWidth();
				int height = layeredPane.getHeight();
				contentPanel.setBounds(0, 0, width, height);
				offlinePanel.setBounds(0, 0, width, height);
			}
		});
		setContentPane(layeredPane);
	}

	public void showOfflineOverlay() {
		offlinePanel.setVisible(true);
	}

	public void hideOfflineOverlay() {
		offlinePanel.setVisible(false);
	}

	public String[] getCafeteriaItems() {
		return CAFETERIA_ITEMS;
	}

	public Map<String, Double> getCafeItemPrices() {
		return cafeItemPrices;
	}

	public Map<String, Double> getGearItemPrices() {
		return gearItemPrices;
	}

	public BookingDetails getTempBookingDetails() {
		return tempBookingDetails;
	}

	public PricingSettings getPricingSettings() {
		return pricingSettings;
	}

	public List<MembershipPackage> getMembershipPackages() {
		return membershipPackages;
	}

	public User getCurrentUser() {
		return currentUser;
	}

	public boolean isShiftModeActive() {
		return isShiftModeActive;
	}

	private void attachRealtimeListeners(CountDownLatch latch) {
		membershipManager.attachMembershipListener(latch);

		firebaseDataManager.attachCafeItemsListener(loadedItems -> SwingUtilities.invokeLater(() -> {
            cafeItemPrices.clear();
            List<String> itemNames = new ArrayList<>();
            for (CafeItem item : loadedItems) {
                cafeItemPrices.put(item.getName(), item.getPrice());
                itemNames.add(item.getName());
            }
            CAFETERIA_ITEMS = itemNames.toArray(new String[0]);
            Arrays.sort(CAFETERIA_ITEMS);
            uiBuilder.updateCafeItemChoices(CAFETERIA_ITEMS);
        }));

        // --- THIS LISTENER HAS BEEN FIXED ---
		firebaseDataManager.attachGearItemsListener(loadedItems -> SwingUtilities.invokeLater(() -> {
			gearItemPrices.clear();
            // Loop through the list of GearItem objects to populate the price map
			for (GearItem item : loadedItems) {
                gearItemPrices.put(item.getName(), item.getPrice());
            }
		}));

		firebaseDataManager.attachMembershipPackageListener(packages -> {
			SwingUtilities.invokeLater(() -> this.membershipPackages = packages);
		});

		firebaseDataManager.attachPricingSettingsListener(settings -> {
			SwingUtilities.invokeLater(() -> {
				this.pricingSettings = settings;
				updateActiveView();
			});
		});
	}

	private void setShiftModeButtonState(boolean isShiftModeActive) {
		boolean enableState = !isShiftModeActive;
		uiBuilder.getSearchButton().setEnabled(enableState);
		uiBuilder.getAboutButton().setEnabled(enableState);
		uiBuilder.getHowToUseButton().setEnabled(enableState);
		uiBuilder.getCafeSaleButton().setEnabled(enableState);
		uiBuilder.getOutgoingButton().setEnabled(enableState);
		uiBuilder.getGearButton().setEnabled(enableState);
		uiBuilder.getRegistryViewButton().setEnabled(enableState);
		uiBuilder.getChangeViewButton().setEnabled(enableState);
		uiBuilder.getBulkBookingButton().setEnabled(enableState);
		uiBuilder.getSearchField().setEnabled(enableState);
	}

	private void setTopButtonState(boolean enabled) {
		uiBuilder.getSearchButton().setEnabled(enabled);
		uiBuilder.getAboutButton().setEnabled(enabled);
		uiBuilder.getHowToUseButton().setEnabled(enabled);
		uiBuilder.getCafeSaleButton().setEnabled(enabled);
		uiBuilder.getOutgoingButton().setEnabled(enabled);
		uiBuilder.getMembershipButton().setEnabled(enabled);
		uiBuilder.getGearButton().setEnabled(enabled);
		uiBuilder.getRegistryViewButton().setEnabled(enabled);
		uiBuilder.getChangeViewButton().setEnabled(enabled);
		uiBuilder.getDateChooser().setEnabled(enabled);
		uiBuilder.getSearchField().setEnabled(enabled);
	}

	public UIBuilder getUiBuilder() {
		return uiBuilder;
	}

	private void initWindow() {
		setTitle("Ace Alley Office Management");
		setUndecorated(true);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		JRootPane rootPane = this.getRootPane();
		KeyStroke adminExitKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q,
				KeyEvent.CTRL_DOWN_MASK | KeyEvent.SHIFT_DOWN_MASK);
		String adminActionKey = "ADMIN_EXIT";
		InputMap inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
		inputMap.put(adminExitKeyStroke, adminActionKey);
		Action adminExitAction = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		rootPane.getActionMap().put(adminActionKey, adminExitAction);
	}

	@Override
	public void onBookCourt(int courtNumber) {
		List<String> selectedTimes = uiBuilder.getTimeSlotsList().getSelectedValuesList();
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null || selectedTimes.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Please select a date and time slot first.", "Selection Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		String[] options = { "Regular Booking", "Membership Booking" };
		int choice = JOptionPane.showOptionDialog(this, "Select the type of booking:", "Booking Type",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			bookingManager.handleRegularBooking(courtNumber, selectedTimes, selectedDate);
		} else if (choice == 1) {
			bookingManager.handleMembershipBooking(courtNumber, selectedTimes, selectedDate);
		}
	}

	@Override
	public void onAddBookingToSlot(int courtNumber) {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();
		if (selectedDate == null || selectedTime == null) {
			JOptionPane.showMessageDialog(this, "Cannot add booking: Please ensure a time slot is selected.",
					"Selection Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		List<BookingDetails> bookingsInSlot = findBookingsForHour(dateStr, selectedTime, courtNumber);

		if (bookingsInSlot.isEmpty()) {
			return;
		}

		List<BookingDetails> activeBookings = bookingsInSlot.stream().filter(b -> !b.isDidNotShow())
				.collect(Collectors.toList());

		if (activeBookings.isEmpty()) {
			List<String> selectedTimes = List.of(selectedTime);
			proceedWithAddOnBooking(courtNumber, selectedDate, selectedTimes);
		} else if (activeBookings.size() == 1) {
			BookingDetails theActiveBooking = activeBookings.get(0);

			// --- THIS IS THE FIX ---
			// We now pass the 'selectedTime' so the logic knows which hour the user clicked
			// on.
			bookingManager.handleAdjacentHalfHourBooking(theActiveBooking, selectedDate, selectedTime);

		} else {
			JOptionPane.showMessageDialog(this, "This time slot is already full with active bookings.", "Slot Full",
					JOptionPane.WARNING_MESSAGE);
		}
	}

	private void proceedWithAddOnBooking(int courtNumber, Date selectedDate, List<String> selectedTimes) {
		JOptionPane.showMessageDialog(this,
				"You are adding a new booking to a slot where the original booker did not show.", "Add-on Booking",
				JOptionPane.INFORMATION_MESSAGE);

		String[] options = { "Regular Booking", "Membership Booking" };
		int choice = JOptionPane.showOptionDialog(this, "Select the type of the new booking:", "Add-on Booking Type",
				JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			bookingManager.handleRegularBooking(courtNumber, selectedTimes, selectedDate);
		} else if (choice == 1) {
			bookingManager.handleMembershipBooking(courtNumber, selectedTimes, selectedDate);
		}
	}

	@Override
	public void onCancelCourt(int courtNumber) {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();

		if (selectedDate == null || selectedTime == null) {
			JOptionPane.showMessageDialog(this, "Please select a date and time slot first.", "Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);

		List<BookingDetails> bookingsInSlot = findBookingsForHour(dateStr, selectedTime, courtNumber);
		int bookingIndex = courtDisplayIndex.getOrDefault(courtNumber, 0);

		if (!bookingsInSlot.isEmpty() && bookingIndex < bookingsInSlot.size()) {
			BookingDetails bookingToCancel = bookingsInSlot.get(bookingIndex);

			int result = JOptionPane.showConfirmDialog(this,
					"Cancel the booking for " + bookingToCancel.getBookerName() + "?", "Confirm Cancellation",
					JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				bookingManager.cancelEntireBooking(bookingToCancel, dateStr);
				if (bookingToCancel.equals(currentInfoBookingDetails)) {
					clearAndDisableInfoFields();
				}
			}
		} else {
			JOptionPane.showMessageDialog(this, "No booking found to cancel for this slot.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	@Override
	public void onInfoCourt(int courtNumber) {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();
		if (selectedDate == null || selectedTime == null) {
			clearAndDisableInfoFields();
			return;
		}
		String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		List<BookingDetails> bookings = findBookingsForHour(selectedDateStr, selectedTime, courtNumber);
		int currentIndex = courtDisplayIndex.getOrDefault(courtNumber, 0);

		if (!bookings.isEmpty() && currentIndex < bookings.size()) {
			BookingDetails details = bookings.get(currentIndex);

			// NEW: Save the ID of the booking being viewed
			saveLastViewedBooking(selectedDateStr, selectedTime, courtNumber, details.getDocumentId());

			currentInfoBookingDetails = details;
			tempBookingDetails = new BookingDetails(details);
			refreshInfoPanelDisplay();
			enableInfoFields(true);
			updateActiveView();
		} else {
			clearAndDisableInfoFields();
		}
	}

	public void toggleShiftMode() {
		if (!isShiftModeActive) {
			bookingBeingShifted = currentInfoBookingDetails;
			if (bookingBeingShifted == null) {
				return;
			}
			isShiftModeActive = true;
			setShiftModeButtonState(true);
			uiBuilder.getStartShiftButton().setText("Cancel Shift");
			uiBuilder.getStartShiftButton().setBackground(new Color(220, 53, 69));
			enableInfoFields(false);
			uiBuilder.getStartShiftButton().setEnabled(true);
			JOptionPane.showMessageDialog(this, "SHIFT MODE ACTIVATED:\nPlease select a new time slot on the grid.",
					"Shift Mode", JOptionPane.INFORMATION_MESSAGE);

		} else {
			isShiftModeActive = false;
			bookingBeingShifted = null;
			setShiftModeButtonState(false);
			uiBuilder.getStartShiftButton().setText("Start Shift");
			uiBuilder.getStartShiftButton().setBackground(new Color(23, 162, 184));

			if (currentInfoBookingDetails != null) {
				enableInfoFields(true);
			} else {
				enableInfoFields(false);
			}
		}

		for (int i = 1; i <= venueConfig.getNumberOfCourts(); i++) {
			Court court = uiBuilder.getCourt(i);
			if (court != null) {
				court.setShiftMode(isShiftModeActive);
			}
		}
	}

	public void handleChangeView() {
		isGridView = !isGridView;
		if (isGridView) {
			uiBuilder.getTimeSlotsScrollPane().setVisible(false);
		} else {
			uiBuilder.getTimeSlotsScrollPane().setVisible(true);
		}
		CardLayout cl = uiBuilder.getViewCardLayout();
		JPanel container = uiBuilder.getViewContainer();
		cl.next(container);
		updateActiveView();
	}

	public void updateActiveView() {
		if (isGridView) {
			updateGridViewDisplay();
		} else {
			updateClassicViewDisplay();
		}
	}

	public void updateClassicViewDisplay() {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null)
			return;

		String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		String selectedTime = uiBuilder.getTimeSlotsList().getSelectedValue();
		int numberOfCourts = venueConfig.getNumberOfCourts();

		for (int i = 1; i <= numberOfCourts; i++) {
			Court court = uiBuilder.getCourt(i);
			if (court == null)
				continue;

			List<BookingDetails> bookings = findBookingsForHour(selectedDateStr, selectedTime, i);

			if (!bookings.isEmpty()) {
				// --- THIS IS THE FIX (PART 2) ---
				// This method now ONLY reads from the in-memory map, which is correctly
				// updated by the navigation logic. It no longer resets the state.
				int targetIndex = courtDisplayIndex.getOrDefault(i, 0);
				if (targetIndex >= bookings.size()) {
					targetIndex = 0;
					courtDisplayIndex.put(i, 0);
				}
				// --- END OF FIX ---

				BookingDetails details = bookings.get(targetIndex);
				boolean isPaid = (details.getCashPaid() + details.getUpiPaid()) >= details.getPrice();

				court.updateBookingState(true, isPaid, details.isDidNotShow());
				court.setBookerName(details.getBookerName());
				court.setTotalAmount(details.getPrice());

				double duration = details.getEndTime() - details.getStartTime();
				boolean isMembershipBooking = details.getMembershipIdUsed() != null
						&& !details.getMembershipIdUsed().isEmpty();
				double racketPrice = isMembershipBooking ? pricingSettings.getRacket_member()
						: pricingSettings.getRacket_regular();
				double ballPrice = isMembershipBooking ? pricingSettings.getBall_member()
						: pricingSettings.getBall_regular();
				double racketCost = details.getRacketsUsed() * racketPrice * duration;
				double ballCost = details.getBallsUsed() * ballPrice * duration;
				double cafeCost = 0;
				if (details.getCafeItemsMap() != null) {
					for (Map.Entry<String, Integer> item : details.getCafeItemsMap().entrySet()) {
						cafeCost += cafeItemPrices.getOrDefault(item.getKey(), 0.0) * item.getValue();
					}
				}

				double courtCost;
				String bookingSource = "";

				if (isMembershipBooking) {
					courtCost = 0;
					bookingSource = " (Membership)";
				} else {
					if (details.isPlayoSelected() || details.isKhelomoreSelected()) {
						courtCost = 0.0;
						bookingSource = details.isPlayoSelected() ? " (Playo)" : " (Khelomore)";
					} else {
						double preDiscountTotal = details.getPrice() + details.getDiscountAmount();
						courtCost = preDiscountTotal - racketCost - ballCost - cafeCost;
					}
				}
				court.setCourtAmount(courtCost, bookingSource);
				court.setRentalAmount(racketCost, ballCost);
				court.setCafeAmount(cafeCost);
				court.setPaginatorInfo(targetIndex, bookings.size());
			} else {
				court.updateBookingState(false, false, false);
			}
		}
	}

	private void populateDnsPanel(BookingDetails details) {
		if (details == null)
			return;
		uiBuilder.getDnsBookerNameLabel().setText(details.getBookerName());
		uiBuilder.getDnsContactLabel().setText(details.getBookerContact());
		String timeInfo = formatHourToTime(details.getStartTime()) + " - " + formatHourToTime(details.getEndTime());
		uiBuilder.getDnsDateTimeLabel().setText(details.getDate() + " at " + timeInfo);
		double amountPaid = details.getCashPaid() + details.getUpiPaid();
		uiBuilder.getDnsPriceLabel().setText("₹" + String.format("%.2f", amountPaid));
	}

	public void refreshInfoPanelDisplay() {
		BookingDetails details = getCurrentInfoBookingDetails();
		if (details == null) {
			uiBuilder.getRightCardLayout().show(uiBuilder.getRightCardPanel(), "NORMAL");
			clearAndDisableInfoFields();
			return;
		}

		populateInfoPanel(details);
		populateDnsPanel(details);

		uiBuilder.getNormalDnsCheckbox().setSelected(details.isDidNotShow());
		uiBuilder.getOverlayDnsCheckbox().setSelected(details.isDidNotShow());

		if (details.isDidNotShow()) {
			uiBuilder.getRightCardLayout().show(uiBuilder.getRightCardPanel(), "DNS");
		} else {
			uiBuilder.getRightCardLayout().show(uiBuilder.getRightCardPanel(), "NORMAL");
		}
	}

	public void updateGridViewDisplay() {
		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null)
			return;

		String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		GridSlot[][] slots = uiBuilder.getBookingSlots();
		int numberOfCourts = venueConfig.getNumberOfCourts();

		for (int row = 0; row < timeSlots.length; row++) {
			for (int col = 0; col < numberOfCourts; col++) {
				slots[row][col].setBorder(BorderFactory.createMatteBorder(1, 1, 1, 1, new Color(70, 70, 70)));
				List<BookingDetails> bookingsInThisSlot = findBookingsForHour(selectedDateStr, timeSlots[row], col + 1);

				// --- THIS IS THE FIX (PART 3) ---
				// This method now ONLY reads from the in-memory map. It no longer loads state.
				String key = row + ":" + col;
				int displayIndex = gridSlotDisplayIndex.getOrDefault(key, 0);

				if (displayIndex >= bookingsInThisSlot.size()) {
					displayIndex = 0;
				}
				slots[row][col].updateDisplay(bookingsInThisSlot, displayIndex);
			}

		}

		// 6. After drawing everything, apply the highlight for the currently selected
		// booking.
		if (currentInfoBookingDetails != null && currentInfoBookingDetails.getDate().equals(selectedDateStr)) {
			double startTime = currentInfoBookingDetails.getStartTime();
			double endTime = currentInfoBookingDetails.getEndTime();

			// Iterate using 0.5 steps to correctly highlight 30-min slots
			for (double hour = startTime; hour < endTime; hour += 0.5) {
				for (int courtNum : currentInfoBookingDetails.getCourts()) {
					int courtIndex = courtNum - 1;
					int timeIndex = (int) Math.floor(hour);
					if (timeIndex < slots.length && courtIndex >= 0 && courtIndex < numberOfCourts) {
						slots[timeIndex][courtIndex]
								.setBorder(BorderFactory.createLineBorder(new Color(0, 255, 255), 2));
					}
				}
			}
		}
	}

	public void clearAndDisableInfoFields() {
		uiBuilder.getInfoCourtNumberField().setText("");
		uiBuilder.getInfoBookingDateField().setText("");
		uiBuilder.getInfoStartTimeField().setText("");
		uiBuilder.getInfoEndTimeField().setText("");
		uiBuilder.getBookerNameField().setText("");
		uiBuilder.getBookerContactField().setText("");
		uiBuilder.getBookerEmailField().setText("");
		uiBuilder.getPriceField().setText("");
		uiBuilder.getPlayoRadioButton().setSelected(false);
		uiBuilder.getKhelomoreRadioButton().setSelected(false);
		uiBuilder.getDiscountCheckbox().setSelected(false);
		uiBuilder.getDiscountAmountField().setText("0.00");
		uiBuilder.getCashCheckbox().setSelected(false);
		uiBuilder.getCashAmountField().setText("0.00");
		uiBuilder.getUpiCheckbox().setSelected(false);
		uiBuilder.getUpiAmountField().setText("0.00");
		uiBuilder.getRacketCountDisplay().setText("0");
		uiBuilder.getBallCountDisplay().setText("0");
		uiBuilder.getCafeItemsSummaryArea().setText("");
		enableInfoFields(false);
		currentInfoBookingDetails = null;
		tempBookingDetails = null;
		updateActiveView();
	}

	private void enableInfoFields(boolean enable) {
		uiBuilder.getBookerNameField().setEditable(enable);
		uiBuilder.getBookerContactField().setEditable(enable);
		uiBuilder.getBookerEmailField().setEditable(enable);
		uiBuilder.getPriceField().setEditable(enable);
		uiBuilder.getSaveChangesButton().setEnabled(enable);
		uiBuilder.getStartShiftButton().setEnabled(enable);
		uiBuilder.getCancelBookingButton().setEnabled(enable);
		uiBuilder.getExtendBookingButton().setEnabled(enable);
		uiBuilder.getIncreaseRacketsButton().setEnabled(enable);
		uiBuilder.getDecreaseRacketsButton().setEnabled(enable);
		uiBuilder.getIncreaseBallsButton().setEnabled(enable);
		uiBuilder.getDecreaseBallsButton().setEnabled(enable);
		uiBuilder.getRegularRadioButton().setEnabled(enable);
		uiBuilder.getPlayoRadioButton().setEnabled(enable);
		uiBuilder.getKhelomoreRadioButton().setEnabled(enable);
		uiBuilder.getCafeItemSelectionBox().setEnabled(enable);
		uiBuilder.getIncreaseCafeItemButton().setEnabled(enable);
		uiBuilder.getDecreaseCafeItemButton().setEnabled(enable);
		uiBuilder.getDiscountCheckbox().setEnabled(enable);
		uiBuilder.getCashCheckbox().setEnabled(enable);
		uiBuilder.getUpiCheckbox().setEnabled(enable);
		uiBuilder.getDiscountAmountField().setEditable(enable && uiBuilder.getDiscountCheckbox().isSelected());
		uiBuilder.getCashAmountField().setEditable(enable && uiBuilder.getCashCheckbox().isSelected());
		uiBuilder.getUpiAmountField().setEditable(enable && uiBuilder.getUpiCheckbox().isSelected());
		uiBuilder.getNormalDnsCheckbox().setEnabled(enable);
		uiBuilder.getOverlayDnsCheckbox().setEnabled(enable);
	}

	private boolean isTimeRangeAvailable(String dateStr, double newStartTime, double newEndTime, List<Integer> courts,
			BookingDetails bookingToIgnore) {
		Map<String, Map<Integer, List<BookingDetails>>> bookingsForDay = allBookings.get(dateStr);
		if (bookingsForDay == null) {
			return true;
		}

		for (List<BookingDetails> bookingsInSlot : bookingsForDay.values().stream()
				.flatMap(courtMap -> courtMap.values().stream()).collect(Collectors.toList())) {
			for (BookingDetails existingBooking : bookingsInSlot) {
				if (existingBooking.getDocumentId().equals(bookingToIgnore.getDocumentId())) {
					continue;
				}
				boolean courtConflict = existingBooking.getCourts().stream().anyMatch(courts::contains);
				if (courtConflict) {
					if (existingBooking.getStartTime() < newEndTime && existingBooking.getEndTime() > newStartTime) {
						return false;
					}
				}
			}
		}
		return true;
	}

	public String[] getTimeSlots() {
		return timeSlots;
	}

	private void populateInfoPanel(BookingDetails details) {
		String courtsStr = "N/A";
		if (details.getCourts() != null && !details.getCourts().isEmpty()) {
			courtsStr = details.getCourts().stream().sorted().map(String::valueOf).collect(Collectors.joining(", "));
		}
		uiBuilder.getInfoCourtNumberField().setText(courtsStr);
		uiBuilder.getInfoBookingDateField().setText(details.getDate());
		uiBuilder.getInfoStartTimeField().setText(formatHourToTime(details.getStartTime()));
		uiBuilder.getInfoEndTimeField().setText(formatHourToTime(details.getEndTime()));
		uiBuilder.getBookerNameField().setText(details.getBookerName());
		uiBuilder.getBookerContactField().setText(details.getBookerContact());
		uiBuilder.getBookerEmailField().setText(details.getBookerEmail());
		uiBuilder.getPriceField().setText(String.format("%.2f", details.getPrice()));
		uiBuilder.getRacketCountDisplay().setText(String.valueOf(details.getRacketsUsed()));
		uiBuilder.getBallCountDisplay().setText(String.valueOf(details.getBallsUsed()));
		if (details.getMembershipIdUsed() != null && !details.getMembershipIdUsed().isEmpty()) {
			uiBuilder.getPlatformPanel().setVisible(false);
			uiBuilder.getRegularRadioButton().setSelected(true);
		} else {
			uiBuilder.getPlatformPanel().setVisible(true);
			boolean isPlayo = details.isPlayoSelected();
			boolean isKhelomore = details.isKhelomoreSelected();
			uiBuilder.getPlayoRadioButton().setSelected(isPlayo);
			uiBuilder.getKhelomoreRadioButton().setSelected(isKhelomore);
			if (!isPlayo && !isKhelomore) {
				uiBuilder.getRegularRadioButton().setSelected(true);
			}
		}
		uiBuilder.getDiscountCheckbox().setSelected(details.getDiscountAmount() > 0);
		uiBuilder.getDiscountAmountField().setText(String.format("%.2f", details.getDiscountAmount()));
		uiBuilder.getDiscountAmountField().setVisible(details.getDiscountAmount() > 0);
		uiBuilder.getCashCheckbox().setSelected(details.getCashPaid() > 0);
		uiBuilder.getCashAmountField().setText(String.format("%.2f", details.getCashPaid()));
		uiBuilder.getCashAmountField().setVisible(details.getCashPaid() > 0);
		uiBuilder.getUpiCheckbox().setSelected(details.getUpiPaid() > 0);
		uiBuilder.getUpiAmountField().setText(String.format("%.2f", details.getUpiPaid()));
		uiBuilder.getUpiAmountField().setVisible(details.getUpiPaid() > 0);
		updateCafeItemQuantityDisplay();
		updateCafeItemsSummaryArea();
	}

	private String formatHourToTime(double d) {
		if (d == 24.0)
			return "12:00 AM";
		if (d < 0 || d >= 24)
			return "N/A";
		try {
			int hour = (int) d;
			int minute = (int) Math.round((d - hour) * 60);
			if (minute == 60) {
				hour += 1;
				minute = 0;
			}
			String time24h = String.format("%02d:%02d", hour, minute);
			SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
			Date date = twentyFourHourFormat.parse(time24h);
			return twelveHourFormat.format(date);
		} catch (ParseException e) {
			e.printStackTrace();
			return "N/A";
		}
	}

	private double safeParseDouble(String text) {
		if (text == null || text.trim().isEmpty()) {
			return 0.0;
		}
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException e) {
			return 0.0; // Return 0 if parsing fails
		}
	}

	/**
	 * Safely parses a string into an integer. Returns 0 if the string is empty,
	 * null, or not a valid number.
	 */
	private int safeParseInt(String text) {
		if (text == null || text.trim().isEmpty()) {
			return 0;
		}
		try {
			return Integer.parseInt(text.trim());
		} catch (NumberFormatException e) {
			return 0; // Return 0 if parsing fails
		}
	}

	// --- THIS IS THE CORRECTED METHOD ---
	public void updateTotalPriceDisplay() {
		if (currentInfoBookingDetails == null)
			return;
		try {
			Date bookingDate = new SimpleDateFormat("dd-MM-yyyy").parse(currentInfoBookingDetails.getDate());
			String timeSlot = currentInfoBookingDetails.getTime();
			int numCourts = currentInfoBookingDetails.getCourts().size();
			double duration = currentInfoBookingDetails.getEndTime() - currentInfoBookingDetails.getStartTime();
			double baseCourtPricePerHour = calculateBasePrice(bookingDate, timeSlot);
			double basePrice = baseCourtPricePerHour * duration * numCourts;

			// FIX: Using safe helper methods to prevent NumberFormatException
			int racketsUsed = safeParseInt(uiBuilder.getRacketCountDisplay().getText());
			int ballsUsed = safeParseInt(uiBuilder.getBallCountDisplay().getText());

			boolean isMembershipBooking = (tempBookingDetails != null
					&& tempBookingDetails.getMembershipIdUsed() != null);
			double racketPrice = isMembershipBooking ? pricingSettings.getRacket_member()
					: pricingSettings.getRacket_regular();
			double ballPrice = isMembershipBooking ? pricingSettings.getBall_member()
					: pricingSettings.getBall_regular();

			double racketsCost = racketsUsed * racketPrice * duration;
			double ballsCost = ballsUsed * ballPrice * duration;
			double cafeCost = 0;

			if (tempBookingDetails != null && tempBookingDetails.getCafeItemsMap() != null) {
				for (Map.Entry<String, Integer> item : tempBookingDetails.getCafeItemsMap().entrySet()) {
					cafeCost += cafeItemPrices.getOrDefault(item.getKey(), 0.0) * item.getValue();
				}
			}

			double currentCourtCost = basePrice;

			// --- THIS IS THE FIX ---
			// Add 'isMembershipBooking' to the condition.
			if (uiBuilder.getPlayoRadioButton().isSelected() || uiBuilder.getKhelomoreRadioButton().isSelected() || isMembershipBooking) {
			    currentCourtCost = 0.0;
			}
			// --- END OF FIX ---

			double discount = 0;
			if (uiBuilder.getDiscountCheckbox().isSelected()) {
			    discount = safeParseDouble(uiBuilder.getDiscountAmountField().getText());
			}

			double finalPrice = currentCourtCost + racketsCost + ballsCost + cafeCost - discount;
			uiBuilder.getPriceField().setText(String.format("%.2f", Math.max(0, finalPrice)));
		} catch (ParseException e) {
			uiBuilder.getPriceField().setText("Error");
			e.printStackTrace();
		}
	}

	public void updateCafeItemQuantityDisplay() {
		if (tempBookingDetails == null)
			return;
		String selectedItem = (String) uiBuilder.getCafeItemSelectionBox().getSelectedItem();
		if (selectedItem != null && tempBookingDetails.getCafeItemsMap() != null) {
			int quantity = tempBookingDetails.getCafeItemsMap().getOrDefault(selectedItem, 0);
			uiBuilder.getCafeItemQuantityDisplay().setText(String.valueOf(quantity));
		}
	}

	public void updateCafeItemsSummaryArea() {
		if (currentInfoBookingDetails == null || currentInfoBookingDetails.getCafeItemsMap() == null) {
			uiBuilder.getCafeItemsSummaryArea().setText("");
			return;
		}
		StringBuilder summary = new StringBuilder();
		currentInfoBookingDetails.getCafeItemsMap().entrySet().stream().filter(entry -> entry.getValue() > 0)
				.forEach(entry -> summary.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n"));
		uiBuilder.getCafeItemsSummaryArea().setText(summary.toString());
	}

	private double calculateBasePrice(Date selectedDate, String time) {
		if (selectedDate == null || time == null) {
			return getPricingSettings().getCourt_weekday_regular();
		}
		return this.pricingStrategy.calculateCourtPrice(selectedDate, time);
	}

	public void performSearch() {
		String searchQuery = uiBuilder.getSearchField().getText().trim();
		if (searchQuery.isEmpty() || searchQuery.equals("Search Contact or Name..."))
			return;

		Date selectedDate = uiBuilder.getDateChooser().getDate();
		if (selectedDate == null) {
			JOptionPane.showMessageDialog(this, "Please select a date before searching.", "Date Not Selected",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		final LoadingDialog loadingDialog = new LoadingDialog(this);

		firebaseDataManager.searchBookings(searchQuery, dateStr, results -> {
			SwingUtilities.invokeLater(() -> {
				loadingDialog.dispose();
				dialogFactory.showSearchResultsDialog(results, this::showBookingDetails);
				this.toFront();
			});
		});
		loadingDialog.setVisible(true);
	}

	public void showBookingDetails(BookingDetails bookingToShow) {
		if (bookingToShow == null)
			return;
		try {
			Date bookingDate = new SimpleDateFormat("dd-MM-yyyy").parse(bookingToShow.getDate());
			uiBuilder.getDateChooser().setDate(bookingDate);
			uiBuilder.getTimeSlotsList().setSelectedValue(bookingToShow.getTime(), true);

			int courtNum = bookingToShow.getCourts().get(0);
			List<BookingDetails> bookingsInSlot = findBookingsForHour(bookingToShow.getDate(), bookingToShow.getTime(),
					courtNum);

			int targetIndex = 0;
			for (int i = 0; i < bookingsInSlot.size(); i++) {
				if (bookingsInSlot.get(i).getDocumentId().equals(bookingToShow.getDocumentId())) {
					targetIndex = i;
					break;
				}
			}

			String key = uiBuilder.getTimeSlotsList().getSelectedIndex() + ":" + (courtNum - 1);
			gridSlotDisplayIndex.put(key, targetIndex);

			currentInfoBookingDetails = bookingToShow;
			tempBookingDetails = new BookingDetails(bookingToShow);
			refreshInfoPanelDisplay();
			enableInfoFields(true);
			updateActiveView();
		} catch (ParseException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "Error parsing booking date.", "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	public void handleExtendBooking() {
		BookingDetails details = getCurrentInfoBookingDetails();
		if (details == null) {
			JOptionPane.showMessageDialog(this, "No booking selected to extend.", "Error", JOptionPane.WARNING_MESSAGE);
			return;
		}
		String[] options = { "30 Minutes", "1 Hour", "Cancel" };
		int choice = JOptionPane.showOptionDialog(this, "Select extension duration:", "Extend Booking", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[1]);

		if (choice == 2 || choice == JOptionPane.CLOSED_OPTION) return;

		double hoursToExtend = (choice == 0) ? 0.5 : 1.0;
		double extensionStartTime = details.getEndTime();
		double extensionEndTime = details.getEndTime() + hoursToExtend;

		if (!isTimeRangeAvailable(details.getDate(), extensionStartTime, extensionEndTime, details.getCourts(), details)) {
			JOptionPane.showMessageDialog(this, "Cannot extend booking. The next time slot is not fully available.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}

		try {
			Date bookingDate = new SimpleDateFormat("dd-MM-yyyy").parse(details.getDate());
			boolean isMembershipBooking = details.getMembershipIdUsed() != null && !details.getMembershipIdUsed().isEmpty();

			if (isMembershipBooking) {
				double hoursNeeded = hoursToExtend * details.getCourts().size();
				Membership member = membershipManager.getMember(details.getMembershipIdUsed());
				if (member.getTotalHoursRemaining() < hoursNeeded) {
					JOptionPane.showMessageDialog(this, "Member does not have enough hours to extend.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				int confirm = JOptionPane.showConfirmDialog(this, "This will deduct " + hoursNeeded + " more hour(s) from the member's account. Continue?", "Confirm Extension", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
				    Map<String, Double> extensionLedger = membershipManager.deductHoursForBooking(member.getMemberId(), hoursNeeded);

				    Map<String, Double> ledgerToModify = details.getHoursDeductionLedger();
				    if (ledgerToModify == null) {
				        ledgerToModify = new HashMap<>();
				    }

				    // --- THIS IS THE FIX ---
				    // Use a new, 'final' variable so the lambda is happy.
				    final Map<String, Double> finalLedger = ledgerToModify;

				    if (extensionLedger != null) {
				        // Now, the lambda uses the final variable, which is allowed.
				        extensionLedger.forEach((key, value) -> finalLedger.merge(key, value, Double::sum));
				    }
				    details.setHoursDeductionLedger(finalLedger);
				    details.setEndTime(extensionEndTime);
				    bookingManager.saveBookingDetails(details);
				    refreshInfoPanelDisplay();
				    updateActiveView();
				}
			} else {
				double extensionCost = (calculateBasePrice(bookingDate, formatHourToTime(details.getEndTime())) * hoursToExtend) * details.getCourts().size();
				int confirm = JOptionPane.showConfirmDialog(this, "Extend booking by " + (hoursToExtend == 1 ? "1 hour" : "30 minutes") + " for an additional ₹" + String.format("%.2f", extensionCost) + "?", "Confirm Extension", JOptionPane.YES_NO_OPTION);
				if (confirm == JOptionPane.YES_OPTION) {
					details.setPrice(details.getPrice() + extensionCost);
					details.setEndTime(extensionEndTime);
					bookingManager.saveBookingDetails(details);
					refreshInfoPanelDisplay();
					updateActiveView();
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public static void applyModernTheme() {
		try {
			UIManager.setLookAndFeel(new MaterialLookAndFeel());
            MaterialLookAndFeel.changeTheme(new MaterialOceanicTheme());

            // --- THIS IS THE FIX ---
            // Force global colors for components that the theme might miss.
            Color darkGray = new Color(45, 45, 45);
            Color mediumGray = new Color(60, 60, 60);

            // This specifically targets the empty space below the JTable in a JScrollPane.
            UIManager.put("Viewport.background", darkGray);
            
            // Also ensure the table and its header have the correct defaults.
            UIManager.put("Table.background", mediumGray);
            UIManager.put("TableHeader.background", new Color(30, 30, 30));
            // --- END OF FIX ---

            // Keep the JDateChooser customizations
            // ... (JDateChooser settings are correct) ...

		} catch (Exception e) {
			System.err.println("Failed to initialize Material UI Look and Feel.");
			e.printStackTrace();
		}
	}

	private void clearGridSelectionHighlights() {
		if (isGridView) {
			updateGridViewDisplay();
		}
	}

	private MouseAdapter createGridMouseListener() {
		return new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Object source = e.getSource();
				GridSlot sourcePanel = null;

				if (source instanceof GridSlot) {
					sourcePanel = (GridSlot) source;
				} else if (source instanceof JLabel) {
					sourcePanel = (GridSlot) ((JLabel) source).getParent();
				}
				if (sourcePanel == null)
					return;

				int row = -1, col = -1;
				GridSlot[][] bookingSlots = uiBuilder.getBookingSlots();
				for (int i = 0; i < timeSlots.length; i++) {
					for (int j = 0; j < venueConfig.getNumberOfCourts(); j++) {
						if (sourcePanel == bookingSlots[i][j]) {
							row = i;
							col = j;
							break;
						}
					}
					if (row != -1)
						break;
				}
				if (row == -1)
					return;

				String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(uiBuilder.getDateChooser().getDate());
				boolean isSlotBooked = !findBookingsForHour(dateStr, timeSlots[row], col + 1).isEmpty();

				if (isSlotBooked && !isShiftModeActive) {
					isDragging = false;
					clearGridSelectionHighlights();

					List<BookingDetails> bookings = findBookingsForHour(dateStr, timeSlots[row], col + 1);
					if (bookings.isEmpty())
						return;

					String key = row + ":" + col;
					int currentIndex = gridSlotDisplayIndex.getOrDefault(key, 0);
					if (currentIndex >= bookings.size())
						currentIndex = 0;

					BookingDetails bookingToSelect = bookings.get(currentIndex);

					currentInfoBookingDetails = bookingToSelect;
					tempBookingDetails = new BookingDetails(bookingToSelect);
					refreshInfoPanelDisplay();

					// --- THIS IS THE FIX ---
					// Explicitly enable all the fields and buttons in the info panel.
					enableInfoFields(true);
					// --- END OF FIX ---

					updateActiveView();

					saveLastViewedBooking(dateStr, timeSlots[row], col + 1, bookingToSelect.getDocumentId());

					return;
				}

				isDragging = true;
				dragStartRow = dragEndRow = row;
				dragStartCol = dragEndCol = col;
				updateSelectionHighlight();

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				if (!isDragging)
					return;

				Object source = e.getSource();
				GridSlot sourcePanel = null;
				if (source instanceof GridSlot) {
					sourcePanel = (GridSlot) source;
				} else if (source instanceof JLabel) {
					sourcePanel = (GridSlot) ((JLabel) source).getParent();
				}
				if (sourcePanel == null)
					return;

				GridSlot[][] bookingSlots = uiBuilder.getBookingSlots();
				for (int i = 0; i < timeSlots.length; i++) {
					for (int j = 0; j < venueConfig.getNumberOfCourts(); j++) {
						if (sourcePanel == bookingSlots[i][j]) {
							dragEndRow = i;
							dragEndCol = j;
							updateSelectionHighlight();
							return;
						}
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				if (!isDragging)
					return;
				isDragging = false;

				int startRow = Math.min(dragStartRow, dragEndRow);
				int endRow = Math.max(dragStartRow, dragEndRow);
				int startCol = Math.min(dragStartCol, dragEndCol);
				int endCol = Math.max(dragStartCol, dragEndCol);
				List<String> selectedTimes = new ArrayList<>();
				for (int r = startRow; r <= endRow; r++) {
					selectedTimes.add(timeSlots[r]);
				}
				List<Integer> selectedCourts = new ArrayList<>();
				for (int c = startCol; c <= endCol; c++) {
					selectedCourts.add(c + 1);
				}
				if (selectedTimes.isEmpty() || selectedCourts.isEmpty())
					return;

				if (isShiftModeActive) {
					handleBookingShift(selectedTimes, selectedCourts);
				} else if (isBulkBookingModeActive) {
					bookingManager.handleBulkBooking(bulkBookingStartDate, bulkBookingEndDate, selectedTimes,
							selectedCourts);
					exitBulkBookingMode();
				} else {
					handleNewBooking(selectedTimes, selectedCourts);
				}
			}
		};
	}

	public void startBulkBookingProcess() {
		if (isBulkBookingModeActive) {
			exitBulkBookingMode();
			JOptionPane.showMessageDialog(this, "Bulk Booking Mode Canceled.", "Canceled",
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		if (isShiftModeActive) {
			JOptionPane.showMessageDialog(this, "Please finish or cancel the current shift operation first.",
					"Operation in Progress", JOptionPane.WARNING_MESSAGE);
			return;
		}

		BulkBookingDialog dialog = new BulkBookingDialog(this);
		dialog.setVisible(true);

		Date startDate = dialog.getStartDate();
		Date endDate = dialog.getEndDate();

		if (startDate != null && endDate != null) {
			this.bulkBookingStartDate = startDate;
			this.bulkBookingEndDate = endDate;
			this.isBulkBookingModeActive = true;

			setTopButtonState(false);
			uiBuilder.getBulkBookingButton().setText("Stop Bulk Book");
			uiBuilder.getBulkBookingButton().setBackground(new Color(220, 53, 69));

			if (!isGridView) {
				handleChangeView();
			}
			setClassicCourtModes(true);
			JOptionPane.showMessageDialog(this, "BULK BOOKING MODE:\nPlease select the desired time slots on the grid.",
					"Bulk Booking", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void exitBulkBookingMode() {
		this.isBulkBookingModeActive = false;
		this.bulkBookingStartDate = null;
		this.bulkBookingEndDate = null;
		setTopButtonState(true);
		uiBuilder.getBulkBookingButton().setText("Bulk Booking");
		uiBuilder.getBulkBookingButton().setBackground(new Color(28, 184, 92));
		setClassicCourtModes(false);
	}

	private void setClassicCourtModes(boolean isBulkMode) {
		for (int i = 1; i <= venueConfig.getNumberOfCourts(); i++) {
			Court court = uiBuilder.getCourt(i);
			if (court != null) {
				court.setBulkBookingMode(isBulkMode);
			}
		}
	}

	private void handleNewBooking(List<String> selectedTimes, List<Integer> selectedCourts) {
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(uiBuilder.getDateChooser().getDate());
		for (String time : selectedTimes) {
			for (int court : selectedCourts) {
				if (!findBookingsForHour(dateStr, time, court).isEmpty()) {
					JOptionPane.showMessageDialog(this, "Selection includes a booked slot.", "Selection Error",
							JOptionPane.ERROR_MESSAGE);
					clearGridSelectionHighlights();
					return;
				}
			}
		}

		String[] options = { "Regular Booking", "Membership Booking" };
		int choice = JOptionPane.showOptionDialog(this, "Select the type of booking for the selected block:",
				"Booking Type", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		if (choice == 0) {
			bookingManager.handleMultiCourtBooking(selectedCourts, selectedTimes, uiBuilder.getDateChooser().getDate());
		} else if (choice == 1) {
			if (selectedCourts.size() > 1 || selectedTimes.size() > 1) {
				bookingManager.handleMembershipBlockBooking(selectedCourts, selectedTimes,
						List.of(uiBuilder.getDateChooser().getDate()));
			} else {
				bookingManager.handleMembershipBooking(selectedCourts.get(0), selectedTimes,
						uiBuilder.getDateChooser().getDate());
			}
		}
	}

	private double convertTimeSlotToHour(String timeSlot) {
		if (timeSlot == null || timeSlot.trim().isEmpty()) {
			return -1;
		}
		try {
			SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
			Date date = twelveHourFormat.parse(timeSlot);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			int minute = calendar.get(Calendar.MINUTE);
			return hour + (minute / 60.0);
		} catch (ParseException e) {
			e.printStackTrace();
			return -1;
		}
	}

	public void refreshGearItems() {
		firebaseDataManager.loadGearItems(loadedItems -> {
			SwingUtilities.invokeLater(() -> {
				gearItemPrices.clear();
				gearItemPrices.putAll(loadedItems);
				System.out.println("Gear items have been refreshed from Firestore.");
			});
		});
	}

	public void refreshCafeItems() {
		firebaseDataManager.loadCafeItems(loadedItems -> {
			SwingUtilities.invokeLater(() -> {
				cafeItemPrices.clear();
				cafeItemPrices.putAll(loadedItems);
				CAFETERIA_ITEMS = cafeItemPrices.keySet().toArray(new String[0]);
				Arrays.sort(CAFETERIA_ITEMS);
				// Assuming your UIBuilder class has this method
				uiBuilder.updateCafeItemChoices(CAFETERIA_ITEMS);
				System.out.println("Cafe items have been refreshed from Firestore.");
			});
		});
	}

	private void handleBookingShift(List<String> newTimes, List<Integer> newCourts) {
		String newDateStr = new SimpleDateFormat("dd-MM-yyyy").format(uiBuilder.getDateChooser().getDate());

		// 1. Check for availability in the new slot
		for (String time : newTimes) {
			for (int court : newCourts) {
				List<BookingDetails> conflictingBookings = findBookingsForHour(newDateStr, time, court);
				for (BookingDetails conflicting : conflictingBookings) {
					if (!conflicting.getDocumentId().equals(bookingBeingShifted.getDocumentId())) {
						JOptionPane.showMessageDialog(this, "The new selection includes a slot that is already booked.", "Shift Error", JOptionPane.ERROR_MESSAGE);
						clearGridSelectionHighlights();
						return;
					}
				}
			}
		}

		boolean isMembershipBooking = bookingBeingShifted.getMembershipIdUsed() != null && !bookingBeingShifted.getMembershipIdUsed().isEmpty();

        // --- THIS ENTIRE BLOCK HAS BEEN REWRITTEN ---
		if (isMembershipBooking) {
            // Step 1: Immediately refund all hours from the original booking.
			Map<String, Double> originalLedger = bookingBeingShifted.getHoursDeductionLedger();
			if (originalLedger != null && !originalLedger.isEmpty()) {
				membershipManager.refundMemberHours(bookingBeingShifted.getMembershipIdUsed(), originalLedger);
			}

            // Step 2: Calculate hours needed for the NEW booking.
			double newTotalHours = (double) newTimes.size() * newCourts.size();
			
            // Step 3: Check if the member has enough hours for the new booking *after the refund*.
			Membership member = membershipManager.getMember(bookingBeingShifted.getMembershipIdUsed());
			if (member.getTotalHoursRemaining() < newTotalHours) {
				JOptionPane.showMessageDialog(this,
					"Member does not have enough hours for the new time slot.\nThe original hours have been refunded to their account.",
					"Shift Error & Refund Complete", JOptionPane.ERROR_MESSAGE);
				
                // Since the shift failed, we treat it as a cancellation.
				firebaseDataManager.removeBooking(bookingBeingShifted.getDocumentId());
				bookingBeingShifted = null;
				toggleShiftMode();
				clearAndDisableInfoFields();
				updateActiveView();
				return;
			}

            // Step 4: Deduct the hours for the new booking and get the NEW ledger.
			Map<String, Double> newLedger = membershipManager.deductHoursForBooking(bookingBeingShifted.getMembershipIdUsed(), newTotalHours);
			
            // Step 5: Update the booking object with the new ledger.
			bookingBeingShifted.setHoursDeductionLedger(newLedger);
		}

		// Price recalculation for regular bookings (this part is unchanged and correct)
		if (!isMembershipBooking) {
			double duration = newTimes.size();
			double totalSlots = (double) newTimes.size() * newCourts.size();
			double baseCourtPricePerHr = calculateBasePrice(uiBuilder.getDateChooser().getDate(), newTimes.get(0));
			double racketPrice = pricingSettings.getRacket_regular();
			double ballPrice = pricingSettings.getBall_regular();
			double racketsCost = bookingBeingShifted.getRacketsUsed() * racketPrice * duration;
			double ballsCost = bookingBeingShifted.getBallsUsed() * ballPrice * duration;
			double courtCost = 0;
			if (!bookingBeingShifted.isPlayoSelected() && !bookingBeingShifted.isKhelomoreSelected()) {
				courtCost = baseCourtPricePerHr * totalSlots;
			}
			double cafeCost = 0;
			if (bookingBeingShifted.getCafeItemsMap() != null) {
				for (Map.Entry<String, Integer> item : bookingBeingShifted.getCafeItemsMap().entrySet()) {
					cafeCost += cafeItemPrices.getOrDefault(item.getKey(), 0.0) * item.getValue();
				}
			}
			double finalPrice = courtCost + racketsCost + ballsCost + cafeCost - bookingBeingShifted.getDiscountAmount();
			bookingBeingShifted.setPrice(finalPrice);
		}
        // --- END OF REWRITTEN BLOCK ---

		// 6. Update the booking object with all new details and save it
		double newStartTime = convertTimeSlotToHour(newTimes.get(0));
		double newEndTime = newStartTime + newTimes.size();
		bookingBeingShifted.setDate(newDateStr);
		bookingBeingShifted.setTime(newTimes.get(0));
		bookingBeingShifted.setCourts(newCourts);
		bookingBeingShifted.setStartTime(newStartTime);
		bookingBeingShifted.setEndTime(newEndTime);
		
		bookingManager.saveBookingDetails(bookingBeingShifted);

		// 7. Exit shift mode and refresh UI
		toggleShiftMode();
		clearAndDisableInfoFields();
		updateActiveView();
		JOptionPane.showMessageDialog(this, "Booking successfully shifted!", "Success", JOptionPane.INFORMATION_MESSAGE);
	}

	private void updateSelectionHighlight() {
		clearGridSelectionHighlights();
		int startRow = Math.min(dragStartRow, dragEndRow);
		int endRow = Math.max(dragStartRow, dragEndRow);
		int startCol = Math.min(dragStartCol, dragEndCol);
		int endCol = Math.max(dragStartCol, dragEndCol);
		GridSlot[][] slots = uiBuilder.getBookingSlots();
		for (int r = startRow; r <= endRow; r++) {
			for (int c = startCol; c <= endCol; c++) {
				slots[r][c].setBackground(new Color(0, 102, 153));
			}
		}
	}

	public Map<String, Map<String, Map<Integer, List<BookingDetails>>>> getAllBookings() {
		return allBookings;
	}

	public BookingDetails getCurrentInfoBookingDetails() {
		return currentInfoBookingDetails;
	}

	@Override
	public void onSlotClicked(int timeIndex, int courtNumber) {
		// Intentionally left blank
	}
}