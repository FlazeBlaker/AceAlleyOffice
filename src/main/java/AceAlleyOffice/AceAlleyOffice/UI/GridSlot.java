// File: AceAlleyOffice/AceAlleyOffice/UI/GridSlot.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import AceAlleyOffice.AceAlleyOffice.Main.Main;

public class GridSlot extends JPanel {

	private final Main mainApp;
	private final int row;
	private final int col;

	private final JLabel infoLabel;
	private final JButton prevButton;
	private final JButton nextButton;

	private static final Color DNS_COLOR = new Color(108, 117, 125);
	private static final Color PAID_COLOR = new Color(0, 102, 0);
	private static final Color UNPAID_COLOR = new Color(153, 0, 0);
	private static final Color EMPTY_COLOR = new Color(60, 60, 60);

	public GridSlot(Main mainApp, int row, int col) {
		this.mainApp = mainApp;
		this.row = row;
		this.col = col;

		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();

		infoLabel = new JLabel();
		infoLabel.setForeground(Color.WHITE);
		infoLabel.setHorizontalAlignment(SwingConstants.CENTER);
		infoLabel.setVerticalAlignment(SwingConstants.CENTER);

		// --- THIS IS THE FIX (PART 1) ---
		// Create buttons using the new ArrowIcon instead of text characters.
		prevButton = createArrowButton(new ArrowIcon(ArrowIcon.Direction.LEFT, 8, 14, Color.CYAN));
		nextButton = createArrowButton(new ArrowIcon(ArrowIcon.Direction.RIGHT, 8, 14, Color.CYAN));
		// --- END OF FIX ---

		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		gbc.fill = GridBagConstraints.BOTH;
		add(infoLabel, gbc);
		gbc.gridx = 0;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.CENTER;
		add(prevButton, gbc);
		gbc.gridx = 2;
		add(nextButton, gbc);

		prevButton.addActionListener(e -> mainApp.navigateGridSlot(this.row, this.col, -1));
		nextButton.addActionListener(e -> mainApp.navigateGridSlot(this.row, this.col, 1));
	}

	// This is your excellent display logic from before
	public void updateDisplay(List<BookingDetails> bookings, int displayIndex) {
		if (bookings.isEmpty()) {
			infoLabel.setText("Available");
			setBackground(EMPTY_COLOR);
			infoLabel.setForeground(Color.LIGHT_GRAY);
		} else {
			BookingDetails details = bookings.get(displayIndex);
			if (details.isDidNotShow()) {
				setBackground(DNS_COLOR);
			} else {
				boolean isPaid = (details.getCashPaid() + details.getUpiPaid()) >= details.getPrice();
				setBackground(isPaid ? PAID_COLOR : UNPAID_COLOR);
			}
			infoLabel.setForeground(Color.WHITE);

			String bookingSource = "";
			if (details.getMembershipIdUsed() != null && !details.getMembershipIdUsed().isEmpty()) {
				bookingSource = " (Membership)";
			} else if (details.isPlayoSelected()) {
				bookingSource = " (Playo)";
			} else if (details.isKhelomoreSelected()) {
				bookingSource = " (Khelomore)";
			}

			String countIndicator = bookings.size() > 1 ? " (" + (displayIndex + 1) + "/" + bookings.size() + ")" : "";

			infoLabel.setText("<html><center>" + details.getBookerName() + bookingSource + countIndicator + "<br>₹"
					+ String.format("%.0f", details.getPrice()) + "</center></html>");
		}
		// Make the arrow buttons invisible
		prevButton.setVisible(bookings.size() > 1);
		nextButton.setVisible(bookings.size() > 1);
		prevButton.setEnabled(displayIndex > 0);
		nextButton.setEnabled(displayIndex < bookings.size() - 1);
	}

	// Helper method to create styled, small arrow buttons
	private JButton createArrowButton(Icon icon) {
		JButton button = new JButton(icon);
		button.setOpaque(false);
		button.setContentAreaFilled(false);
		button.setBorderPainted(false);
		button.setPreferredSize(new Dimension(30, 40));
		return button;
	}

	public JLabel getInfoLabel() {
		return infoLabel;
	}

	private static class ArrowIcon implements Icon {
		public enum Direction {
			LEFT, RIGHT
		}

		private final Direction direction;
		private final Color color;
		private final int width;
		private final int height;

		public ArrowIcon(Direction direction, int width, int height, Color color) {
			this.direction = direction;
			this.width = width;
			this.height = height;
			this.color = color;
		}

		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			Graphics2D g2d = (Graphics2D) g.create();
			g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setColor(color);

			int midY = y + height / 2;
			Polygon triangle;

			if (direction == Direction.LEFT) {
				triangle = new Polygon(new int[] { x + width, x, x + width }, new int[] { y, midY, y + height }, 3);
			} else { // RIGHT
				triangle = new Polygon(new int[] { x, x + width, x }, new int[] { y, midY, y + height }, 3);
			}
			g2d.fill(triangle);
			g2d.dispose();
		}

		@Override
		public int getIconWidth() {
			return width;
		}

		@Override
		public int getIconHeight() {
			return height;
		}
	}
}