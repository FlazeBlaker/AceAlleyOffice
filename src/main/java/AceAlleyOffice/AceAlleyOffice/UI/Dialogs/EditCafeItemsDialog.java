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
import AceAlleyOffice.AceAlleyOffice.UI.CafeItem;

public class EditCafeItemsDialog extends JDialog {

    private final FirebaseDataManager firebaseDataManager;
    private final DefaultTableModel tableModel;
    private final JTable itemTable;
    private List<CafeItem> currentItems; // Store full objects

    public EditCafeItemsDialog(Dialog parent, FirebaseDataManager firebaseDataManager) {
        super(parent, "Edit Cafe Items", true);
        this.firebaseDataManager = firebaseDataManager;

        setSize(500, 600);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // UPDATED: Added "Quantity" column
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
        
        setTransferHandler(new FileDropHandler());
        loadItems();

        addButton.addActionListener(e -> showEditDialog(null));
        saveButton.addActionListener(e -> saveChanges());
        deleteButton.addActionListener(e -> deleteSelectedItem());
    }

    private void loadItems() {
        firebaseDataManager.attachCafeItemsListener(items -> {
            SwingUtilities.invokeLater(() -> {
                tableModel.setRowCount(0);
                this.currentItems = items;
                currentItems.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
                for (CafeItem item : currentItems) {
                    tableModel.addRow(new Object[]{item.getName(), item.getPrice(), item.getQuantity()});
                }
            });
        });
    }

    private void saveChanges() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to save.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (itemTable.isEditing()) {
            itemTable.getCellEditor().stopCellEditing();
        }

        CafeItem itemToUpdate = currentItems.get(selectedRow);
        String docId = itemToUpdate.getDocumentId();

        try {
            String newName = tableModel.getValueAt(selectedRow, 0).toString();
            double newPrice = Double.parseDouble(tableModel.getValueAt(selectedRow, 1).toString());
            int newQuantity = Integer.parseInt(tableModel.getValueAt(selectedRow, 2).toString());

            firebaseDataManager.updateCafeItem(docId, newName, newPrice, newQuantity);
            JOptionPane.showMessageDialog(this, "Changes saved successfully!");
            // Listener will auto-refresh
        } catch(NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Invalid price or quantity in table. Please enter a number.", "Error", JOptionPane.ERROR_MESSAGE);
            loadItems(); // Revert invalid edit
        }
    }

    private void deleteSelectedItem() {
        int selectedRow = itemTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an item to delete.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        CafeItem itemToDelete = currentItems.get(selectedRow);
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete '" + itemToDelete.getName() + "'?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            firebaseDataManager.deleteCafeItem(itemToDelete.getDocumentId());
            // Listener will auto-refresh
        }
    }

    private void showEditDialog(CafeItem itemToEdit) {
        JTextField nameField = new JTextField(itemToEdit != null ? itemToEdit.getName() : "");
        JTextField priceField = new JTextField(itemToEdit != null ? String.valueOf(itemToEdit.getPrice()) : "0.0");
        JTextField quantityField = new JTextField(itemToEdit != null ? String.valueOf(itemToEdit.getQuantity()) : "0");

        JPanel panel = new JPanel(new GridLayout(0, 2, 5, 5));
        panel.add(new JLabel("Item Name:"));
        panel.add(nameField);
        panel.add(new JLabel("Price:"));
        panel.add(priceField);
        panel.add(new JLabel("Quantity:"));
        panel.add(quantityField);

        int result = JOptionPane.showConfirmDialog(this, panel, itemToEdit != null ? "Edit Item" : "Add New Item", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText().trim();
                double price = Double.parseDouble(priceField.getText().trim());
                int quantity = Integer.parseInt(quantityField.getText().trim());

                if (name.isEmpty()) return;

                if (itemToEdit != null) {
                    firebaseDataManager.updateCafeItem(itemToEdit.getDocumentId(), name, price, quantity);
                } else {
                    firebaseDataManager.addCafeItem(name, price, quantity);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Invalid price or quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // --- ADDED: A new inner class to handle the file drop ---
    private class FileDropHandler extends TransferHandler {
        @Override
        public boolean canImport(TransferSupport support) {
            // We only support dropping files
            return support.isDataFlavorSupported(DataFlavor.javaFileListFlavor);
        }

        @Override
        public boolean importData(TransferSupport support) {
            if (!canImport(support)) {
                return false;
            }
            try {
                // Get the list of dropped files
                List<File> files = (List<File>) support.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                if (files.isEmpty() || !files.get(0).getName().toLowerCase().endsWith(".json")) {
                    JOptionPane.showMessageDialog(EditCafeItemsDialog.this, "Please drop a single .json file.", "Invalid File", JOptionPane.WARNING_MESSAGE);
                    return false;
                }

                File file = files.get(0); // Process the first dropped file
                
                int confirm = JOptionPane.showConfirmDialog(EditCafeItemsDialog.this, 
                    "This will DELETE all current cafe items and replace them with the contents of this file.\nAre you sure?", 
                    "Confirm Import", JOptionPane.YES_NO_OPTION);

                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }

                // Read and parse the JSON file
                String jsonText = new String(Files.readAllBytes(file.toPath()));
                JSONObject jsonObject = new JSONObject(jsonText);
                Map<String, Object> newItems = new HashMap<>();
                Iterator<String> keys = jsonObject.keys();
                while (keys.hasNext()) {
                    String itemName = keys.next();
                    double itemPrice = jsonObject.getDouble(itemName);
                    newItems.put(itemName, itemPrice);
                }

                // Call the batch update method and refresh the view
                firebaseDataManager.batchUpdateCafeItems(newItems);
                // Add a small delay to allow the batch to complete, then reload
                new Timer(1000, e -> loadItems()).start();
                
                return true;

            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(EditCafeItemsDialog.this, "Failed to import file: " + e.getMessage(), "Import Error", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }
    }
}