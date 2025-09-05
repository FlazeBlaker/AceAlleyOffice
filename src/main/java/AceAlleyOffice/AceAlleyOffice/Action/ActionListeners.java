// File: AceAlleyOffice/AceAlleyOffice/Action/ActionListeners.java
package AceAlleyOffice.AceAlleyOffice.Action;

import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.beans.PropertyChangeListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.cloud.firestore.ListenerRegistration;

import AceAlleyOffice.AceAlleyOffice.Core.BookingManager;
import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.MembershipManager;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.UIBuilder;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.DialogFactory;

public class ActionListeners {

	private final Main mainApp;
	private final UIBuilder uiBuilder;
	private final FirebaseDataManager firebaseDataManager;
	private final MembershipManager membershipManager;
	private final DialogFactory dialogFactory;
	private final BookingManager bookingManager;
	private ListenerRegistration activeBookingListenerRegistration;
	private String listenedDate = null;

	public ActionListeners(Main mainApp, UIBuilder uiBuilder, FirebaseDataManager firebaseDataManager,
			MembershipManager membershipManager, DialogFactory dialogFactory, BookingManager bookingManager) {
		this.mainApp = mainApp;
		this.uiBuilder = uiBuilder;
		this.firebaseDataManager = firebaseDataManager;
		this.membershipManager = membershipManager;
		this.dialogFactory = dialogFactory;
		this.bookingManager = bookingManager;
		attachAllListeners();
	}

	private void handleDnsToggle(JCheckBox sourceCheckbox) {
		BookingDetails details = mainApp.getCurrentInfoBookingDetails();
		if (details == null) {
			sourceCheckbox.setSelected(false);
			return;
		}

		boolean isDns = sourceCheckbox.isSelected();
		details.setDidNotShow(isDns);
		bookingManager.saveBookingDetails(details);
		// The save triggers a data refresh which calls refreshInfoPanelDisplay in Main,
		// which will switch the overlay and sync the state of both checkboxes.
	}

