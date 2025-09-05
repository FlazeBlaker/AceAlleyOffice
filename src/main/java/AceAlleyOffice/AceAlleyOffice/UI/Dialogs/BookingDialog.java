// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/BookingDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import AceAlleyOffice.AceAlleyOffice.UI.ModernButton;

public class BookingDialog extends JDialog {

	private boolean isConfirmed = false;
	private final JTextField bookerNamePrompt = new JTextField(20);
	private final JTextField bookerContactPrompt = new JTextField(20);
	private final JTextField bookerEmailPrompt = new JTextField(20);
	private final JRadioButton promptPlayoRadioButton = new JRadioButton("Playo");
	private final JRadioButton promptKhelomoreRadioButton = new JRadioButton("Khelomore");
	private final JRadioButton promptRegularRadioButton = new JRadioButton("Regular", true);
	private final JTextField racketsPrompt = new JTextField("0", 5);
	private final JTextField ballsPrompt = new JTextField("0", 5);

	private final JPanel halfHourPanel;
	private final JRadioButton firstHalfHourRadio;
	private final JRadioButton secondHalfHourRadio;
	private final JRadioButton fullHourRadio;
	private final ButtonGroup halfHourGroup;
	private boolean isHalfHourMode = false;
	private final JButton okButton;

	public BookingDialog(Frame owner, String title) {
		super(owner, title, true);
		getContentPane().setBackground(new Color(45, 45, 45));
		setLayout(new BorderLayout(10, 10));

		JPanel contentPanel = new JPanel(new GridBagLayout());
		contentPanel.setOpaque(false);
		contentPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;

		int y = 0;
		gbc.gridy = y++;
		contentPanel.add(createSectionPanel("Customer Details", createCustomerPanel(), BorderLayout.CENTER), gbc);
		gbc.gridy = y++;
		contentPanel.add(createSectionPanel("Booking Type", createPlatformPanel(), BorderLayout.WEST), gbc);

		// NEW: Use GridLayout for vertical stacking
		halfHourPanel = new JPanel(new GridLayout(0, 1, 0, 5));
		halfHourPanel.setOpaque(false);
		halfHourGroup = new ButtonGroup();

		firstHalfHourRadio = new JRadioButton();
		secondHalfHourRadio = new JRadioButton();
		fullHourRadio = new JRadioButton(); // NEW: Instantiate the full hour button

		halfHourGroup.add(firstHalfHourRadio);
		halfHourGroup.add(secondHalfHourRadio);
		halfHourGroup.add(fullHourRadio); // NEW: Add to the button group

		styleRadioButton(firstHalfHourRadio);
		styleRadioButton(secondHalfHourRadio);
		styleRadioButton(fullHourRadio); // NEW: Style the new button

		halfHourPanel.add(firstHalfHourRadio);
		halfHourPanel.add(secondHalfHourRadio);
		halfHourPanel.add(fullHourRadio); // NEW: Add to the panel
		halfHourPanel.setVisible(false);
		gbc.gridy = y++;
		contentPanel.add(halfHourPanel, gbc);

		gbc.gridy = y++;
		contentPanel.add(createSectionPanel("Rentals", createRentalsPanel(), BorderLayout.CENTER), gbc);

		okButton = new ModernButton("Confirm Booking", new Color(40, 167, 69));
		add(contentPanel, BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.SOUTH);

		ActionListener platformListener = e -> {
			if (isHalfHourMode) {
				halfHourPanel.setVisible(promptRegularRadioButton.isSelected());
				pack();
			}
		};

		promptRegularRadioButton.addActionListener(platformListener);
		promptPlayoRadioButton.addActionListener(platformListener);
		promptKhelomoreRadioButton.addActionListener(platformListener);

		this.pack();
		setLocationRelativeTo(owner);
	}

	public void configureForHalfHourBooking(String firstSlotText, boolean isFirstSlotBooked, String secondSlotText,
			boolean isSecondSlotBooked) {
		this.isHalfHourMode = true;
		halfHourPanel.setVisible(true);
		promptRegularRadioButton.setSelected(true);

		firstHalfHourRadio.setText(firstSlotText);
		firstHalfHourRadio.setEnabled(!isFirstSlotBooked);

		secondHalfHourRadio.setText(secondSlotText);
		secondHalfHourRadio.setEnabled(!isSecondSlotBooked);

		// NEW: Configure the full-hour option
		String fullHourText = firstSlotText.split(" - ")[0] + " - " + secondSlotText.split(" - ")[1];
		fullHourRadio.setText(fullHourText);
		// The full hour is only available if NEITHER half is booked
		fullHourRadio.setEnabled(!isFirstSlotBooked && !isSecondSlotBooked);

		// NEW: Default selection logic
		if (!isFirstSlotBooked && !isSecondSlotBooked) {
			fullHourRadio.setSelected(true); // Default to full hour if available
		} else if (!isFirstSlotBooked) {
			firstHalfHourRadio.setSelected(true);
		} else if (!isSecondSlotBooked) {
			secondHalfHourRadio.setSelected(true);
		}

		okButton.setEnabled(!isFirstSlotBooked || !isSecondSlotBooked);
		this.pack();
	}

