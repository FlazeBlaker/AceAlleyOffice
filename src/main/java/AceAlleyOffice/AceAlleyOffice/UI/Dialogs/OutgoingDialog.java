// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/OutgoingDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.UI.OutgoingExpense;
import com.google.cloud.firestore.ListenerRegistration;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;

public class OutgoingDialog extends JDialog {

	private final FirebaseDataManager firebaseDataManager;
	private final String dateStr;
	private DefaultListModel<OutgoingExpense> expenseListModel;
	private JList<OutgoingExpense> expenseList;
	private JLabel totalExpensesLabel;
	private ListenerRegistration expenseListenerRegistration;
	private JButton editButton, deleteButton;

	public OutgoingDialog(JFrame parent, FirebaseDataManager firebaseDataManager, Date selectedDate) {
		super(parent, "Outgoing Expenses for " + new SimpleDateFormat("dd MMMM yyyy").format(selectedDate), true);
		this.firebaseDataManager = firebaseDataManager;
		this.dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);

		setupUI();
		attachListener();

		setMinimumSize(new Dimension(500, 600));
		setLocationRelativeTo(parent);

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (expenseListenerRegistration != null) {
					expenseListenerRegistration.remove();
				}
			}
		});
	}

	private void attachListener() {
		expenseListenerRegistration = firebaseDataManager.attachOutgoingExpenseListenerForDate(dateStr,
				(snapshot, error) -> {
					if (error != null) {
						System.err.println("Listener failed: " + error);
						return;
					}
					if (snapshot != null) {
						SwingUtilities.invokeLater(() -> {
							expenseListModel.clear();
							double total = 0;
							for (var doc : snapshot.getDocuments()) {
								OutgoingExpense expense = doc.toObject(OutgoingExpense.class);
								expenseListModel.addElement(expense);
								total += expense.getAmount();
							}
							totalExpensesLabel.setText(String.format("Total Outgoing: ₹%.2f", total));
						});
					}
				});
	}

	private void setupUI() {
		JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
		mainPanel.setBackground(new Color(45, 45, 45));
		mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

		totalExpensesLabel = new JLabel("Total Outgoing: ₹0.00");
		totalExpensesLabel.setFont(new Font("Arial", Font.BOLD, 20));
		totalExpensesLabel.setForeground(new Color(255, 100, 100));
		totalExpensesLabel.setHorizontalAlignment(SwingConstants.CENTER);
		mainPanel.add(totalExpensesLabel, BorderLayout.NORTH);

		expenseListModel = new DefaultListModel<>();
		expenseList = new JList<>(expenseListModel);
		expenseList.setBackground(new Color(60, 60, 60));
		expenseList.setForeground(Color.WHITE);
		expenseList.setFont(new Font("Monospaced", Font.PLAIN, 14));
		mainPanel.add(new JScrollPane(expenseList), BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
		bottomPanel.setOpaque(false);
		bottomPanel.add(createInputPanel(), BorderLayout.CENTER);
		bottomPanel.add(createEditDeletePanel(), BorderLayout.SOUTH);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);

		setContentPane(mainPanel);

		expenseList.addListSelectionListener(e -> {
			boolean isSelected = !expenseList.isSelectionEmpty();
			editButton.setEnabled(isSelected);
			deleteButton.setEnabled(isSelected);
		});
	}

	private JPanel createInputPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		panel.setBorder(BorderFactory.createTitledBorder(new LineBorder(new Color(80, 80, 80)), "Add New Expense"));
		panel.setOpaque(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);
		gbc.fill = GridBagConstraints.HORIZONTAL;

		gbc.gridx = 0;
		gbc.gridy = 0;
		panel.add(createStyledLabel("Description:"), gbc);
		gbc.gridx = 1;
		gbc.weightx = 1.0;
		JTextField descriptionField = new JTextField(20);
		panel.add(descriptionField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.weightx = 0;
		panel.add(createStyledLabel("Amount (₹):"), gbc);
		gbc.gridx = 1;
		JTextField amountField = new JTextField(10);
		panel.add(amountField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.CENTER;
		JButton addButton = new JButton("Add Expense");
		panel.add(addButton, gbc);

		addButton.addActionListener(e -> {
			String description = descriptionField.getText().trim();
			String amountStr = amountField.getText().trim();
			if (description.isEmpty() || amountStr.isEmpty()) {
				JOptionPane.showMessageDialog(this, "Description and amount cannot be empty.", "Input Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				double amount = Double.parseDouble(amountStr);
				if (amount <= 0) {
					JOptionPane.showMessageDialog(this, "Amount must be positive.", "Input Error",
							JOptionPane.ERROR_MESSAGE);
					return;
				}
				OutgoingExpense newExpense = new OutgoingExpense(description, amount, dateStr);
                firebaseDataManager.addOutgoingExpense(newExpense);

				descriptionField.setText("");
				amountField.setText("");
				descriptionField.requestFocus();
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Invalid amount entered.", "Input Error",
						JOptionPane.ERROR_MESSAGE);
			}
		});
		return panel;
	}

	private JPanel createEditDeletePanel() {
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		panel.setOpaque(false);

		editButton = new JButton("Edit Selected");
		editButton.setEnabled(false);
		deleteButton = new JButton("Delete Selected");
		deleteButton.setEnabled(false);

		panel.add(editButton);
		panel.add(deleteButton);

		editButton.addActionListener(e -> handleEdit());
		deleteButton.addActionListener(e -> handleDelete());

		return panel;
	}

	private void handleEdit() {
		OutgoingExpense selectedExpense = expenseList.getSelectedValue();
		if (selectedExpense == null)
			return;

		JTextField descField = new JTextField(selectedExpense.getDescription());
		JTextField amtField = new JTextField(String.valueOf(selectedExpense.getAmount()));
		Object[] message = { "Description:", descField, "Amount:", amtField };

		int option = JOptionPane.showConfirmDialog(this, message, "Edit Expense", JOptionPane.OK_CANCEL_OPTION);
		if (option == JOptionPane.OK_OPTION) {
			try {
				selectedExpense.setDescription(descField.getText());
				selectedExpense.setAmount(Double.parseDouble(amtField.getText()));
				firebaseDataManager.updateOutgoingExpense(selectedExpense);
			} catch (NumberFormatException ex) {
				JOptionPane.showMessageDialog(this, "Invalid amount.", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void handleDelete() {
		OutgoingExpense selectedExpense = expenseList.getSelectedValue();
		if (selectedExpense == null)
			return;

		int confirm = JOptionPane.showConfirmDialog(this, "Delete this expense?\n" + selectedExpense,
				"Confirm Deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
		if (confirm == JOptionPane.YES_OPTION) {
			firebaseDataManager.removeOutgoingExpense(selectedExpense.getDocumentId());
		}
	}

	private JLabel createStyledLabel(String text) {
		JLabel label = new JLabel(text);
		label.setForeground(Color.WHITE);
		return label;
	}
}