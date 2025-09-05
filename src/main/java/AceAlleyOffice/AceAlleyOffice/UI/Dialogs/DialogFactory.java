// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/DialogFactory.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.MembershipManager;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.SearchResultDialog.OnSearchResultClickedListener;

public class DialogFactory {

	private final Main mainApp;
	private final JFrame owner;
	// This map structure is assumed to be: Date -> Venue -> Court -> List<Booking>
	private final Map<String, Map<String, Map<Integer, List<BookingDetails>>>> allBookings;
	private final FirebaseDataManager firebaseDataManager;
	private boolean isHalfHour; // <-- ADD THIS NEW FIELD

	public DialogFactory(Main mainApp, Map<String, Map<String, Map<Integer, List<BookingDetails>>>> allBookings,
			FirebaseDataManager firebaseDataManager) {
		this.mainApp = mainApp;
		this.owner = mainApp;
		this.allBookings = allBookings;
		this.firebaseDataManager = firebaseDataManager;
		this.isHalfHour = false; // Default to false
	}
    public boolean isHalfHour() {
        return isHalfHour;
    }

    public void setHalfHour(boolean isHalfHour) {
        this.isHalfHour = isHalfHour;
    }
	public void showAboutDialog() {
		new AboutDialog(owner).setVisible(true);
	}

	public void showHowToUseDialog() {
		new HowToUseDialog(owner).setVisible(true);
	}
	public void showRegistryViewDialog(String dateStr) {
		new RegistryViewDialog(mainApp, dateStr, allBookings).setVisible(true);
	}
    
	// This method is in your DialogFactory.java class
	public void showMembershipDialog(MembershipManager membershipManager) {
	    new MembershipDialog(
	        mainApp, 
	        membershipManager, 
	        firebaseDataManager, 
	        mainApp.getMembershipPackages(),
	        mainApp.getCurrentUser() 
	    ).setVisible(true);
	}

	public void showOutgoingDialog(Date selectedDate) {
		if (selectedDate == null) {
			JOptionPane.showMessageDialog(owner, "Please select a date first.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		new OutgoingDialog(owner, firebaseDataManager, selectedDate).setVisible(true);
	}

	public void showGearSaleDialog(Date selectedDate) {
		if (selectedDate == null) {
			JOptionPane.showMessageDialog(owner, "Please select a date first.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		GearSaleDialog dialog = new GearSaleDialog(mainApp, firebaseDataManager, selectedDate,
				mainApp.getCurrentUser());
		dialog.setVisible(true);
	}

	public void showCafeSaleDialog(Date selectedDate) {
		if (selectedDate == null) {
			JOptionPane.showMessageDialog(owner, "Please select a date first.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		CafeSaleDialog dialog = new CafeSaleDialog(mainApp, firebaseDataManager, selectedDate,
				mainApp.getCurrentUser());
		dialog.setVisible(true);
	}

	public void showSearchResultsDialog(List<BookingDetails> results, OnSearchResultClickedListener listener) {
		SearchResultDialog dialog = new SearchResultDialog(owner, results, listener);
		dialog.setVisible(true);
	}

	public void showRegistryView(Date selectedDate) {
		if (selectedDate == null) {
			JOptionPane.showMessageDialog(owner, "Please select a date to view.", "Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		// bookingsForDate is a map of: Venue -> Court -> List<Booking>
		Map<String, Map<Integer, List<BookingDetails>>> bookingsForDate = allBookings.getOrDefault(dateStr,
				Collections.emptyMap());

		JDialog registryDialog = new JDialog(owner, "Detailed Registry for " + dateStr, true);
		registryDialog.setSize(1920, 1080);
		registryDialog.setLocationRelativeTo(owner);
		registryDialog.setLayout(new BorderLayout());

		String[] columnNames = { "Platform", "Sr.No", "Name", "Contact", "Time", "Courts", "Hours", "UPI", "Cash",
				"Discount", "Total", "Paid" };

		DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};
		JTable registryTable = new JTable(tableModel);

		// --- FIXED: Correctly flattened the nested map to get all unique bookings for
		// the day ---
		Set<BookingDetails> uniqueBookings = new HashSet<>();
		if (bookingsForDate != null) {
			// Iterate through each venue's map of bookings
			for (Map<Integer, List<BookingDetails>> courtMap : bookingsForDate.values()) {
				// Iterate through each court's list of bookings
				for (List<BookingDetails> bookingList : courtMap.values()) {
					// Add all bookings from the list to our set
					uniqueBookings.addAll(bookingList);
				}
			}
		}

		int srNo = 1;
		for (BookingDetails details : uniqueBookings) {
			String platform = "Walk-in";
			if (details.isPlayoSelected())
				platform = "Playo";
			else if (details.isKhelomoreSelected())
				platform = "Khelomore";
			else if (details.getMembershipIdUsed() != null && !details.getMembershipIdUsed().isEmpty())
				platform = "Membership";

			double hours = details.getEndTime() - details.getStartTime();
			String courtsStr = details.getCourts().stream().sorted().map(String::valueOf)
					.collect(Collectors.joining(", "));
			// --- Calls the new, corrected formatHourToTime method ---
			String timeRange = formatHourToTime(details.getStartTime()) + " - "
					+ formatHourToTime(details.getEndTime());

			double totalPaid = details.getCashPaid() + details.getUpiPaid();

			Object[] rowData = { platform, srNo++, details.getBookerName(), details.getBookerContact(), timeRange,
					courtsStr, hours, details.getUpiPaid(), details.getCashPaid(), details.getDiscountAmount(),
					details.getPrice(), totalPaid };
			tableModel.addRow(rowData);
		}

		registryTable.setRowSorter(new TableRowSorter<>(tableModel));
		registryDialog.add(new JScrollPane(registryTable), BorderLayout.CENTER);

		JButton closeButton = new JButton("Close");
		closeButton.addActionListener(e -> registryDialog.dispose());
		JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		bottomPanel.add(closeButton);
		registryDialog.add(bottomPanel, BorderLayout.SOUTH);

		registryDialog.setVisible(true);
	}

	/**
	 * --- FIXED HELPER METHOD --- Correctly formats a double representing an hour
	 * (e.g., 8.5 for 8:30) into a 12-hour AM/PM time string.
	 * 
	 * @param d The hour value (e.g., 14.5 for 2:30 PM).
	 * @return A formatted time string like "02:30 PM".
	 */
	private String formatHourToTime(double d) {
		// Handle the special case where a booking ends at midnight.
		if (d == 24.0) {
			return "12:00 AM";
		}
		// Ensure the hour is within a valid 24-hour day range.
		if (d < 0 || d >= 24) {
			return "N/A";
		}
		try {
			// Separate the whole number for the hour.
			int hour = (int) d;
			// Convert the decimal part to minutes, rounding to handle floating-point
			// inaccuracies.
			int minute = (int) Math.round((d - hour) * 60);

			// If rounding results in 60 minutes, adjust to the next hour.
			if (minute == 60) {
				hour += 1;
				minute = 0;
			}

			// Create a 24-hour format string (e.g., "08:30").
			String time24h = String.format("%02d:%02d", hour, minute);

			// Use SimpleDateFormat to parse the 24-hour string and re-format it to 12-hour
			// AM/PM.
			SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm");
			SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
			Date date = twentyFourHourFormat.parse(time24h);
			return twelveHourFormat.format(date);
		} catch (ParseException e) {
			// This should not be reached with the controlled input, but is here for safety.
			e.printStackTrace();
			return "N/A";
		}
	}
}