	private void attachAllListeners() {
		// --- Main Buttons ---
		uiBuilder.getSearchButton().addActionListener(e -> mainApp.performSearch());
		uiBuilder.getAboutButton().addActionListener(e -> dialogFactory.showAboutDialog());
		uiBuilder.getHowToUseButton().addActionListener(e -> dialogFactory.showHowToUseDialog());
		uiBuilder.getCafeSaleButton()
				.addActionListener(e -> dialogFactory.showCafeSaleDialog(uiBuilder.getDateChooser().getDate()));
		uiBuilder.getOutgoingButton()
				.addActionListener(e -> dialogFactory.showOutgoingDialog(uiBuilder.getDateChooser().getDate()));
		// This listener should be in your ActionListeners.java file or wherever you set
		// up your UI listeners.

		uiBuilder.getMembershipButton().addActionListener(e -> dialogFactory.showMembershipDialog(membershipManager)); // REMOVED:
																														// The
																														// date
																														// argument
		uiBuilder.getRegistryViewButton().addActionListener(e -> {
			Date selectedDate = uiBuilder.getDateChooser().getDate();
			if (selectedDate == null) {
				JOptionPane.showMessageDialog(mainApp, "Please select a date first.", "Date Required",
						JOptionPane.WARNING_MESSAGE);
				return; // Stop here if no date is selected
			}
			dialogFactory.showRegistryView(selectedDate);
		});
		uiBuilder.getBulkBookingButton().addActionListener(e -> mainApp.startBulkBookingProcess());
		uiBuilder.getSaveChangesButton().addActionListener(e -> saveChanges());
		uiBuilder.getChangeViewButton().addActionListener(e -> mainApp.handleChangeView());
		uiBuilder.getGearButton()
				.addActionListener(e -> dialogFactory.showGearSaleDialog(uiBuilder.getDateChooser().getDate()));
		uiBuilder.getStartShiftButton().addActionListener(e -> mainApp.toggleShiftMode());
		ActionListener dnsToggleListener = e -> handleDnsToggle((JCheckBox) e.getSource());
		uiBuilder.getNormalDnsCheckbox().addActionListener(dnsToggleListener);
		uiBuilder.getOverlayDnsCheckbox().addActionListener(dnsToggleListener); // --- Core Listeners ---
		uiBuilder.getDateChooser().addPropertyChangeListener("date", dateChangeListener());
		uiBuilder.getExtendBookingButton().addActionListener(e -> mainApp.handleExtendBooking());

		// --- FIXED: This listener was too aggressive. ---
		// It no longer clears the info fields, which was causing the bug.
		uiBuilder.getTimeSlotsList().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				mainApp.updateActiveView();
			}
		});

		uiBuilder.getSearchField().addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				if (uiBuilder.getSearchField().getText().equals("Search Contact or Name...")) {
					uiBuilder.getSearchField().setText("");
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (uiBuilder.getSearchField().getText().isEmpty()) {
					uiBuilder.getSearchField().setText("Search Contact or Name...");
				}
			}
		});

		// --- FIXED: This now correctly calls the robust onCancelCourt method in
		uiBuilder.getCancelBookingButton().addActionListener(e -> {
			BookingDetails bookingToCancel = mainApp.getCurrentInfoBookingDetails();
			if (bookingToCancel != null) {
				int result = JOptionPane.showConfirmDialog(mainApp,
						"Cancel the booking for " + bookingToCancel.getBookerName() + "?", "Confirm Cancellation",
						JOptionPane.YES_NO_OPTION);

				if (result == JOptionPane.YES_OPTION) {
					// Use the booking object's own date for cancellation
					bookingManager.cancelEntireBooking(bookingToCancel, bookingToCancel.getDate());
					mainApp.clearAndDisableInfoFields();
				}
			} else {
				JOptionPane.showMessageDialog(mainApp, "No booking selected in the info panel to cancel.", "Error",
						JOptionPane.WARNING_MESSAGE);
			}
		});

		// --- Listeners for Info Panel Interactivity ---
		uiBuilder.getDecreaseRacketsButton().addActionListener(e -> updateCounter(true, -1));
		uiBuilder.getIncreaseRacketsButton().addActionListener(e -> updateCounter(true, 1));
		uiBuilder.getDecreaseBallsButton().addActionListener(e -> updateCounter(false, -1));
		uiBuilder.getIncreaseBallsButton().addActionListener(e -> updateCounter(false, 1));

		uiBuilder.getDiscountCheckbox().addActionListener(e -> {
			boolean isSelected = uiBuilder.getDiscountCheckbox().isSelected();
			uiBuilder.getDiscountAmountField().setVisible(isSelected);
			uiBuilder.getDiscountAmountField().setEditable(isSelected);
			if (!isSelected)
				uiBuilder.getDiscountAmountField().setText("0.00");
			mainApp.updateTotalPriceDisplay();
		});
		uiBuilder.getCashCheckbox().addActionListener(e -> {
			boolean isSelected = uiBuilder.getCashCheckbox().isSelected();
			uiBuilder.getCashAmountField().setVisible(isSelected);
			uiBuilder.getCashAmountField().setEditable(isSelected);
			if (!isSelected)
				uiBuilder.getCashAmountField().setText("0.00");
		});
		uiBuilder.getUpiCheckbox().addActionListener(e -> {
			boolean isSelected = uiBuilder.getUpiCheckbox().isSelected();
			uiBuilder.getUpiAmountField().setVisible(isSelected);
			uiBuilder.getUpiAmountField().setEditable(isSelected);
			if (!isSelected)
				uiBuilder.getUpiAmountField().setText("0.00");
		});

		ActionListener priceUpdateListener = e -> mainApp.updateTotalPriceDisplay();
		uiBuilder.getRegularRadioButton().addActionListener(priceUpdateListener);
		uiBuilder.getPlayoRadioButton().addActionListener(priceUpdateListener);
		uiBuilder.getKhelomoreRadioButton().addActionListener(priceUpdateListener);

		DocumentListener priceUpdateDocumentListener = new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				mainApp.updateTotalPriceDisplay();
			}

			public void removeUpdate(DocumentEvent e) {
				mainApp.updateTotalPriceDisplay();
			}

			public void insertUpdate(DocumentEvent e) {
				mainApp.updateTotalPriceDisplay();
			}
		};
		uiBuilder.getDiscountAmountField().getDocument().addDocumentListener(priceUpdateDocumentListener);

		uiBuilder.getCafeItemSelectionBox().addActionListener(e -> mainApp.updateCafeItemQuantityDisplay());
		uiBuilder.getIncreaseCafeItemButton().addActionListener(e -> updateCafeItem(1));
		uiBuilder.getDecreaseCafeItemButton().addActionListener(e -> updateCafeItem(-1));
	}

	private void updateCounter(boolean isRacket, int change) {
		BookingDetails tempDetails = mainApp.getTempBookingDetails();
		if (tempDetails == null)
			return;

		int currentValue = isRacket ? tempDetails.getRacketsUsed() : tempDetails.getBallsUsed();
		int newValue = Math.max(0, currentValue + change);

		if (isRacket) {
			tempDetails.setRacketsUsed(newValue);
			uiBuilder.getRacketCountDisplay().setText(String.valueOf(newValue));
		} else {
			tempDetails.setBallsUsed(newValue);
			uiBuilder.getBallCountDisplay().setText(String.valueOf(newValue));
		}

		mainApp.updateTotalPriceDisplay();
	}

	private void updateCafeItem(int change) {
		BookingDetails tempDetails = mainApp.getTempBookingDetails();
		String selectedItem = (String) uiBuilder.getCafeItemSelectionBox().getSelectedItem();

		if (tempDetails != null && selectedItem != null) {
			Map<String, Integer> cafeItemsMap = tempDetails.getCafeItemsMap();
			int currentQuantity = cafeItemsMap.getOrDefault(selectedItem, 0);
			int newQuantity = Math.max(0, currentQuantity + change);
			cafeItemsMap.put(selectedItem, newQuantity);

			mainApp.updateCafeItemQuantityDisplay();
			mainApp.updateTotalPriceDisplay();
		}
	}

	private PropertyChangeListener dateChangeListener() {
		return evt -> {
			if (!"date".equals(evt.getPropertyName()))
				return;

			if (activeBookingListenerRegistration != null) {
				activeBookingListenerRegistration.remove();
			}
			Date newDate = (Date) evt.getNewValue();
			if (newDate != null) {
				listenedDate = new SimpleDateFormat("dd-MM-yyyy").format(newDate);
				activeBookingListenerRegistration = firebaseDataManager.attachBookingListenerForDate(listenedDate,
						(snapshot, error) -> {
							if (error != null) {
								System.err.println("Listen failed: " + error);
								return;
							}

							Map<String, Map<Integer, List<BookingDetails>>> bookingsForDay = new HashMap<>();
							if (snapshot != null) {
								snapshot.getDocuments().forEach(doc -> {
									BookingDetails details = doc.toObject(BookingDetails.class);
									if (details != null && details.getCourts() != null) {

										// --- THIS IS THE FIX FOR OVERNIGHT BOOKINGS ---
										long startTime = (long) details.getStartTime();
										long endTime = (long) details.getEndTime();

										if (endTime <= startTime) { // Overnight Booking
											// Part 1: From start time until midnight (on the current day)
											for (long hour = startTime; hour < 24; hour++) {
												addBookingToMap(bookingsForDay, details, hour);
											}

											// Part 2: From midnight to end time (on the NEXT day)
											String nextDate = getNextDateString(listenedDate);
											if (nextDate != null) {
												// This safely gets or creates the map for the next day.
												Map<String, Map<Integer, List<BookingDetails>>> bookingsForNextDay = mainApp
														.getAllBookings().computeIfAbsent(nextDate, k -> new HashMap<>());
												

												for (long hour = 0; hour < endTime; hour++) {
													addBookingToMap(bookingsForNextDay, details, hour);
												}
											}
										} else { // Same-Day Booking
											for (long hour = startTime; hour < endTime; hour++) {
												addBookingToMap(bookingsForDay, details, hour);
											}
										}
										// --- END OF FIX ---
									}
								});
							}
							mainApp.getAllBookings().put(listenedDate, bookingsForDay);

							mainApp.clearAndDisableInfoFields();
							mainApp.updateActiveView();
						});
			}
		};
	}

	/**
	 * Helper method to add a booking to the correct list within the complex map
	 * structure.
	 * 
	 * @param bookingsForDay The map for a specific day.
	 * @param details        The booking to add.
	 * @param hour           The specific hour-long slot to add the booking to.
	 */
	private void addBookingToMap(Map<String, Map<Integer, List<BookingDetails>>> bookingsForDay, BookingDetails details,
			long hour) {
		String timeSlotString = formatHourToTime(hour);
		if (timeSlotString == null)
			return;

		Map<Integer, List<BookingDetails>> bookingsForTime = bookingsForDay.computeIfAbsent(timeSlotString,
				k -> new HashMap<>());

		for (Integer courtNum : details.getCourts()) {
			List<BookingDetails> bookingsForCourt = bookingsForTime.computeIfAbsent(courtNum, k -> new ArrayList<>());

			// Avoid adding the same booking object multiple times to the same list
			if (!bookingsForCourt.contains(details)) {
				bookingsForCourt.add(details);

			}
		}
	}

	private String formatHourToTime(long hour) {
		if (hour < 0 || hour >= 24)
			return null;
		try {
			SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH");
			SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:00 a");
			Date date = twentyFourHourFormat.parse(String.valueOf(hour));
			return twelveHourFormat.format(date);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Takes a date string (dd-MM-yyyy) and returns the string for the next day.
	 * 
	 * @param dateStr The input date string.
	 * @return The date string for the following day.
	 */
	private String getNextDateString(String dateStr) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
			Date date = sdf.parse(dateStr);
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(date);
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			return sdf.format(calendar.getTime());
		} catch (ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private void saveChanges() {
		BookingDetails originalDetails = mainApp.getCurrentInfoBookingDetails();
		BookingDetails tempDetails = mainApp.getTempBookingDetails();
		if (originalDetails == null || tempDetails == null) {
			JOptionPane.showMessageDialog(mainApp, "No booking is selected to save.", "Save Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}

		java.util.function.Function<String, Double> safeParseDouble = (text) -> {
			if (text == null || text.trim().isEmpty())
				return 0.0;
			try {
				return Double.parseDouble(text.trim());
			} catch (NumberFormatException e) {
				return 0.0;
			}
		};

		originalDetails.setBookerName(uiBuilder.getBookerNameField().getText().toLowerCase());
		originalDetails.setBookerContact(uiBuilder.getBookerContactField().getText());
		originalDetails.setBookerEmail(uiBuilder.getBookerEmailField().getText());
		originalDetails.setPrice(safeParseDouble.apply(uiBuilder.getPriceField().getText()));

		originalDetails.setRacketsUsed(tempDetails.getRacketsUsed());
		originalDetails.setBallsUsed(tempDetails.getBallsUsed());
		originalDetails.setCafeItemsMap(tempDetails.getCafeItemsMap());

		originalDetails.setPlayoSelected(uiBuilder.getPlayoRadioButton().isSelected());
		originalDetails.setKhelomoreSelected(uiBuilder.getKhelomoreRadioButton().isSelected());
		originalDetails.setDiscountAmount(uiBuilder.getDiscountCheckbox().isSelected()
				? safeParseDouble.apply(uiBuilder.getDiscountAmountField().getText())
				: 0.0);
		originalDetails.setCashPaid(uiBuilder.getCashCheckbox().isSelected()
				? safeParseDouble.apply(uiBuilder.getCashAmountField().getText())
				: 0.0);
		originalDetails.setUpiPaid(
				uiBuilder.getUpiCheckbox().isSelected() ? safeParseDouble.apply(uiBuilder.getUpiAmountField().getText())
						: 0.0);

		bookingManager.saveBookingDetails(originalDetails);
		mainApp.updateCafeItemsSummaryArea();
	}
}