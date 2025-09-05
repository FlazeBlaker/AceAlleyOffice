// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/MembershipDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.MembershipManager;
import AceAlleyOffice.AceAlleyOffice.Core.User;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.Membership;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPackage;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPurchase;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;

public class MembershipDialog extends JDialog implements MembershipManager.MembershipUpdateListener {

	private final MembershipManager membershipManager;
	private final FirebaseDataManager firebaseDataManager;
	private final User currentUser;
	private JTabbedPane tabbedPane;
	private JTextField nameField, phoneField, emailField;
	private JComboBox<MembershipPackage> packageComboBox;
	private JLabel amountLabel;
	private JCheckBox paymentDoneCheckbox;
	private JRadioButton cashRadio, upiRadio;
	private JButton addMemberButton;
	private JTextField searchField;
	private JList<Membership> searchResultsList;
	private DefaultListModel<Membership> searchListModel;
	private JLabel memberNameLabel, memberPhoneLabel, memberEmailLabel, memberHoursLabel;
	private JTextArea memberExpiryDetailsArea;
	private Membership selectedMember;
	private JButton addPackageButton, removeHoursButton, editButton;
	private final Main mainApp;

	public MembershipDialog(Main owner, MembershipManager manager, FirebaseDataManager firebaseDataManager,
			List<MembershipPackage> packages, User currentUser) {
		super(owner, "Membership Management", true);
		this.mainApp = owner;
		this.membershipManager = manager;
		this.firebaseDataManager = firebaseDataManager;
		this.currentUser = currentUser;
		this.membershipManager.setListener(this);
		setSize(750, 600);
		setLocationRelativeTo(owner);
		getContentPane().setBackground(new Color(45, 45, 45));

		tabbedPane = new JTabbedPane();
		tabbedPane.addTab("Check Existing Member", createCheckMemberPanel());
		tabbedPane.addTab("Add New Member", createAddMemberPanel(packages));
		styleTabbedPane(tabbedPane);

		add(tabbedPane);
	}

	@Override
	public void onMembershipsUpdated() {
		SwingUtilities.invokeLater(() -> {
			String selectedMemberId = null;
			if (selectedMember != null) {
				selectedMemberId = selectedMember.getMemberId();
			}
			performSearch();
			if (selectedMemberId != null) {
				for (int i = 0; i < searchListModel.getSize(); i++) {
					if (searchListModel.getElementAt(i).getMemberId().equals(selectedMemberId)) {
						searchResultsList.setSelectedIndex(i);
						break;
					}
				}
			}
			updateDetailsPanel();
		});
	}