	// NEW: Add a getter for the new button
	public JRadioButton getFullHourRadio() {
		return fullHourRadio;
	}

	public boolean showDialog() {
		setVisible(true);
		return isConfirmed;
	}

	private JPanel createCustomerPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		addField(panel, "Booker Name:", bookerNamePrompt, gbc, 0);
		addField(panel, "Phone Number:", bookerContactPrompt, gbc, 1);
		addField(panel, "Email (Optional):", bookerEmailPrompt, gbc, 2);
		return panel;
	}

	private JPanel createPlatformPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		panel.setOpaque(false);
		ButtonGroup group = new ButtonGroup();
		group.add(promptRegularRadioButton);
		group.add(promptPlayoRadioButton);
		group.add(promptKhelomoreRadioButton);
		styleRadioButton(promptRegularRadioButton);
		styleRadioButton(promptPlayoRadioButton);
		styleRadioButton(promptKhelomoreRadioButton);
		panel.add(promptRegularRadioButton);
		panel.add(promptPlayoRadioButton);
		panel.add(promptKhelomoreRadioButton);
		return panel;
	}

	private JPanel createRentalsPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(4, 4, 4, 4);
		gbc.anchor = GridBagConstraints.WEST;
		addField(panel, "Rackets Used:", racketsPrompt, gbc, 0);
		addField(panel, "Balls Used:", ballsPrompt, gbc, 1);
		return panel;
	}

	private JPanel createSectionPanel(String title, JComponent content, String alignment) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);
		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(150, 150, 150)),
				title);
		border.setTitleFont(new Font("Arial", Font.BOLD, 16));
		border.setTitleColor(new Color(255, 200, 0));
		panel.setBorder(BorderFactory.createCompoundBorder(border, new EmptyBorder(10, 10, 10, 10)));
		panel.add(content, alignment);
		return panel;
	}

	private void addField(JPanel p, String label, JComponent comp, GridBagConstraints gbc, int y) {
		gbc.gridx = 0;
		gbc.gridy = y;
		gbc.weightx = 0;
		gbc.fill = GridBagConstraints.NONE;
		p.add(styleLabel(new JLabel(label)), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		styleTextField((JTextField) comp);
		p.add(comp, gbc);
	}

	private void styleRadioButton(AbstractButton button) {
		button.setOpaque(false);
		button.setForeground(Color.WHITE);
		button.setFont(new Font("Arial", Font.PLAIN, 14));
	}

	private JLabel styleLabel(JLabel label) {
		label.setForeground(Color.WHITE);
		label.setFont(new Font("Arial", Font.PLAIN, 14));
		return label;
	}

	private void styleTextField(JTextField field) {
		Border fieldBorder = BorderFactory.createCompoundBorder(new LineBorder(new Color(100, 100, 100), 1),
				new EmptyBorder(5, 5, 5, 5));
		field.setFont(new Font("Arial", Font.PLAIN, 14));
		field.setBackground(new Color(30, 30, 30));
		field.setForeground(Color.WHITE);
		field.setCaretColor(Color.WHITE);
		field.setBorder(fieldBorder);
	}

	private JPanel createButtonPanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
		panel.setOpaque(false);
		JButton cancelButton = new ModernButton("Cancel", new Color(108, 117, 125));
		okButton.addActionListener(e -> {
			isConfirmed = true;
			setVisible(false);
		});
		cancelButton.addActionListener(e -> {
			isConfirmed = false;
			setVisible(false);
		});
		panel.add(cancelButton);
		panel.add(okButton);
		return panel;
	}

	// Getters
	public boolean isHalfHourMode() {
		return isHalfHourMode;
	}

	public JRadioButton getPromptRegularRadioButton() {
		return promptRegularRadioButton;
	}

	public JRadioButton getFirstHalfHourRadio() {
		return firstHalfHourRadio;
	}

	public JRadioButton getSecondHalfHourRadio() {
		return secondHalfHourRadio;
	}

	public JTextField getBookerNamePrompt() {
		return bookerNamePrompt;
	}

	public JTextField getBookerContactPrompt() {
		return bookerContactPrompt;
	}

	public JTextField getBookerEmailPrompt() {
		return bookerEmailPrompt;
	}

	public JRadioButton getPromptPlayoRadioButton() {
		return promptPlayoRadioButton;
	}

	public JRadioButton getPromptKhelomoreRadioButton() {
		return promptKhelomoreRadioButton;
	}

	public JTextField getRacketsPrompt() {
		return racketsPrompt;
	}

	public JTextField getBallsPrompt() {
		return ballsPrompt;
	}
}