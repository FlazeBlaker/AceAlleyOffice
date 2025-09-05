// File: AceAlleyOffice/AceAlleyOffice/Core/BookingManager.java
package AceAlleyOffice.AceAlleyOffice.Core;

import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.BookingDialog;
import AceAlleyOffice.AceAlleyOffice.UI.Membership;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

public class BookingManager {

	private final Main owner;
	private final FirebaseDataManager firebaseDataManager;
	private final MembershipManager membershipManager;
	private final String venueId;
	private final NotificationService notificationService; // <-- 1. ADD THIS FIELD

	public BookingManager(Main owner, FirebaseDataManager dataManager, MembershipManager membershipManager, String venueId) {
        this.owner = owner;
        this.firebaseDataManager = dataManager;
        this.membershipManager = membershipManager;
        this.venueId = venueId;
        this.notificationService = new NotificationService(); // <-- INITIALIZE IT
    }

	public void handleRegularBooking(int courtNumber, List<String> selectedTimes, Date selectedDate) {
		String title = "Confirm Booking for Court " + courtNumber;
		BookingDialog dialog = new BookingDialog(owner, title);

		if (selectedTimes.size() == 1) {
			String timeSlot = selectedTimes.get(0);
			boolean[] availability = getHalfHourAvailability(selectedDate, timeSlot, courtNumber);
			boolean firstHalfTaken = availability[0];
			boolean secondHalfTaken = availability[1];

			if (firstHalfTaken && secondHalfTaken) {
				JOptionPane.showMessageDialog(owner, "This hour is fully booked with two half-hour sessions.",
						"Slot Full", JOptionPane.WARNING_MESSAGE);
				return;
			}

			double hourStart = convertTimeSlotToHour(timeSlot);
			String firstSlotText = formatHourToTime(hourStart) + " - " + formatHourToTime(hourStart + 0.5);
			String secondSlotText = formatHourToTime(hourStart + 0.5) + " - " + formatHourToTime(hourStart + 1.0);
			dialog.configureForHalfHourBooking(firstSlotText, firstHalfTaken, secondSlotText, secondHalfTaken);
		}

		addContactListener(dialog.getBookerContactPrompt(), dialog.getBookerNamePrompt(),
				dialog.getBookerEmailPrompt());

		if (dialog.showDialog()) {
			BookingDetails newBooking = processAndSaveBooking(dialog, selectedDate, selectedTimes,
					List.of(courtNumber));
			owner.displayNewBooking(newBooking);
			if (newBooking != null) {
				owner.displayNewBooking(newBooking);
			}
		}
	}

	// NOTE: This is the corrected method
	public void handleAdjacentHalfHourBooking(BookingDetails existingBooking, Date selectedDate,
			String clickedTimeSlot) {
		// 1. Get context from the user's click
		double clickedHourStart = convertTimeSlotToHour(clickedTimeSlot);
		int courtNumber = existingBooking.getCourts().get(0);

		// 2. Get availability for the hour the user is interested in
		boolean[] availability = getHalfHourAvailability(selectedDate, clickedTimeSlot, courtNumber);
		boolean firstHalfTaken = availability[0];
		boolean secondHalfTaken = availability[1];

		// 3. Offer the available slot(s) within that specific hour
		if (!firstHalfTaken && !secondHalfTaken) {
			// This can happen if the existing booking ends exactly at the start of this
			// slot.
			// In this case, we can treat it as a request for a new regular booking.
			handleRegularBooking(courtNumber, List.of(clickedTimeSlot), selectedDate);

		} else if (!firstHalfTaken) {
			// If the first half (e.g., 3:00 - 3:30) is available, offer to book it.
			double newBookingStartTime = clickedHourStart;
			String message = "Do you want to book the available slot from " + formatHourToTime(newBookingStartTime)
					+ " to " + formatHourToTime(newBookingStartTime + 0.5) + "?";
			int confirm = JOptionPane.showConfirmDialog(owner, message, "Confirm Booking", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				promptForAndSaveFolderBooking(existingBooking, selectedDate, newBookingStartTime);
			}
		} else if (!secondHalfTaken) {
			// If the second half (e.g., 3:30 - 4:00) is available, offer to book it.
			// THIS HANDLES YOUR SCENARIO.
			double newBookingStartTime = clickedHourStart + 0.5;
			String message = "Do you want to book the available slot from " + formatHourToTime(newBookingStartTime)
					+ " to " + formatHourToTime(newBookingStartTime + 0.5) + "?";
			int confirm = JOptionPane.showConfirmDialog(owner, message, "Confirm Booking", JOptionPane.YES_NO_OPTION);
			if (confirm == JOptionPane.YES_OPTION) {
				promptForAndSaveFolderBooking(existingBooking, selectedDate, newBookingStartTime);
			}
		}
		// If both halves are taken, do nothing. The user will see the slot is full.
	}

