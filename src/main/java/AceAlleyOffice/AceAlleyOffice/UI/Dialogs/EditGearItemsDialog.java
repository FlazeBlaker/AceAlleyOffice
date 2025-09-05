// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/EditGearItemsDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.datatransfer.DataFlavor;
import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.TransferHandler;
import javax.swing.table.DefaultTableModel;

import org.json.JSONObject;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.UI.GearItem;

public class EditGearItemsDialog extends JDialog {

    private final FirebaseDataManager firebaseDataManager;
    private final DefaultTableModel tableModel;
    private final JTable itemTable;
    private List<GearItem> currentItems;
    private Map<String, String> itemDocIds; // Map item name to its Firestore document ID

    public EditGearItemsDialog(Dialog parent, FirebaseDataManager firebaseDataManager) {
        super(parent, "Edit Gear Items", true);
        this.firebaseDataManager = firebaseDataManager;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        tableModel = new DefaultTableModel(new String[]{"Item Name", "Price", "Quantity"}, 0);
        itemTable = new JTable(tableModel);
        add(new JScrollPane(itemTable), BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        JButton addButton = new JButton("Add New Item");
        JButton saveButton = new JButton("Save Changes");
        JButton deleteButton = new JButton("Delete Selected");
        buttonPanel.add(addButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        loadItems();

        addButton.addActionListener(e -> showEditDialog(null));
        saveButton.addActionListener(e -> saveChanges());
        deleteButton.addActionListener(e -> deleteSelectedItem());
    }
    private void showEditDialog(GearItem itemToEdit) {
        JTextField nameField = new JTextField(itemToEdit != null ? itemToEdit.getName() : "");
        JTextField priceField = new JTextField(itemToEdit != null ? String.valueOf(itemToEdit.getPrice()) : "0.0");
        JTextField quantityField = new JTextField(itemToEdit != null ? String.valueOf(itemToEdit.getQuantity()) : "0");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Item Name:")); panel.add(nameField);
        panel.add(new JLabel("Price:")); panel.add(priceField);
        panel.add(new JLabel("Quantity:")); panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(this, panel, itemToEdit != null ? "Edit Item" : "Add New Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());
                if (name.isEmpty()) return;

                if (itemToEdit != null) {
                    firebaseDataManager.updateGearItem(itemToEdit.getDocumentId(), name, price, quantity);
                } else {
                    firebaseDataManager.addGearItem(name, price, quantity);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void loadItems() {
        firebaseDataManager.attachGearItemsListener(items -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                this.currentItems = items;
                currentItems.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (GearItem item : currentItems) {
                    tableModel.addRow(new Object[]{item.getName(), item.getPrice(), item.getQuantity()});
                }
            });
        });
    }

    private void addNewItem() {
        String name = JOptionPane.showInputDialog(this, "Enter new item name:");
        if (name == null || name.trim().isEmpty()) return;

        String priceStr = JOptionPane.showInputDialog(this, "Enter price for " + name + ":");
        if (priceStr == null) return;

        try {
            double price = Double.parseDouble(priceStr);
            firebaseDataManager.addGearItem(name, price);
            loadItems();
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveChanges() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to save.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (itemTable.isEditing()) itemTable.getCellEditor().stopCellEditing();

        GearItem itemToUpdate = currentItems.get(selectedRow);

        try {
            String newName = tableModel.getValueAt(selectedRow, 0).toString();
            double newPrice = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
            int newQuantity = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());

            firebaseDataManager.updateGearItem(itemToUpdate.getDocumentId(), newName, newPrice, newQuantity);
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");
        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity.", "Error", JOptionPane.ERROR_MESSAGE);
            loadItems();
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) return;
        
        GearItem itemToDelete = currentItems.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete '" + itemToDelete.getName() + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            firebaseDataManager.deleteGearItem(itemToDelete.getDocumentId());
        }
    }

    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (files.isEmpty() || !files.get(0).getName().toLowerCase().endsWith(".json")) {
                    JOptionPane.showMessageDialog(EditGearItemsDialog.this, "Please drop a single .json file.", "Invalid File", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                File file = files.get(0);
                
                int confirm = JOptionPane.showConfirmDialog(EditGearItemsDialog.this, 
                    "This will DELETE all current gear items and replace them with the contents of this file.\nAre you sure?", 
                    "Confirm Import", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }

                String jsonText = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObject = new JSONObject(jsonText);
                Map<String, Object> newItems = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String itemName = keys.next();
                    double itemPrice = jsonObject.getDouble(itemName);
                    newItems.put(itemName, itemPrice);
                }

                firebaseDataManager.batchUpdateGearItems(newItems);
                new Timer(1500, e -> loadItems()).start(); // Delay to allow batch to complete
                
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(EditGearItemsDialog.this, "Failed to import file: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
}