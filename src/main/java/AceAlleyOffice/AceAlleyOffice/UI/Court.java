// File: Components/Court.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import AceAlleyOffice.AceAlleyOffice.Interfaces.CourtBookingListener;

public class Court extends JPanel {
	private int courtNumber;
	private boolean isBooked;
	private String bookerName;
	private double totalAmount;
	private CourtBookingListener listener;
	private boolean isFullyPaid;
	private boolean isShiftModeActive = false;
	private boolean isBulkBookingModeActive = false;
	private JLabel bookerNameLabel;
	private JLabel paymentStatusLabel;
	private JLabel totalAmountLabel;
	private JLabel courtAmountLabel;
	private JLabel rentalAmountLabel;
	private JLabel cafeAmountLabel;
	private boolean isDns; // <-- NEW state variable
	private JButton bookButton;
	private JButton cancelButton;
	private JButton infoButton;
	private JButton addBookingButton;

	// --- NEW: Paginator Components ---
	private JButton previousBookingButton;
	private JButton nextBookingButton;
	private JLabel bookingPaginatorLabel;

	private final Color AVAILABLE_COLOR = Color.LIGHT_GRAY;
	private final Color BOOKED_COLOR = new Color(255, 160, 122);
	private final Color PAID_COLOR = new Color(152, 251, 152);
	private final Color DNS_COLOR = new Color(66, 135, 245); // <-- NEW Blue color for DNS