	private JPanel createAddMemberPanel(List<MembershipPackage> packages) {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBackground(new Color(45, 45, 45));
		panel.setBorder(new EmptyBorder(15, 15, 15, 15));
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(8, 5, 8, 5);
		gbc.anchor = GridBagConstraints.WEST;
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(styleLabel(new JLabel("Name:")), gbc);
		gbc.gridx = 1;
		nameField = new JTextField(25);
		styleTextField(nameField);
		panel.add(nameField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 1;
		panel.add(styleLabel(new JLabel("Phone Number:")), gbc);
		gbc.gridx = 1;
		phoneField = new JTextField(25);
		styleTextField(phoneField);
		panel.add(phoneField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 2;
		panel.add(styleLabel(new JLabel("Email (Optional):")), gbc);
		gbc.gridx = 1;
		emailField = new JTextField(25);
		styleTextField(emailField);
		panel.add(emailField, gbc);
		gbc.gridx = 0;
		gbc.gridy = 3;
		panel.add(styleLabel(new JLabel("Package:")), gbc);

		packageComboBox = new JComboBox<>(packages.toArray(new MembershipPackage[0]));
		styleComboBox(packageComboBox);
		amountLabel = styleLabel(new JLabel(""));
		packageComboBox.addActionListener(e -> {
			MembershipPackage selected = (MembershipPackage) packageComboBox.getSelectedItem();
			if (selected != null) {
				amountLabel.setText(String.format("Amount: ₹%,.0f", selected.getPrice()));
			}
		});
		if (packageComboBox.getActionListeners().length > 0) {
			packageComboBox.getActionListeners()[0].actionPerformed(null);
		}

		JPanel packagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
		packagePanel.setOpaque(false);
		packagePanel.add(packageComboBox);
		packagePanel.add(Box.createHorizontalStrut(20));
		packagePanel.add(amountLabel);
		gbc.gridx = 1;
		panel.add(packagePanel, gbc);
		gbc.gridx = 1;
		gbc.gridy = 4;
		paymentDoneCheckbox = new JCheckBox("Payment Received");
		styleCheckBox(paymentDoneCheckbox);
		panel.add(paymentDoneCheckbox, gbc);

		JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
		paymentMethodPanel.setOpaque(false);
		cashRadio = new JRadioButton("Cash");
		styleCheckBox(cashRadio);
		upiRadio = new JRadioButton("UPI");
		styleCheckBox(upiRadio);
		ButtonGroup group = new ButtonGroup();
		group.add(cashRadio);
		group.add(upiRadio);
		paymentMethodPanel.add(cashRadio);
		paymentMethodPanel.add(upiRadio);
		cashRadio.setVisible(false);
		upiRadio.setVisible(false);
		gbc.gridy = 5;
		panel.add(paymentMethodPanel, gbc);
		gbc.gridy = 6;
		gbc.anchor = GridBagConstraints.CENTER;
		addMemberButton = new JButton("Add Member");
		stylePrimaryButton(addMemberButton, new Color(90, 90, 90));
		panel.add(addMemberButton, gbc);

		ActionListener paymentListener = e -> {
			boolean ready = !nameField.getText().trim().isEmpty() && !phoneField.getText().trim().isEmpty()
					&& paymentDoneCheckbox.isSelected() && (cashRadio.isSelected() || upiRadio.isSelected());
			addMemberButton.setEnabled(ready);
			addMemberButton.setBackground(ready ? new Color(60, 179, 113) : new Color(90, 90, 90));
		};

		DocumentListener fieldListener = (SimpleDocumentListener) e -> paymentListener.actionPerformed(null);
		nameField.getDocument().addDocumentListener(fieldListener);
		phoneField.getDocument().addDocumentListener(new DocumentListener() {
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
				paymentListener.actionPerformed(null);
				if (timer != null)
					timer.stop();
				timer = new Timer(500, (event) -> {
					String phoneNumber = phoneField.getText().trim();
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
		paymentDoneCheckbox.addActionListener(e -> {
			boolean selected = paymentDoneCheckbox.isSelected();
			cashRadio.setVisible(selected);
			upiRadio.setVisible(selected);
			if (!selected)
				group.clearSelection();
			paymentListener.actionPerformed(null);
		});
		cashRadio.addActionListener(paymentListener);
		upiRadio.addActionListener(paymentListener);
		addMemberButton.addActionListener(e -> handleAddMember());
		return panel;
	}

	private JPanel createCheckMemberPanel() {
		JPanel panel = new JPanel(new BorderLayout(10, 10));
		panel.setBackground(new Color(45, 45, 45));
		panel.setBorder(new EmptyBorder(10, 10, 10, 10));

		JPanel searchPanel = new JPanel(new BorderLayout(10, 0));
		searchPanel.setOpaque(false);
		searchPanel.add(styleLabel(new JLabel("Search Name/Phone:")), BorderLayout.WEST);
		searchField = new JTextField();
		styleTextField(searchField);
		searchPanel.add(searchField, BorderLayout.CENTER);
		panel.add(searchPanel, BorderLayout.NORTH);

		searchListModel = new DefaultListModel<>();
		searchResultsList = new JList<>(searchListModel);
		styleList(searchResultsList);
		JScrollPane listScrollPane = new JScrollPane(searchResultsList);
		listScrollPane.setBorder(new LineBorder(new Color(80, 80, 80)));

		JPanel detailsPanel = new JPanel(new GridBagLayout());
		styleTitledBorder(detailsPanel, "Member Details");
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(6, 10, 6, 10);
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.weightx = 1.0;
		gbc.gridwidth = 2;

		memberNameLabel = styleLabel(new JLabel("Name: "));
		memberPhoneLabel = styleLabel(new JLabel("Phone: "));
		memberEmailLabel = styleLabel(new JLabel("Email: "));
		memberHoursLabel = styleLabel(new JLabel("Hours Remaining: "));
		memberHoursLabel.setFont(memberHoursLabel.getFont().deriveFont(Font.BOLD, 16f));
		memberHoursLabel.setForeground(new Color(32, 190, 120));

		gbc.gridy = 0;
		detailsPanel.add(memberNameLabel, gbc);
		gbc.gridy = 1;
		detailsPanel.add(memberPhoneLabel, gbc);
		gbc.gridy = 2;
		detailsPanel.add(memberEmailLabel, gbc);
		gbc.gridy = 3;
		gbc.insets = new Insets(15, 10, 15, 10);
		detailsPanel.add(memberHoursLabel, gbc);
		gbc.gridy = 4;
		gbc.insets = new Insets(0, 10, 10, 10);
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		memberExpiryDetailsArea = new JTextArea(4, 20);
		memberExpiryDetailsArea.setEditable(false);
		memberExpiryDetailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		memberExpiryDetailsArea.setBackground(new Color(50, 50, 50));
		memberExpiryDetailsArea.setForeground(Color.LIGHT_GRAY);
		memberExpiryDetailsArea.setBorder(null);
		JScrollPane expiryScrollPane = new JScrollPane(memberExpiryDetailsArea);
		expiryScrollPane.setBorder(new LineBorder(new Color(80, 80, 80)));
		detailsPanel.add(expiryScrollPane, gbc);

		// --- THIS IS THE FIX ---
		// Create a panel for all buttons, but only add admin buttons if the role
		// matches.
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
		buttonPanel.setOpaque(false);

		// "Add Package" button is available to all users (staff and admin)
		addPackageButton = new JButton("Add Package");
		styleButton(addPackageButton, new Color(0, 123, 255));
		addPackageButton.addActionListener(e -> handleAddPackage());
		buttonPanel.add(addPackageButton);

		// "Remove Hours" and "Edit Details" are admin-only
		if (currentUser != null && "admin".equals(currentUser.getRole())) {
			removeHoursButton = new JButton("Remove Hours");
			styleButton(removeHoursButton, new Color(240, 173, 78));
			removeHoursButton.addActionListener(e -> handleRemoveHours());

			editButton = new JButton("Edit Details");
			styleButton(editButton, new Color(110, 110, 110));
			editButton.addActionListener(e -> handleEditDetails());

			buttonPanel.add(removeHoursButton);
			buttonPanel.add(editButton);
		}

		gbc.gridy = 5;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(6, 10, 6, 10);
		detailsPanel.add(buttonPanel, gbc);
		// --- END OF FIX ---

		JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScrollPane, detailsPanel);
		splitPane.setDividerLocation(250);
		splitPane.setOpaque(false);
		splitPane.setBorder(null);
		panel.add(splitPane, BorderLayout.CENTER);

		searchResultsList.addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting())
				updateDetailsPanel();
		});
		searchField.getDocument().addDocumentListener((SimpleDocumentListener) e -> performSearch());

		loadAllMembersToList();
		return panel;
	}

	private void handleAddMember() {
		String name = nameField.getText().trim();
		String phone = phoneField.getText().trim();
		if (name.isEmpty() || phone.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Name and Phone cannot be empty.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		boolean memberExists = membershipManager.findMembersByName(phone).stream()
				.anyMatch(member -> member.getPhoneNumber().equals(phone));
		if (memberExists) {
			JOptionPane.showMessageDialog(this,
					"A member with this phone number already exists.\nPlease use the 'Check Existing Member' tab to manage them.",
					"Member Exists", JOptionPane.ERROR_MESSAGE);
			return;
		}
		MembershipPackage selectedPackage = (MembershipPackage) packageComboBox.getSelectedItem();
		if (selectedPackage == null) {
			JOptionPane.showMessageDialog(this, "Please select a package.", "Input Error", JOptionPane.ERROR_MESSAGE);
			return;
		}
		membershipManager.addMember(name, phone, emailField.getText().trim(), selectedPackage);
		MembershipPurchase purchase = new MembershipPurchase(name, phone, selectedPackage.getHours(),
				selectedPackage.getPrice());
		firebaseDataManager.addMembershipPurchase(purchase);
		JOptionPane.showMessageDialog(this, "Member added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
		tabbedPane.setSelectedIndex(0);
		searchField.setText(phone);
		performSearch();
		clearAddMemberForm();
	}

	private void handleAddPackage() {
		if (selectedMember == null)
			return;
		if (selectedMember.getTotalHoursRemaining() > 0) {
			JOptionPane.showMessageDialog(this,
					"This member already has an active package.\nNew packages can only be added once the existing balance is zero.",
					"Active Package Exists", JOptionPane.WARNING_MESSAGE);
			return;
		}
		if (!verifyPassword())
			return;
		List<MembershipPackage> packages = mainApp.getMembershipPackages();
		if (packages == null || packages.isEmpty()) {
			JOptionPane.showMessageDialog(this, "No membership packages have been configured.", "No Packages",
					JOptionPane.ERROR_MESSAGE);
			return;
		}
		JComboBox<MembershipPackage> packageComboBox = new JComboBox<>(packages.toArray(new MembershipPackage[0]));
		styleComboBox(packageComboBox);
		int result = JOptionPane.showConfirmDialog(this, packageComboBox, "Select a Package to Add",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			MembershipPackage selectedPackage = (MembershipPackage) packageComboBox.getSelectedItem();
			if (selectedPackage != null) {
				membershipManager.addPackageToMember(selectedMember.getMemberId(), selectedPackage);

				// Create a record of the purchase
				MembershipPurchase purchase = new MembershipPurchase(selectedMember.getName(),
						selectedMember.getPhoneNumber(), selectedPackage.getHours(), selectedPackage.getPrice());
				firebaseDataManager.addMembershipPurchase(purchase);

				JOptionPane.showMessageDialog(this, "Package added successfully!", "Success",
						JOptionPane.INFORMATION_MESSAGE);
			}
		}
	}

	private void handleRemoveHours() {
		if (selectedMember == null)
			return;
		if (!verifyPassword())
			return;
		String hoursStr = JOptionPane.showInputDialog(this, "Enter hours to remove:");
		try {
			double hours = Double.parseDouble(hoursStr);
			if (hours <= 0 || selectedMember.getTotalHoursRemaining() < hours) {
				throw new NumberFormatException();
            }
			membershipManager.deductHoursForBooking(selectedMember.getMemberId(), hours);
            
			JOptionPane.showMessageDialog(this, "Hours updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(this, "Invalid number or not enough hours to remove.", "Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void handleEditDetails() {
		if (selectedMember == null)
			return;
		if (!verifyPassword())
			return;
		JTextField editName = new JTextField(selectedMember.getName());
		JTextField editPhone = new JTextField(selectedMember.getPhoneNumber());
		JTextField editEmail = new JTextField(selectedMember.getEmail());
		int option = JOptionPane.showConfirmDialog(this,
				new Object[] { "Name:", editName, "Phone:", editPhone, "Email:", editEmail }, "Edit Details",
				JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			membershipManager.editMember(selectedMember.getMemberId(), editName.getText(), editPhone.getText(),
					editEmail.getText());
			JOptionPane.showMessageDialog(this, "Details updated.", "Success", JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void performSearch() {
		String query = searchField.getText().trim();
		searchListModel.clear();
		if (query.isEmpty()) {
			loadAllMembersToList();
		} else {
			membershipManager.findMembersByName(query).forEach(searchListModel::addElement);
		}
	}

	private void loadAllMembersToList() {
		searchListModel.clear();
		membershipManager.getAllMembers().forEach(searchListModel::addElement);
	}

	private void updateDetailsPanel() {
		selectedMember = searchResultsList.getSelectedValue();
		if (selectedMember != null) {
			selectedMember = membershipManager.getMember(selectedMember.getMemberId());
			if (selectedMember == null) {
				loadAllMembersToList();
				return;
			}
			memberNameLabel.setText("Name: " + selectedMember.getName());
			memberPhoneLabel.setText("Phone: " + selectedMember.getPhoneNumber());
			memberEmailLabel.setText("Email: " + selectedMember.getEmail());
			memberHoursLabel
					.setText("Hours Remaining: " + String.format("%.1f", selectedMember.getTotalHoursRemaining()));
			memberExpiryDetailsArea.setText(getExpiryDetailsString(selectedMember));
			memberExpiryDetailsArea.setCaretPosition(0);

			// --- THIS IS THE FIX ---
			// The logic for enabling/disabling buttons is now split.
			boolean hasActiveHours = selectedMember.getTotalHoursRemaining() > 0;

			// This button exists for all users, so no null check is needed here.
			addPackageButton.setEnabled(!hasActiveHours);

			// These buttons only exist for admins, so we must check if they are null.
			if (removeHoursButton != null && editButton != null) {
				removeHoursButton.setEnabled(hasActiveHours);
				editButton.setEnabled(true);
			}
		} else {
			memberNameLabel.setText("Name: ");
			memberPhoneLabel.setText("Phone: ");
			memberEmailLabel.setText("Email: ");
			memberHoursLabel.setText("Hours Remaining: ");
			memberExpiryDetailsArea.setText("");

			// Disable all buttons if nothing is selected.
			addPackageButton.setEnabled(false);
			if (removeHoursButton != null && editButton != null) {
				removeHoursButton.setEnabled(false);
				editButton.setEnabled(false);
			}
		}
	}

	private String getExpiryDetailsString(Membership member) {
		List<Membership.HourPackage> packages = member.getHourPackages();
		if (packages == null || packages.isEmpty())
			return "No active hour packages.";
		StringBuilder sb = new StringBuilder("Active Packages (Oldest First):\n---------------------------------\n");
		SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy");
		packages.stream().filter(pkg -> pkg.getHours() > 0).forEach(pkg -> sb
				.append(String.format("- %.1f hrs, expires %s\n", pkg.getHours(), sdf.format(pkg.getExpiryDate()))));
		return sb.toString();
	}

	private void clearAddMemberForm() {
		nameField.setText("");
		phoneField.setText("");
		emailField.setText("");
		packageComboBox.setSelectedIndex(0);
		paymentDoneCheckbox.setSelected(false);
	}

	private boolean verifyPassword() {
		JPasswordField passwordField = new JPasswordField();
		int option = JOptionPane.showConfirmDialog(this, passwordField, "Enter Admin Password",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (option != JOptionPane.OK_OPTION)
			return false;

		String correctPassword = mainApp.getPricingSettings().getAdmin_password();
		if (new String(passwordField.getPassword()).equals(correctPassword)) {
			return true;
		} else {
			JOptionPane.showMessageDialog(this, "Incorrect password.", "Access Denied", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void styleTitledBorder(JPanel panel, String title) {
		panel.setOpaque(false);
		TitledBorder titled = BorderFactory.createTitledBorder(new LineBorder(new Color(80, 80, 80)), title);
		titled.setTitleFont(new Font("Arial", Font.BOLD, 16));
		titled.setTitleColor(new Color(255, 200, 0));
		panel.setBorder(BorderFactory.createCompoundBorder(titled, new EmptyBorder(5, 5, 5, 5)));
	}

	private JLabel styleLabel(JLabel label) {
		label.setForeground(new Color(200, 200, 200));
		label.setFont(new Font("Arial", Font.BOLD, 14));
		return label;
	}

	private void styleTextField(JTextField field) {
		field.setFont(new Font("Arial", Font.PLAIN, 14));
		field.setBackground(new Color(30, 30, 30));
		field.setForeground(Color.WHITE);
		field.setCaretColor(Color.WHITE);
		field.setBorder(
				BorderFactory.createCompoundBorder(new LineBorder(new Color(80, 80, 80)), new EmptyBorder(5, 5, 5, 5)));
	}

	private void styleCheckBox(AbstractButton checkBox) {
		checkBox.setFont(new Font("Arial", Font.PLAIN, 14));
		checkBox.setForeground(Color.WHITE);
		checkBox.setOpaque(false);
	}

	private void styleButton(JButton button, Color bgColor) {
		button.setFont(new Font("Arial", Font.BOLD, 12));
		button.setBackground(bgColor);
		button.setForeground(Color.WHITE);
		button.setFocusPainted(false);
		button.setBorder(new EmptyBorder(8, 15, 8, 15));
	}

	private void stylePrimaryButton(JButton button, Color disabledColor) {
		styleButton(button, disabledColor);
		button.setEnabled(false);
	}

	private void styleComboBox(JComboBox<MembershipPackage> combo) {
		combo.setFont(new Font("Arial", Font.PLAIN, 14));
		combo.setBackground(new Color(50, 50, 50));
		combo.setForeground(Color.WHITE);
	}

	private void styleList(JList<Membership> list) {
		list.setBackground(new Color(30, 30, 30));
		list.setForeground(Color.WHITE);
		list.setSelectionBackground(new Color(0, 123, 255));
		list.setSelectionForeground(Color.WHITE);
		list.setFont(new Font("Arial", Font.PLAIN, 14));
	}

	private void styleTabbedPane(JTabbedPane pane) {
		pane.setOpaque(false);
		pane.setForeground(Color.BLACK);
		pane.setFont(new Font("Arial", Font.BOLD, 12));
	}

	@FunctionalInterface
	private interface SimpleDocumentListener extends DocumentListener {
		void update(DocumentEvent e);

		@Override
		default void insertUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		default void removeUpdate(DocumentEvent e) {
			update(e);
		}

		@Override
		default void changedUpdate(DocumentEvent e) {
			update(e);
		}
	}
}