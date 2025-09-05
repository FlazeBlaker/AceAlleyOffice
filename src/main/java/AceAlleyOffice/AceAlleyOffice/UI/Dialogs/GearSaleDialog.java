// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/GearSaleDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.cloud.firestore.ListenerRegistration;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.Core.User;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.GearItem;
import AceAlleyOffice.AceAlleyOffice.UI.GearSale;

public class GearSaleDialog extends JDialog {
    private final Main mainApp;
    private final FirebaseDataManager firebaseDataManager;
    private final String dateStr;
    private final User currentUser;
    private DefaultListModel<GearSale> saleListModel;
    private JLabel totalSalesLabel;
    private ListenerRegistration saleHistoryListener, gearItemListener;
    private DefaultListModel<String> dailySummaryListModel;
    private List<GearItem> allGearItems = new ArrayList<>();
    private final Map<String, Integer> currentSaleQuantities = new HashMap<>();
    private final Map<String, JLabel> saleQuantityLabels = new HashMap<>();
    private JPanel itemsPanel;
    private JLabel newSaleTotalLabel;
    private JTextField nameField, phoneField;
    private JRadioButton cashRadio, upiRadio;

    public GearSaleDialog(Main parent, FirebaseDataManager firebaseDataManager, Date selectedDate, User currentUser) {
        super(parent, "Gear Sales for " + new SimpleDateFormat("dd MMMM yyyy").format(selectedDate), true);
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
            @Override public void windowClosing(WindowEvent e) {
                if (saleHistoryListener != null) saleHistoryListener.remove();
                if (gearItemListener != null) gearItemListener.remove();
            }
        });
    }

    private void attachListeners() {
        gearItemListener = firebaseDataManager.attachGearItemsListener(items -> {
            SwingUtilities.invokeLater(() -> {
                this.allGearItems = items;
                this.allGearItems.sort((a,b) -> a.getName().compareToIgnoreCase(b.getName()));
                refreshInteractiveInputPanel();
            });
        });

        saleHistoryListener = firebaseDataManager.attachGearSaleListenerForDate(dateStr, (snapshot, error) -> {
            if (error != null) return;
            if (snapshot != null) {
                SwingUtilities.invokeLater(() -> {
                    saleListModel.clear();
                    dailySummaryListModel.clear();
                    double total = 0;
                    Map<String, Integer> dailyItemTotals = new TreeMap<>();
                    for (var doc : snapshot.getDocuments()) {
                        GearSale sale = doc.toObject(GearSale.class);
                        sale.setDocumentId(doc.getId());
                        saleListModel.addElement(sale);
                        total += sale.getTotalAmount();
                        if (sale.getItemsSold() != null) {
                            for (Map.Entry<String, Integer> itemEntry : sale.getItemsSold().entrySet()) {
                                dailyItemTotals.merge(itemEntry.getKey(), itemEntry.getValue(), Integer::sum);
                            }
                        }
                    }
                    totalSalesLabel.setText(String.format("Total Gear Sales for Today: ₹%.2f", total));
                    for (Map.Entry<String, Integer> entry : dailyItemTotals.entrySet()) {
                        dailySummaryListModel.addElement(entry.getKey() + ": " + entry.getValue());
                    }
                });
            }
        });
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
        totalSalesLabel = new JLabel("Total Gear Sales for Today: ₹0.00");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 20));
        totalSalesLabel.setForeground(new Color(60, 179, 113));
        totalSalesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalSalesLabel.setBorder(new EmptyBorder(0, 0, 10, 0));
        mainPanel.add(totalSalesLabel, BorderLayout.NORTH);
        setContentPane(mainPanel);
    }
    
    private JPanel createHistoryAndSummaryPanel() {
        saleListModel = new DefaultListModel<>();
        JList<GearSale> saleList = new JList<>(saleListModel);
        saleList.setBackground(new Color(60, 60, 60));
        saleList.setForeground(Color.WHITE);
        saleList.setCellRenderer(new SaleListCellRenderer());
        JScrollPane historyScrollPane = new JScrollPane(saleList);
        historyScrollPane.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), "Today's Gear Sales History", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
        dailySummaryListModel = new DefaultListModel<>();
        JList<String> dailySummaryList = new JList<>(dailySummaryListModel);
        dailySummaryList.setBackground(new Color(60, 60, 60));
        dailySummaryList.setForeground(Color.WHITE);
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
        deleteOneButton.addActionListener(e -> {
            String selectedSummary = dailySummaryList.getSelectedValue();
            if (selectedSummary == null) return;
            String itemName = selectedSummary.split(":")[0].trim();
            List<GearSale> allSalesForDay = new ArrayList<>();
            for (int i = 0; i < saleListModel.getSize(); i++) allSalesForDay.add(saleListModel.getElementAt(i));
            new SelectGearSaleDialog(this, firebaseDataManager, allSalesForDay, itemName).setVisible(true);
        });
        deleteAllButton.addActionListener(e -> {
            String selectedValue = dailySummaryList.getSelectedValue();
            if (selectedValue == null) return;
            String itemName = selectedValue.split(":")[0].trim();
            if (JOptionPane.showConfirmDialog(this, "Delete ALL sales of '" + itemName + "' for today?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                firebaseDataManager.deleteAllSalesOfGearItem(dateStr, itemName);
            }
        });
        TitledBorder summaryBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), "Daily Item Summary", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE);
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
        panel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(new Color(80, 80, 80)), "Add New Gear Sale", 0, 0, new Font("Arial", Font.BOLD, 14), Color.WHITE));
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
        nameField = new JTextField(15);
        phoneField = new JTextField(15);
        phoneField.getDocument().addDocumentListener(new DocumentListener() {
            private Timer timer;
            public void insertUpdate(DocumentEvent e) { handleChange(); }
            public void removeUpdate(DocumentEvent e) { handleChange(); }
            public void changedUpdate(DocumentEvent e) { handleChange(); }
            private void handleChange() {
                if (timer != null) timer.stop();
                timer = new Timer(500, (event) -> {
                    String phoneNumber = phoneField.getText().trim();
                    if (phoneNumber.length() >= 10) {
                        firebaseDataManager.getContact(phoneNumber, contactData -> {
                            if (contactData != null) {
                                SwingUtilities.invokeLater(() -> nameField.setText((String) contactData.getOrDefault("name", "")));
                            }
                        });
                    }
                });
                timer.setRepeats(false);
                timer.start();
            }
        });
        gbcAction.gridx = 0; gbcAction.gridy = 0; actionPanel.add(new JLabel("Customer Name:"), gbcAction);
        gbcAction.gridx = 1; gbcAction.gridy = 0; gbcAction.gridwidth = 2; gbcAction.fill = GridBagConstraints.HORIZONTAL; actionPanel.add(nameField, gbcAction);
        gbcAction.gridx = 0; gbcAction.gridy = 1; gbcAction.gridwidth = 1; gbcAction.fill = GridBagConstraints.NONE; actionPanel.add(new JLabel("Customer Phone:"), gbcAction);
        gbcAction.gridx = 1; gbcAction.gridy = 1; gbcAction.gridwidth = 2; gbcAction.fill = GridBagConstraints.HORIZONTAL; actionPanel.add(phoneField, gbcAction);
        cashRadio = new JRadioButton("Cash");
        upiRadio = new JRadioButton("UPI");
        ButtonGroup paymentGroup = new ButtonGroup();
        paymentGroup.add(cashRadio); paymentGroup.add(upiRadio); cashRadio.setSelected(true);
        JPanel paymentPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        paymentPanel.setOpaque(false);
        paymentPanel.add(cashRadio); paymentPanel.add(upiRadio);
        gbcAction.gridx = 0; gbcAction.gridy = 2; gbcAction.gridwidth = 2; gbcAction.fill = GridBagConstraints.NONE;
        actionPanel.add(paymentPanel, gbcAction);
        newSaleTotalLabel = new JLabel("New Sale Total: ₹0.00");
        newSaleTotalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        newSaleTotalLabel.setForeground(Color.WHITE);
        gbcAction.gridy = 3; actionPanel.add(newSaleTotalLabel, gbcAction);
        JButton saveButton = new JButton("Save Sale");
        JButton resetButton = new JButton("Reset Items");
        JPanel buttonContainer = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonContainer.setOpaque(false);
        buttonContainer.add(saveButton); buttonContainer.add(resetButton);
        if (currentUser != null && "admin".equals(currentUser.getRole())) {
            JButton editButton = new JButton("Edit Items");
            editButton.addActionListener(e -> new EditGearItemsDialog(this, firebaseDataManager).setVisible(true));
            buttonContainer.add(editButton);
        }
        gbcAction.gridy = 4; gbcAction.gridwidth = 2; gbcAction.anchor = GridBagConstraints.CENTER;
        actionPanel.add(buttonContainer, gbcAction);
        resetButton.addActionListener(e -> resetSalePanel());
        saveButton.addActionListener(e -> saveSale());
        panel.add(itemsScrollPane, BorderLayout.CENTER);
        panel.add(actionPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void refreshInteractiveInputPanel() {
        if (itemsPanel == null) return;
        itemsPanel.removeAll();
        GridBagConstraints gbcItems = new GridBagConstraints();
        gbcItems.insets = new Insets(4, 4, 4, 4);
        for (int i = 0; i < allGearItems.size(); i++) {
            GearItem item = allGearItems.get(i);
            currentSaleQuantities.putIfAbsent(item.getName(), 0);
            JLabel nameLabel = new JLabel(item.getName());
            nameLabel.setForeground(Color.WHITE);
            JLabel quantityDisplayLabel = new JLabel("(Qty: " + item.getQuantity() + ")");
            quantityDisplayLabel.setForeground(item.getQuantity() > 0 ? Color.LIGHT_GRAY : Color.RED);
            JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
            namePanel.setOpaque(false);
            namePanel.add(nameLabel);
            namePanel.add(quantityDisplayLabel);
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
        gbcItems.gridy = allGearItems.size(); gbcItems.weighty = 1.0;
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
        for (Map.Entry<String, Integer> entry : currentSaleQuantities.entrySet()) {
            if (entry.getValue() > 0) {
                double itemPrice = allGearItems.stream().filter(item -> item.getName().equals(entry.getKey())).mapToDouble(GearItem::getPrice).findFirst().orElse(0.0);
                total += itemPrice * entry.getValue();
            }
        }
        newSaleTotalLabel.setText(String.format("New Sale Total: ₹%.2f", total));
    }

    private void saveSale() {
        String custName = nameField.getText().trim();
        String custPhone = phoneField.getText().trim();
        if (custName.isEmpty() || custPhone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Customer name and phone are required.", "Input Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        Map<String, Integer> itemsToSell = new HashMap<>();
        Map<String, String> nameToIdMap = new HashMap<>();
        double totalSalePrice = 0;
        for (GearItem item : allGearItems) {
            nameToIdMap.put(item.getName(), item.getDocumentId());
            int quantityToSell = currentSaleQuantities.getOrDefault(item.getName(), 0);
            if (quantityToSell > 0) {
                if (quantityToSell > item.getQuantity()) {
                    JOptionPane.showMessageDialog(this, "Not enough stock for " + item.getName() + ".\nRequested: " + quantityToSell + ", Available: " + item.getQuantity(), "Out of Stock", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                itemsToSell.put(item.getName(), quantityToSell);
                totalSalePrice += item.getPrice() * quantityToSell;
            }
        }
        if (itemsToSell.isEmpty()) return;
        String paymentMethod = upiRadio.isSelected() ? "UPI" : "Cash";
        GearSale sale = new GearSale(custName, custPhone, itemsToSell, totalSalePrice, paymentMethod, dateStr);
        firebaseDataManager.addGearSale(sale);
        firebaseDataManager.addOrUpdateContact(custPhone, custName, "");
        Map<String, Integer> itemsToDecrement = new HashMap<>();
        for(Map.Entry<String, Integer> entry : itemsToSell.entrySet()) {
            itemsToDecrement.put(nameToIdMap.get(entry.getKey()), entry.getValue());
        }
        firebaseDataManager.updateGearItemQuantities(itemsToDecrement);
        JOptionPane.showMessageDialog(this, "Gear sale saved successfully!");
        resetSalePanel();
    }

    private void resetSalePanel() {
        nameField.setText("");
        phoneField.setText("");
        cashRadio.setSelected(true);
        for (String itemName : currentSaleQuantities.keySet()) {
            currentSaleQuantities.put(itemName, 0);
            if (saleQuantityLabels.containsKey(itemName)) {
                saleQuantityLabels.get(itemName).setText("0");
            }
        }
        updateTotal();
    }    private static class SaleListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof GearSale) {
                GearSale sale = (GearSale) value;
                String items = sale.getItemsSold().entrySet().stream()
                    .map(entry -> entry.getKey() + "(x" + entry.getValue() + ")")
                    .collect(Collectors.joining(", "));
                setText(String.format("To: %s | Total: ₹%.2f | Items: %s", sale.getCustomerName(), sale.getTotalAmount(), items));
                setFont(new Font("Monospaced", Font.PLAIN, 12));
            }
            return this;
        }
    }
}