	public Court(int courtNumber, CourtBookingListener listener) {
		this.courtNumber = courtNumber;
		this.listener = listener;
		this.isBooked = false;
		this.bookerName = "";
		this.totalAmount = 0.0;
		this.isFullyPaid = false;

		setLayout(new GridBagLayout());
		setBackground(Color.LIGHT_GRAY);
		setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		setPreferredSize(new Dimension(375, 325));
		GridBagConstraints gbc = new GridBagConstraints();

		// --- Paginator Label is centered at the top (Arrows are no longer here) ---
		bookingPaginatorLabel = new JLabel("");
		bookingPaginatorLabel.setFont(new Font("Arial", Font.BOLD, 14));
		bookingPaginatorLabel.setForeground(Color.BLACK);
		bookingPaginatorLabel.setHorizontalAlignment(JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridwidth = 3;
		gbc.insets = new Insets(5, 5, 2, 5);
		add(bookingPaginatorLabel, gbc);
		gbc.gridwidth = 1;

		// --- Add Booking Button (+) ---
		addBookingButton = new ModernButton("+", new Color(30, 144, 255));
		addBookingButton.addActionListener(e -> {
			if (listener != null)
				listener.onAddBookingToSlot(courtNumber);
		});
		gbc.gridx = 2;
		gbc.gridy = 1; // Positioned below the paginator
		gbc.anchor = GridBagConstraints.NORTHEAST;
		add(addBookingButton, gbc);

		// Reset anchor for other components
		gbc.anchor = GridBagConstraints.CENTER;
		gbc.insets = new Insets(2, 2, 2, 2);

		paymentStatusLabel = new JLabel();
		paymentStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
		paymentStatusLabel.setHorizontalAlignment(JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.gridwidth = 2;
		gbc.insets = new Insets(5, 5, 2, 5);
		add(paymentStatusLabel, gbc);

		bookerNameLabel = new JLabel("");
		bookerNameLabel.setFont(new Font("Roboto", Font.BOLD, 18));
		bookerNameLabel.setHorizontalAlignment(JLabel.CENTER);
		bookerNameLabel.setForeground(Color.black);
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 3;
		add(bookerNameLabel, gbc);

		JLabel courtNumLabel = new JLabel("Court " + courtNumber);
		courtNumLabel.setFont(new Font("Arial", Font.BOLD, 28));
		courtNumLabel.setForeground(Color.BLACK);
		courtNumLabel.setHorizontalAlignment(JLabel.CENTER);
		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.gridwidth = 3;
		add(courtNumLabel, gbc);

		totalAmountLabel = new JLabel("Total: ₹" + String.format("%.2f", totalAmount));
		totalAmountLabel.setFont(new Font("Arial", Font.BOLD, 20));
		totalAmountLabel.setHorizontalAlignment(JLabel.CENTER);
		gbc.gridy = 4;
		add(totalAmountLabel, gbc);

		Font breakdownFont = new Font("Arial", Font.PLAIN, 14);
		Color breakdownColor = Color.DARK_GRAY;

		courtAmountLabel = new JLabel("Court: ₹0.00");
		courtAmountLabel.setFont(breakdownFont);
		courtAmountLabel.setForeground(breakdownColor);
		gbc.gridy = 5;
		gbc.gridwidth = 3;
		add(courtAmountLabel, gbc);

		rentalAmountLabel = new JLabel("Rentals: ₹0.00");
		rentalAmountLabel.setFont(breakdownFont);
		rentalAmountLabel.setForeground(breakdownColor);
		gbc.gridy = 6;
		add(rentalAmountLabel, gbc);

		cafeAmountLabel = new JLabel("Cafe: ₹0.00");
		cafeAmountLabel.setFont(breakdownFont);
		cafeAmountLabel.setForeground(breakdownColor);
		gbc.gridy = 7;
		add(cafeAmountLabel, gbc);

		gbc.gridy = 8;
		gbc.gridwidth = 3;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		add(createActionPanel(), gbc);

		updateBookingState(false, false, false);
	}

	// --- NEW: This helper method creates the bottom row of 5 buttons ---
	private JPanel createActionPanel() {
		// --- THIS IS THE FIX ---
		// We've changed GridLayout to FlowLayout. FlowLayout respects each button's
		// natural size instead of stretching them to fill the panel.
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); // Centered, 5px horizontal gap
		panel.setOpaque(false);

		// The buttons are created in the same way
		previousBookingButton = new ModernButton("<", new Color(80, 80, 80));
		previousBookingButton.addActionListener(e -> {
			// Tell the Main class to navigate backwards (-1) for this specific court
			listener.onNavigateBooking(this.courtNumber, -1);
		});
		bookButton = new ModernButton("Book", new Color(60, 179, 113));
		bookButton.addActionListener(e -> listener.onBookCourt(courtNumber));

		cancelButton = new ModernButton("Cancel", new Color(220, 20, 60));
		cancelButton.addActionListener(e -> listener.onCancelCourt(courtNumber));

		infoButton = new ModernButton("Info", new Color(30, 144, 255));
		infoButton.addActionListener(e -> listener.onInfoCourt(courtNumber));

		nextBookingButton = new ModernButton(">", new Color(80, 80, 80));
		nextBookingButton.addActionListener(e -> {
			// Tell the Main class to navigate forwards (+1) for this specific court
			listener.onNavigateBooking(this.courtNumber, 1);
		});
		// Add the buttons to the panel
		panel.add(previousBookingButton);
		panel.add(bookButton);
		panel.add(cancelButton);
		panel.add(infoButton);
		panel.add(nextBookingButton);

		return panel;
	}

	/**
	 * Controls the visibility and state of the booking navigation arrows.
	 * 
	 * @param displayedIndex The index of the currently shown booking (0-based).
	 * @param totalInSlot    The total number of bookings for this slot.
	 */
	// In Court.java
	// In Court.java

	// In Court.java

	public void setPaginatorInfo(int currentIndex, int totalBookings) {
	    if (totalBookings > 1) {
	        // If there's more than one booking, show the paginator
	        bookingPaginatorLabel.setText((currentIndex + 1) + " / " + totalBookings);
	        bookingPaginatorLabel.setVisible(true);
	        previousBookingButton.setVisible(true);
	        nextBookingButton.setVisible(true);

	        // --- THIS IS THE FIX ---
	        // Enable/disable buttons based on the current position
	        previousBookingButton.setEnabled(currentIndex > 0);
	        nextBookingButton.setEnabled(currentIndex < totalBookings - 1);
	        
	    } else {
	        // Otherwise, hide all paginator controls
	        bookingPaginatorLabel.setVisible(false);
	        previousBookingButton.setVisible(false);
	        nextBookingButton.setVisible(false);
	    }
	}

	private void updateButtonStates() {
		boolean controlsEnabled = !isShiftModeActive && !isBulkBookingModeActive;
		bookButton.setEnabled(!isBooked && controlsEnabled);
		cancelButton.setEnabled(isBooked && controlsEnabled);
		infoButton.setEnabled(isBooked && controlsEnabled);
		addBookingButton.setEnabled(isBooked && controlsEnabled);

		// Let setPaginatorInfo handle the detailed logic for arrow buttons
		if (!isBooked || !controlsEnabled) {
			previousBookingButton.setEnabled(false);
			nextBookingButton.setEnabled(false);
		}
	}

	public void updateBookingState(boolean isBooked, boolean isPaid, boolean isDns) {
		this.isBooked = isBooked;
		this.isFullyPaid = isPaid;
		this.isDns = isDns;

		if (!isBooked) {
			setBookerName("");
			setTotalAmount(0.0);
			setCourtAmount(0.0, "");
			setRentalAmount(0.0, 0.0);
			setCafeAmount(0.0);
			// Hide the paginator label when there's no booking
			bookingPaginatorLabel.setVisible(false);
		}
		updateButtonStates();
		updateColors();
	}

	// --- Other methods (setShiftMode, setBookerName, etc.) remain unchanged ---
	public void setShiftMode(boolean isActive) {
		this.isShiftModeActive = isActive;
		updateButtonStates();
	}

	public void setBookerName(String name) {
		this.bookerName = (name == null) ? "" : name;
		this.bookerNameLabel.setText(this.bookerName);
	}

	public void setBulkBookingMode(boolean isActive) {
		this.isBulkBookingModeActive = isActive;
		updateButtonStates();
	}

	public void setTotalAmount(double totalAmount) {
		this.totalAmount = totalAmount;
		totalAmountLabel.setText("Total: ₹" + String.format("%.2f", totalAmount));
		updateColors();
	}

	public void setCourtAmount(double amount, String sourceText) {
		courtAmountLabel.setText("Court: ₹" + String.format("%.2f", amount) + sourceText);
	}

	public void setRentalAmount(double racketAmount, double ballAmount) {
		double totalRental = racketAmount + ballAmount;
		rentalAmountLabel.setText("Rentals: ₹" + String.format("%.2f", totalRental));
	}

	public void setCafeAmount(double amount) {
		cafeAmountLabel.setText("Cafe: ₹" + String.format("%.2f", amount));
	}

	public void updatePaymentStatus(boolean isPaid) {
		this.isFullyPaid = isPaid;
		updateColors();
	}

	private void updateColors() {
		if (isBooked) {
			if (isDns) {
				setBackground(DNS_COLOR);
			} else {
				setBackground(isFullyPaid ? PAID_COLOR : BOOKED_COLOR);
			}
			totalAmountLabel.setForeground(Color.BLACK);
			bookerNameLabel.setVisible(true);
			courtAmountLabel.setVisible(true);
			rentalAmountLabel.setVisible(true);
			cafeAmountLabel.setVisible(true);
		} else {
			setBackground(AVAILABLE_COLOR);
			totalAmountLabel.setForeground(Color.BLACK);
			totalAmountLabel.setText("Total: ₹0.00");
			bookerNameLabel.setVisible(false);
			setCourtAmount(0.0, "");
			setRentalAmount(0.0, 0.0);
			cafeAmountLabel.setText("Cafe: ₹0.00");
			courtAmountLabel.setVisible(false);
			rentalAmountLabel.setVisible(false);
			cafeAmountLabel.setVisible(false);
		}
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// This part draws the white court lines, it is unchanged
		g2d.setColor(Color.WHITE);
		g2d.drawRect(20, 75, getWidth() - 40, getHeight() - 130);
		g2d.drawLine(getWidth() / 2, 75, getWidth() / 2, getHeight() - 55);

		if (isBooked) {
			String statusText;
			Color statusColor;

			// Determine the correct status text and color based on DNS, then payment
			if (isDns) {
				statusText = "Did Not Show";
				statusColor = Color.WHITE; // White text looks best on the blue DNS background
			} else {
				statusText = isFullyPaid ? "Payment Completed" : "Payment Pending";
				statusColor = isFullyPaid ? new Color(0, 100, 0) : Color.RED;
			}

			g2d.setColor(statusColor);
			g2d.setFont(new Font("Arial", Font.BOLD, 20));

			// Center the text horizontally
			FontMetrics fm = g2d.getFontMetrics();
			int textWidth = fm.stringWidth(statusText);
			int x = (getWidth() - textWidth) / 2;

			// --- THIS IS THE FIX ---
			// Increased the Y-coordinate from 50 to 65 to move the text lower.
			int y = 65;

			g2d.drawString(statusText, x, y);
		}
	}
}