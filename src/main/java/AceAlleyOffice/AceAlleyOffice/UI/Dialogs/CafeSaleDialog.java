// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/CafeSaleDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import com.google.cloud.firestore.ListenerRegistration;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.User;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.CafeItem;
import AceAlleyOffice.AceAlleyOffice.UI.CafeSale;

public class CafeSaleDialog extends JDialog {

	private final Main mainApp;
	private final FirebaseDataManager firebaseDataManager;
	private final String dateStr;
	private final User currentUser;

	private DefaultListModel<CafeSale> saleListModel;
	private JLabel totalSalesLabel;
	private ListenerRegistration saleHistoryListener;
	private ListenerRegistration cafeItemListener;
	private DefaultListModel<String> dailySummaryListModel;

	private List<CafeItem> allCafeItems = new ArrayList<>();
	private final Map<String, JLabel> inventoryLabels = new HashMap<>();
	private final Map<String, Integer> currentSaleQuantities = new HashMap<>();
	private final Map<String, JLabel> saleQuantityLabels = new HashMap<>();

	private JPanel itemsPanel;
	private JLabel newSaleTotalLabel;
	private JCheckBox cashCheckBox, upiCheckBox;
	private JTextField cashField, upiField;

    public CafeSaleDialog(Main parent, FirebaseDataManager firebaseDataManager, Date selectedDate, User currentUser) {
        super(parent, "Cafe Sales for " + new SimpleDateFormat("dd MMMM yyyy").format(selectedDate), true);
        this.mainApp = parent;
        this.firebaseDataManager = firebaseDataManager;
        this.dateStr = new SimpleDateFormat("dd-MM-yyyy").format(selectedDate);
        this.currentUser = currentUser;
        
        setupUI();
        attachListeners();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        setSize(new Dimension(1200, screenSize.height - 50));
        setLocationRelativeTo(parent);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (saleHistoryListener != null) saleHistoryListener.remove();
                if (cafeItemListener != null) cafeItemListener.remove();
            }
        });
    }

	// --- THIS IS THE CORRECTED METHOD ---
    private void attachListeners() {
        cafeItemListener = firebaseDataManager.attachCafeItemsListener(items -> {
            SwingUtilities.invokeLater(() -> {
                this.allCafeItems = items;
                this.allCafeItems.sort((a,b) -> a.getName().compareToIgnoreCase(b.getName()));
                refreshInteractiveInputPanel();
            });
        });

        saleHistoryListener = firebaseDataManager.attachCafeSaleListenerForDate(dateStr, (snapshot, error) -> {
            if (error != null) return;
            if (snapshot != null) {
                SwingUtilities.invokeLater(() -> {
                    saleListModel.clear();
                    dailySummaryListModel.clear();
                    double total = 0;
                    Map<String, Integer> dailyItemTotals = new TreeMap<>();
                    for (var doc : snapshot.getDocuments()) {
                        CafeSale sale = doc.toObject(CafeSale.class);
                        sale.setDocumentId(doc.getId());
                        saleListModel.addElement(sale);
                        total += sale.getTotalAmount();
                        if (sale.getItemsSold() != null) {
                            for (Map.Entry<String, Integer> itemEntry : sale.getItemsSold().entrySet()) {
                                dailyItemTotals.merge(itemEntry.getKey(), itemEntry.getValue(), Integer::sum);
                            }
                        }
                    }
                    totalSalesLabel.setText(String.format("Total Sales for Today: ₹%.2f", total));
                    for (Map.Entry<String, Integer> entry : dailyItemTotals.entrySet()) {
                        dailySummaryListModel.addElement(entry.getKey() + ": " + entry.getValue());
                    }
                });
            }
        });
    }
	

    private void refreshInteractiveInputPanel() {
        if (itemsPanel == null) return;
        itemsPanel.removeAll();
        
        GridBagConstraints gbcItems = new GridBagConstraints();
        gbcItems.insets = new Insets(4, 4, 4, 4);

        for (int i = 0; i < allCafeItems.size(); i++) {
            CafeItem item = allCafeItems.get(i);
            currentSaleQuantities.putIfAbsent(item.getName(), 0);

            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setForeground(Color.WHITE);
            
            // This label displays the inventory quantity
            JLabel quantityDisplayLabel = new JLabel("(Qty: " + item.getQuantity() + ")");
            quantityDisplayLabel.setForeground(item.getQuantity() > 0 ? Color.LIGHT_GRAY : Color.RED);

            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            namePanel.setOpaque(false);
            namePanel.add(nameLabel);
            namePanel.add(quantityDisplayLabel); // Add the quantity label to the panel

            JLabel quantityLabel = new JLabel("0", SwingConstants.CENTER);
            saleQuantityLabels.put(item.getName(), quantityLabel);
            quantityLabel.setForeground(Color.WHITE);
            quantityLabel.setPreferredSize(new Dimension(30, 20));
            JButton minusButton = new JButton("-");
            JButton plusButton = new JButton("+");

            plusButton.addActionListener(e -> updateSaleQuantity(item.getName(), 1));
            minusButton.addActionListener(e -> updateSaleQuantity(item.getName(), -1));

            JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            controlPanel.setOpaque(false);
            controlPanel.add(minusButton);
            controlPanel.add(quantityLabel);
            controlPanel.add(plusButton);

            gbcItems.gridy = i;
            gbcItems.gridx = 0; gbcItems.weightx = 1.0; gbcItems.anchor = GridBagConstraints.WEST;
            itemsPanel.add(namePanel, gbcItems);
            gbcItems.gridx = 1; gbcItems.weightx = 0; gbcItems.anchor = GridBagConstraints.EAST;
            itemsPanel.add(controlPanel, gbcItems);
        }
        
        gbcItems.gridy = allCafeItems.size(); gbcItems.weighty = 1.0;
        itemsPanel.add(new JLabel(), gbcItems);
        
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }
	private void updateSaleQuantity(String itemName, int delta) {
		int currentQty = currentSaleQuantities.getOrDefault(itemName, 0);
		int newQty = Math.max(0, currentQty + delta);
		currentSaleQuantities.put(itemName, newQty);
		saleQuantityLabels.get(itemName).setText(String.valueOf(newQty));
		updateTotal();
	}
    private void updateTotal() {
        double total = 0;
        // Iterate through the items the user has added to the current sale
        for (Map.Entry<String, Integer> entry : currentSaleQuantities.entrySet()) {
            String itemName = entry.getKey();
            int quantity = entry.getValue();

            if (quantity > 0) {
                // Find the corresponding CafeItem to get its price
                double itemPrice = allCafeItems.stream()
                    .filter(item -> item.getName().equals(itemName))
                    .mapToDouble(CafeItem::getPrice)
                    .findFirst()
                    .orElse(0.0);
                
                total += itemPrice * quantity;
            }
        }
        newSaleTotalLabel.setText(String.format("New Sale Total: ₹%.2f", total));
    }
	private void saveSale() {
		Map<String, Integer> itemsToSell = new HashMap<>();
		Map<String, String> nameToIdMap = new HashMap<>();
		double totalSalePrice = 0;

		for (CafeItem item : allCafeItems) {
			nameToIdMap.put(item.getName(), item.getDocumentId());
			int quantityToSell = currentSaleQuantities.getOrDefault(item.getName(), 0);
			if (quantityToSell > 0) {
				if (quantityToSell > item.getQuantity()) {
					JOptionPane
							.showMessageDialog(this,
									"Not enough stock for " + item.getName() + ".\nRequested: " + quantityToSell
											+ ", Available: " + item.getQuantity(),
									"Out of Stock", JOptionPane.ERROR_MESSAGE);
					return;
				}
				itemsToSell.put(item.getName(), quantityToSell);
				totalSalePrice += item.getPrice() * quantityToSell;
			}
		}

		if (itemsToSell.isEmpty())
			return;

		try {
			double cashPaid = cashCheckBox.isSelected() ? Double.parseDouble(cashField.getText()) : 0.0;
			double upiPaid = upiCheckBox.isSelected() ? Double.parseDouble(upiField.getText()) : 0.0;

			CafeSale sale = new CafeSale(itemsToSell, totalSalePrice, cashPaid, upiPaid, dateStr);
			firebaseDataManager.addCafeSale(sale);

			Map<String, Integer> itemsToDecrement = new HashMap<>();
			for (Map.Entry<String, Integer> entry : itemsToSell.entrySet()) {
				itemsToDecrement.put(nameToIdMap.get(entry.getKey()), entry.getValue());
			}
			firebaseDataManager.updateItemQuantities(itemsToDecrement);

			JOptionPane.showMessageDialog(this, "Cafe sale saved successfully!");
			resetSalePanel();
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(this, "Invalid number in payment field.", "Input Error",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void resetSalePanel() {
		for (String itemName : currentSaleQuantities.keySet()) {
			currentSaleQuantities.put(itemName, 0);
			if (saleQuantityLabels.containsKey(itemName)) {
				saleQuantityLabels.get(itemName).setText("0");
			}
		}
		cashCheckBox.setSelected(false);
		upiCheckBox.setSelected(false);
		cashField.setText("0.00");
		upiField.setText("0.00");
		cashField.setEnabled(false);
		upiField.setEnabled(false);
		updateTotal();
	}

	    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBackground(new Color(45, 45, 45));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        JPanel leftPanel = createInteractiveInputPanel();
        JPanel rightPanel = createHistoryAndSummaryPanel();
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        mainSplitPane.setOpaque(false);
        mainSplitPane.setBorder(null);
        mainSplitPane.setResizeWeight(0.5);
        mainSplitPane.setDividerSize(10);
        mainPanel.add(mainSplitPane, BorderLayout.CENTER);
        totalSalesLabel = new JLabel("Total Sales for Today: ₹0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalSalesLabel.setForeground(new Color(60, 179, 113));
        totalSalesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalSalesLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(totalSalesLabel, BorderLayout.NORTH);
        setContentPane(mainPanel);
    }

	// --- THIS IS THE CORRECTED METHOD ---
	private JPanel createHistoryAndSummaryPanel() {
		saleListModel = new DefaultListModel<>();
		JList<CafeSale> saleList = new JList<>(saleListModel);
		saleList.setBackground(new Color(60, 60, 60));
		saleList.setForeground(Color.WHITE);
		saleList.setFont(new Font("Monospaced", Font.PLAIN, 14));
		JScrollPane historyScrollPane = new JScrollPane(saleList);
		historyScrollPane
				.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)),
						"Today's Sales History", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
		historyScrollPane.getViewport().setBackground(new Color(60, 60, 60));

		dailySummaryListModel = new DefaultListModel<>();
		JList<String> dailySummaryList = new JList<>(dailySummaryListModel);
		dailySummaryList.setBackground(new Color(60, 60, 60));
		dailySummaryList.setForeground(Color.WHITE);
		dailySummaryList.setFont(new Font("Monospaced", Font.PLAIN, 14));

		JPanel summaryPanel = new JPanel(new BorderLayout());
		summaryPanel.setOpaque(false);
		summaryPanel.add(new JScrollPane(dailySummaryList), BorderLayout.CENTER);

		JButton deleteOneButton = new JButton("Delete One");
		JButton deleteAllButton = new JButton("Delete All");
		JPanel deleteButtonsPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		deleteButtonsPanel.setOpaque(false);
		deleteButtonsPanel.add(deleteOneButton);
		deleteButtonsPanel.add(deleteAllButton);
		summaryPanel.add(deleteButtonsPanel, BorderLayout.SOUTH);

		// --- Action Listeners are now correctly placed inside this method ---
		deleteOneButton.addActionListener(e -> {
			String selectedSummary = dailySummaryList.getSelectedValue();
			if (selectedSummary == null) {
				JOptionPane.showMessageDialog(this, "Please select an item from the summary list first.",
						"No Item Selected", JOptionPane.WARNING_MESSAGE);
				return;
			}
			String itemName = selectedSummary.split(":")[0].trim();

			// Get all sales for the day from the history list model
			List<CafeSale> allSalesForDay = new ArrayList<>();
			for (int i = 0; i < saleListModel.getSize(); i++) {
				allSalesForDay.add(saleListModel.getElementAt(i));
			}

			// Open the new dialog to let the user choose the specific sale
			SelectSaleDialog selectDialog = new SelectSaleDialog(mainApp, firebaseDataManager, allSalesForDay,
					itemName);
			selectDialog.setVisible(true);
		});

		deleteAllButton.addActionListener(e -> {
			String selectedValue = dailySummaryList.getSelectedValue();
			if (selectedValue == null)
				return;
			String itemName = selectedValue.split(":")[0].trim();
			int confirm = JOptionPane.showConfirmDialog(this,
					"Permanently delete ALL sales of '" + itemName + "' for today?", "Confirm Full Deletion",
					JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (confirm == JOptionPane.YES_OPTION) {
				firebaseDataManager.deleteAllSalesOfItem(dateStr, itemName);
			}
		});

		TitledBorder summaryBorder = BorderFactory.createTitledBorder(
				BorderFactory.createLineBorder(new Color(80, 80, 80)), "Daily Item Summary", 0, 0,
				new Font("Arial", Font.BOLD, 14), Color.WHITE);
		summaryPanel.setBorder(summaryBorder);

		JSplitPane rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, historyScrollPane, summaryPanel);
		rightSplitPane.setOpaque(false);
		rightSplitPane.setBorder(null);
		rightSplitPane.setResizeWeight(0.7);
		rightSplitPane.setDividerSize(10);

		JPanel container = new JPanel(new BorderLayout());
		container.setOpaque(false);
		container.add(rightSplitPane, BorderLayout.CENTER);
		return container;
	}

    private JPanel createInteractiveInputPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), "Add New Sale", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        panel.setOpaque(false);

        itemsPanel = new JPanel(new GridBagLayout());
        itemsPanel.setBackground(new Color(50, 50, 50));
        JScrollPane itemsScrollPane = new JScrollPane(itemsPanel);
        itemsScrollPane.setBorder(null);
        itemsScrollPane.getViewport().setBackground(new Color(50, 50, 50));

        JPanel actionPanel = new JPanel(new GridBagLayout());
        actionPanel.setOpaque(false);
        GridBagConstraints gbcAction = new GridBagConstraints();
        gbcAction.insets = new Insets(5, 5, 5, 5);
        gbcAction.anchor = GridBagConstraints.WEST;

        cashCheckBox = new JCheckBox("Cash Paid:");
        cashField = new JTextField("0.00", 10);
        upiCheckBox = new JCheckBox("UPI Paid:");
        upiField = new JTextField("0.00", 10);
        newSaleTotalLabel = new JLabel("New Sale Total: ₹0.00");
        newSaleTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        newSaleTotalLabel.setForeground(Color.WHITE);
        
        cashCheckBox.addActionListener(e -> cashField.setEnabled(cashCheckBox.isSelected()));
        upiCheckBox.addActionListener(e -> upiField.setEnabled(upiCheckBox.isSelected()));

        gbcAction.gridx = 0; gbcAction.gridy = 0; actionPanel.add(cashCheckBox, gbcAction);
        gbcAction.gridx = 1; actionPanel.add(cashField, gbcAction);
        gbcAction.gridx = 0; gbcAction.gridy = 1; actionPanel.add(upiCheckBox, gbcAction);
        gbcAction.gridx = 1; actionPanel.add(upiField, gbcAction);
        gbcAction.gridx = 0; gbcAction.gridy = 2; gbcAction.gridwidth = 2; actionPanel.add(newSaleTotalLabel, gbcAction);

        JButton saveButton = new JButton("Save Sale");
        JButton resetButton = new JButton("Reset Items");
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.add(saveButton);
        buttonContainer.add(resetButton);

        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            JButton editButton = new JButton("Edit Items");
            editButton.addActionListener(e -> new EditCafeItemsDialog(this, firebaseDataManager).setVisible(true));
            buttonContainer.add(editButton);
        }
        gbcAction.gridy = 3; gbcAction.gridwidth = 2; gbcAction.anchor = GridBagConstraints.CENTER;
        actionPanel.add(buttonContainer, gbcAction);
        
        resetButton.addActionListener(e -> resetSalePanel());
        saveButton.addActionListener(e -> saveSale());

        panel.add(itemsScrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);

        return panel;
    }}