	private void promptForAndSaveFolderBooking(BookingDetails adjacentBooking, Date selectedDate, double newStartTime) {
		BookingDialog dialog = new BookingDialog(owner, "New 30-Min Booker Details");
		addContactListener(dialog.getBookerContactPrompt(), dialog.getBookerNamePrompt(),
				dialog.getBookerEmailPrompt());
		if (dialog.showDialog()) {
			processAndSaveAdjacentHalfHourBooking(dialog, adjacentBooking, selectedDate, newStartTime);
		}
	}

	// The rest of the file remains the same...
	public void handleMultiCourtBooking(List<Integer> selectedCourts, List<String> selectedTimes, Date selectedDate) {
		String title = "Confirm Block Booking for Courts: " + selectedCourts.toString();
		BookingDialog dialog = new BookingDialog(owner, title);
		addContactListener(dialog.getBookerContactPrompt(), dialog.getBookerNamePrompt(),
				dialog.getBookerEmailPrompt());
		if (dialog.showDialog()) {
			processAndSaveBooking(dialog, selectedDate, selectedTimes, selectedCourts);
		}
	}

	public void handleBulkBooking(Date startDate, Date endDate, List<String> selectedTimes,
			List<Integer> selectedCourts) {
		if (!areAllSlotsAvailable(startDate, endDate, selectedTimes, selectedCourts)) {
			JOptionPane.showMessageDialog(owner,
					"One or more slots in the selected date range are already booked. Bulk booking aborted.",
					"Availability Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		String[] options = { "Regular Booking", "Membership Booking" };
		int choice = JOptionPane.showOptionDialog(owner, "All slots are available. Select booking type:",
				"Booking Type", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (choice == JOptionPane.CLOSED_OPTION)
			return;

		List<Date> dateRange = getDateRange(startDate, endDate);
		if (choice == 0) {
			BookingDialog dialog = new BookingDialog(owner, "Bulk Booker Details");
			dialog.setSize(500, 500);
			addContactListener(dialog.getBookerContactPrompt(), dialog.getBookerNamePrompt(),
					dialog.getBookerEmailPrompt());
			if (dialog.showDialog()) {
				for (Date day : dateRange) {
					processAndSaveBooking(dialog, day, selectedTimes, selectedCourts);
				}
				JOptionPane.showMessageDialog(owner, "Bulk booking completed successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			}
		} else {
			handleMembershipBlockBooking(selectedCourts, selectedTimes, dateRange);
		}
	}

	private void processAndSaveAdjacentHalfHourBooking(BookingDialog dialog, BookingDetails adjacentBooking,
			Date selectedDate, double newStartTime) {
		String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		String bookerName = dialog.getBookerNamePrompt().getText().toLowerCase();
		String bookerContact = dialog.getBookerContactPrompt().getText();
		String bookerEmail = dialog.getBookerEmailPrompt().getText();
		int racketsUsed = Integer.parseInt(dialog.getRacketsPrompt().getText().trim());
		int ballsUsed = Integer.parseInt(dialog.getBallsPrompt().getText().trim());

		double durationInHours = 0.5;
		double baseCourtPricePerHr = calculateBasePrice(selectedDate, formatHourToTime(newStartTime));
		double courtCost = baseCourtPricePerHr * adjacentBooking.getCourts().size() * durationInHours;
		double racketsTotalCost = racketsUsed * owner.getPricingSettings().getRacket_regular() * durationInHours;
		double ballsTotalCost = ballsUsed * owner.getPricingSettings().getBall_regular() * durationInHours;
		double finalPrice = courtCost + racketsTotalCost + ballsTotalCost;

		String bookingId = bookerContact + "_" + System.currentTimeMillis() + "_" + selectedDateStr;
		BookingDetails finalDetails = new BookingDetails(bookingId, finalPrice, null, bookerName, bookerContact,
				bookerEmail, racketsUsed, ballsUsed, 0, false, false, 0, 0);

		finalDetails.setPrice(finalPrice);
		finalDetails.setHalfHour(true);
		finalDetails.setCourts(adjacentBooking.getCourts());
		finalDetails.setStartTime(newStartTime);
		finalDetails.setEndTime(newStartTime + 0.5);
		finalDetails.setDate(selectedDateStr);
		finalDetails.setTime(formatHourToTime(Math.floor(newStartTime)));
		finalDetails.setTimestamp(Timestamp.now());
		finalDetails.setVenue(venueId);

		firebaseDataManager.addNewBooking(finalDetails);
		firebaseDataManager.addOrUpdateContact(finalDetails.getBookerContact(), finalDetails.getBookerName(),
				finalDetails.getBookerEmail());
	}

    private BookingDetails processAndSaveBooking(BookingDialog dialog, Date selectedDate, List<String> selectedTimes, List<Integer> selectedCourts) {
        String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
        String bookerName = dialog.getBookerNamePrompt().getText().trim();
        String bookerContact = dialog.getBookerContactPrompt().getText().trim();

        if (bookerName.isEmpty() || bookerContact.isEmpty()) {
            JOptionPane.showMessageDialog(owner, "Please enter the Booker Name and Phone Number.", "Missing Information", JOptionPane.WARNING_MESSAGE);
            return null;
        }

        String bookerEmail = dialog.getBookerEmailPrompt().getText();
        int racketsUsed = Integer.parseInt(dialog.getRacketsPrompt().getText().trim());
        int ballsUsed = Integer.parseInt(dialog.getBallsPrompt().getText().trim());
        boolean isPlayo = dialog.getPromptPlayoRadioButton().isSelected();
        boolean isKhelomore = dialog.getPromptKhelomoreRadioButton().isSelected();

        double startTime, endTime, durationInHours;
        boolean isHalfHourBooking = false;

        // --- THIS IS THE FIX ---
        // New logic to handle the three radio buttons (first half, second half, or full hour).
        if (dialog.isHalfHourMode() && dialog.getPromptRegularRadioButton().isSelected()) {
            double hourStart = convertTimeSlotToHour(selectedTimes.get(0));
            if (dialog.getFirstHalfHourRadio().isSelected()) {
                durationInHours = 0.5;
                isHalfHourBooking = true;
                startTime = hourStart;
                endTime = hourStart + 0.5;
            } else if (dialog.getSecondHalfHourRadio().isSelected()) {
                durationInHours = 0.5;
                isHalfHourBooking = true;
                startTime = hourStart + 0.5;
                endTime = hourStart + 1.0;
            } else { // The new fullHourRadio must be selected
                durationInHours = 1.0;
                isHalfHourBooking = false;
                startTime = hourStart;
                endTime = hourStart + 1.0;
            }
        } else {
            // This is the original logic for multi-hour or Playo/Khelomore bookings
            durationInHours = selectedTimes.size();
            isHalfHourBooking = false;
            startTime = convertTimeSlotToHour(selectedTimes.get(0));
            endTime = convertTimeSlotToHour(selectedTimes.get(selectedTimes.size() - 1)) + 1.0;
        }
        // --- END OF FIX ---

        double baseCourtPricePerHr = calculateBasePrice(selectedDate, selectedTimes.get(0));
        double courtCost = (isPlayo || isKhelomore) ? 0.0 : (baseCourtPricePerHr * selectedCourts.size() * durationInHours);
        double racketsTotalCost = racketsUsed * owner.getPricingSettings().getRacket_regular() * durationInHours;
        double ballsTotalCost = ballsUsed * owner.getPricingSettings().getBall_regular() * durationInHours;
        double finalPrice = courtCost + racketsTotalCost + ballsTotalCost;

        String bookingId = bookerContact + "_" + System.currentTimeMillis() + "_" + selectedDateStr;
        BookingDetails finalDetails = new BookingDetails(bookingId, finalPrice, null, bookerName, bookerContact, bookerEmail, racketsUsed, ballsUsed, 0, isPlayo, isKhelomore, 0, 0);
        
        finalDetails.setPrice(finalPrice);
        finalDetails.setHalfHour(isHalfHourBooking); // Set based on the new logic
        finalDetails.setCourts(selectedCourts);
        finalDetails.setStartTime(startTime);
        finalDetails.setEndTime(endTime);
        finalDetails.setDate(selectedDateStr);
        finalDetails.setTime(selectedTimes.get(0));
        finalDetails.setTimestamp(Timestamp.now());
        finalDetails.setVenue(venueId);
        
        firebaseDataManager.addNewBooking(finalDetails);
        firebaseDataManager.addOrUpdateContact(finalDetails.getBookerContact(), finalDetails.getBookerName(), finalDetails.getBookerEmail());
        notificationService.sendBookingConfirmation(finalDetails);
        return finalDetails;
    }

	public void handleMembershipBooking(int courtNumber, List<String> selectedTimes, Date selectedDate) {
		String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
		int hoursNeeded = selectedTimes.size();

		String searchQuery = JOptionPane.showInputDialog(owner, "Enter member name or phone to search:");
		if (searchQuery == null || searchQuery.trim().isEmpty())
			return;

		List<Membership> members = membershipManager.findMembersByName(searchQuery);
		if (members.isEmpty()) {
			JOptionPane.showMessageDialog(owner, "No member found.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		Membership selectedMember = (Membership) JOptionPane.showInputDialog(owner, "Select a member:", "Select Member",
				JOptionPane.QUESTION_MESSAGE, null, members.toArray(), members.get(0));
		if (selectedMember == null)
			return;

		if (selectedMember.getTotalHoursRemaining() < hoursNeeded) {
			JOptionPane.showMessageDialog(owner, "Member does not have enough hours for this booking!", "Booking Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}

		JTextField racketsPrompt = new JTextField("0");
		JTextField ballsPrompt = new JTextField("0");
		Object[] message = { "Member: " + selectedMember.getName(), "Hours to be deducted: " + hoursNeeded,
				"Rackets Used:", racketsPrompt, "Balls Used:", ballsPrompt };
		int result = JOptionPane.showConfirmDialog(owner, message, "Confirm Membership Booking",
				JOptionPane.OK_CANCEL_OPTION);

		if (result == JOptionPane.OK_OPTION) {
			try {
				int racketsUsed = Integer.parseInt(racketsPrompt.getText().trim());
				int ballsUsed = Integer.parseInt(ballsPrompt.getText().trim());

				BookingDetails details = new BookingDetails();
				details.setBookerName(selectedMember.getName().toLowerCase());
				details.setBookerContact(selectedMember.getPhoneNumber());
				details.setBookerEmail(selectedMember.getEmail());
				details.setMembershipIdUsed(selectedMember.getMemberId());
				details.setRacketsUsed(racketsUsed);
				details.setBallsUsed(ballsUsed);

				double durationInHours = selectedTimes.size();
				double racketsTotalCost = details.getRacketsUsed() * owner.getPricingSettings().getRacket_member()
						* durationInHours;
				double ballsTotalCost = details.getBallsUsed() * owner.getPricingSettings().getBall_member()
						* durationInHours;
				details.setPrice(racketsTotalCost + ballsTotalCost);

				double startTime = convertTimeSlotToHour(selectedTimes.get(0));
				double endTime = convertTimeSlotToHour(selectedTimes.get(selectedTimes.size() - 1)) + 1.0;
				details.setStartTime(startTime);
				details.setEndTime(endTime);

				details.setCourts(List.of(courtNumber));
				details.setDate(selectedDateStr);
				details.setTime(selectedTimes.get(0));
				details.setTimestamp(Timestamp.now());
				details.setBookingId(selectedMember.getMemberId() + "_" + System.currentTimeMillis());
				details.setVenue(venueId);

				Firestore db = FirebaseManager.getFirestore();
				db.runTransaction(transaction -> {
					DocumentReference memberRef = db.collection("memberships").document(selectedMember.getMemberId());
					DocumentSnapshot memberSnapshot = transaction.get(memberRef).get();
					Membership memberInDb = memberSnapshot.toObject(Membership.class);

					if (memberInDb == null || memberInDb.getTotalHoursRemaining() < hoursNeeded) {
						throw new Exception("Member not found or has insufficient hours.");
					}
					Map<String, Double> ledger = memberInDb.deductHours(hoursNeeded);
					transaction.set(memberRef, memberInDb);

					details.setHoursDeductionLedger(ledger);
					DocumentReference newBookingRef = db.collection("bookings").document();
					transaction.set(newBookingRef, details);

					return null;
				}).get();

				JOptionPane.showMessageDialog(owner, "Membership booking confirmed.", "Success",
						JOptionPane.INFORMATION_MESSAGE);

			} catch (Exception e) {
				JOptionPane.showMessageDialog(owner, "Booking failed: " + e.getMessage(), "Transaction Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void handleMembershipBlockBooking(List<Integer> selectedCourts, List<String> selectedTimes,
			List<Date> dateRange) {
		int totalHoursNeeded = selectedTimes.size() * selectedCourts.size() * dateRange.size();

		String searchQuery = JOptionPane.showInputDialog(owner, "Enter member name or phone for bulk booking:");
		if (searchQuery == null || searchQuery.trim().isEmpty())
			return;
		List<Membership> members = membershipManager.findMembersByName(searchQuery);
		if (members.isEmpty()) {
			JOptionPane.showMessageDialog(owner, "No member found.", "Search Result", JOptionPane.INFORMATION_MESSAGE);
			return;
		}
		Membership selectedMember = (Membership) JOptionPane.showInputDialog(owner, "Select a member:", "Select Member",
				JOptionPane.QUESTION_MESSAGE, null, members.toArray(), members.get(0));
		if (selectedMember == null)
			return;
		if (selectedMember.getTotalHoursRemaining() < totalHoursNeeded) {
			JOptionPane.showMessageDialog(owner, "Member does not have enough hours for this entire date range!",
					"Booking Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		int confirm = JOptionPane.showConfirmDialog(owner,
				"Deduct " + totalHoursNeeded + " hours from " + selectedMember.getName() + "'s account?\nContinue?",
				"Confirm Bulk Membership Booking", JOptionPane.OK_CANCEL_OPTION);

		if (confirm == JOptionPane.OK_OPTION) {
			try {
				Firestore db = FirebaseManager.getFirestore();
				db.runTransaction(transaction -> {
					DocumentReference memberRef = db.collection("memberships").document(selectedMember.getMemberId());
					DocumentSnapshot memberSnapshot = transaction.get(memberRef).get();
					Membership memberInDb = memberSnapshot.toObject(Membership.class);

					if (memberInDb == null || memberInDb.getTotalHoursRemaining() < totalHoursNeeded) {
						throw new Exception("Member not found or has insufficient hours for bulk booking.");
					}

					Map<String, Double> ledger = memberInDb.deductHours(totalHoursNeeded);
					transaction.set(memberRef, memberInDb);

					for (Date day : dateRange) {
						String selectedDateStr = new SimpleDateFormat("dd-MM-yyyy").format(day);
						BookingDetails details = new BookingDetails();
						details.setBookerName(selectedMember.getName().toLowerCase());
						details.setBookerContact(selectedMember.getPhoneNumber());
						details.setBookerEmail(selectedMember.getEmail());
						details.setMembershipIdUsed(selectedMember.getMemberId());
						details.setPrice(0);

						double startTime = convertTimeSlotToHour(selectedTimes.get(0));
						double endTime = convertTimeSlotToHour(selectedTimes.get(selectedTimes.size() - 1)) + 1.0;
						details.setStartTime(startTime);
						details.setEndTime(endTime);

						details.setCourts(selectedCourts);
						details.setDate(selectedDateStr);
						details.setTime(selectedTimes.get(0));
						details.setTimestamp(Timestamp.now());
						details.setBookingId(selectedMember.getMemberId() + "_" + System.currentTimeMillis() + "_"
								+ selectedDateStr);
						details.setHoursDeductionLedger(ledger);
						details.setVenue(venueId);

						DocumentReference newBookingRef = db.collection("bookings").document();
						transaction.set(newBookingRef, details);
					}
					return null;
				}).get();

				JOptionPane.showMessageDialog(owner, "Bulk membership booking confirmed.", "Success",
						JOptionPane.INFORMATION_MESSAGE);

			} catch (Exception e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(owner, "Bulk booking failed: " + e.getMessage(), "Transaction Error",
						JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	public void cancelEntireBooking(BookingDetails targetBooking, String dateStr) {
		if (targetBooking == null || targetBooking.getDocumentId() == null) {
			JOptionPane.showMessageDialog(owner, "No valid booking selected to cancel.", "Error",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String startTimeStr = formatHourToTime(targetBooking.getStartTime());
		String endTimeStr = formatHourToTime(targetBooking.getEndTime());
		String confirmationMessage = String.format("This will cancel the booking for %s from %s to %s.\nAre you sure?",
				targetBooking.getBookerName(), startTimeStr, endTimeStr);
		int result = JOptionPane.showConfirmDialog(owner, confirmationMessage, "Confirm Cancellation",
				JOptionPane.YES_NO_OPTION);

		if (result == JOptionPane.OK_OPTION) {
			if (targetBooking.getMembershipIdUsed() != null && !targetBooking.getMembershipIdUsed().isEmpty()) {
				Map<String, Double> ledger = targetBooking.getHoursDeductionLedger();
				membershipManager.refundMemberHours(targetBooking.getMembershipIdUsed(), ledger);
			}
			firebaseDataManager.removeBooking(targetBooking.getDocumentId());
			JOptionPane.showMessageDialog(owner, "The booking has been cancelled.", "Success",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	public void saveBookingDetails(BookingDetails currentDetails) {
		firebaseDataManager.updateBooking(currentDetails);
		JOptionPane.showMessageDialog(owner, "Booking details updated.", "Save Success",
				JOptionPane.INFORMATION_MESSAGE);
		owner.refreshInfoPanelDisplay();
		owner.updateActiveView();
	}

	private boolean[] getHalfHourAvailability(Date date, String time, int courtNumber) {
		String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(date);
		List<BookingDetails> bookingsInHour = owner.findBookingsForHour(dateStr, time, courtNumber);
		double hourStart = convertTimeSlotToHour(time);

		// Define the intervals for the two half-hour slots
		double firstHalfStart = hourStart;
		double firstHalfEnd = hourStart + 0.5;
		double secondHalfStart = hourStart + 0.5;
		double secondHalfEnd = hourStart + 1.0;

		// Check if any active booking overlaps with the first half
		boolean firstHalfTaken = bookingsInHour.stream()
				.anyMatch(b -> !b.isDidNotShow() && b.getStartTime() < firstHalfEnd && b.getEndTime() > firstHalfStart);

		// Check if any active booking overlaps with the second half
		boolean secondHalfTaken = bookingsInHour.stream().anyMatch(
				b -> !b.isDidNotShow() && b.getStartTime() < secondHalfEnd && b.getEndTime() > secondHalfStart);

		return new boolean[] { firstHalfTaken, secondHalfTaken };
	}

	private void addContactListener(JTextField contactField, JTextField nameField, JTextField emailField) {
		contactField.getDocument().addDocumentListener(new DocumentListener() {
			private Timer timer;

			public void insertUpdate(DocumentEvent e) {
				handleChange();
			}

			public void removeUpdate(DocumentEvent e) {
				handleChange();
			}

			public void changedUpdate(DocumentEvent e) {
				handleChange();
			}

			private void handleChange() {
				if (timer != null)
					timer.stop();
				timer = new Timer(500, (event) -> {
					String phoneNumber = contactField.getText().trim();
					if (phoneNumber.length() >= 10) {
						firebaseDataManager.getContact(phoneNumber, contactData -> {
							if (contactData != null) {
								SwingUtilities.invokeLater(() -> {
									nameField.setText((String) contactData.getOrDefault("name", ""));
									emailField.setText((String) contactData.getOrDefault("email", ""));
								});
							}
						});
					}
				});
				timer.setRepeats(false);
				timer.start();
			}
		});
	}

	private double convertTimeSlotToHour(String timeSlot) {
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

	private boolean areAllSlotsAvailable(Date startDate, Date endDate, List<String> selectedTimes,
			List<Integer> selectedCourts) {
		AtomicBoolean allSlotsAvailable = new AtomicBoolean(true);
		List<Date> dateRange = getDateRange(startDate, endDate);
		CountDownLatch latch = new CountDownLatch(dateRange.size());
		for (Date day : dateRange) {
			String dateStr = new SimpleDateFormat("dd-MM-yyyy").format(day);
			double startTime = convertTimeSlotToHour(selectedTimes.get(0));
			double endTime = convertTimeSlotToHour(selectedTimes.get(selectedTimes.size() - 1)) + 1.0;
			firebaseDataManager.findUnavailableCourts(dateStr, (int) startTime, (int) endTime, unavailableCourts -> {
				for (int court : selectedCourts) {
					if (unavailableCourts.contains(court)) {
						allSlotsAvailable.set(false);
					}
				}
				latch.countDown();
			});
		}
		try {
			latch.await();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		}
		return allSlotsAvailable.get();
	}

	private List<Date> getDateRange(Date startDate, Date endDate) {
		List<Date> dates = new ArrayList<>();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		while (!calendar.getTime().after(endDate)) {
			dates.add(calendar.getTime());
			calendar.add(Calendar.DAY_OF_MONTH, 1);
		}
		return dates;
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

	private double calculateBasePrice(Date selectedDate, String time) {
		if (selectedDate == null || time == null)
			return owner.getPricingSettings().getCourt_weekday_regular();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(selectedDate);
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);
		if (isWeekend)
			return owner.getPricingSettings().getCourt_weekend();
		try {
			Date hourDate = new SimpleDateFormat("hh:mm a").parse(time);
			calendar.setTime(hourDate);
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			double happyHourStart = owner.getPricingSettings().getCourt_happy_hour_start();
			double happyHourEnd = owner.getPricingSettings().getCourt_happy_hour_end();
			if (hour >= happyHourStart && hour < happyHourEnd) {
				return owner.getPricingSettings().getCourt_weekday_happy();
			} else {
				return owner.getPricingSettings().getCourt_weekday_regular();
			}
		} catch (ParseException e) {
			e.printStackTrace();
			return owner.getPricingSettings().getCourt_weekday_regular();
		}
	}
}