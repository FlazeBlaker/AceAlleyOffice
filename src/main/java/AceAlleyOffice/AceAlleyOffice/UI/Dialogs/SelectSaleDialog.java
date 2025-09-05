// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/SelectSaleDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.UI.CafeSale;
import AceAlleyOffice.AceAlleyOffice.UI.ModernButton;

public class SelectSaleDialog extends JDialog {

    public SelectSaleDialog(Frame owner, FirebaseDataManager firebaseDataManager, List<CafeSale> allSales, String itemToDelete) {
        super(owner, "Select Sale to Modify", true);
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 45));
        
        // 1. Filter the sales to show only those containing the selected item
        DefaultListModel<CafeSale> listModel = new DefaultListModel<>();
        List<CafeSale> relevantSales = allSales.stream()
            .filter(sale -> sale.getItemsSold() != null && sale.getItemsSold().containsKey(itemToDelete))
            .collect(Collectors.toList());
        listModel.addAll(relevantSales);

        JList<CafeSale> saleList = new JList<>(listModel);
        saleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saleList.setBackground(new Color(60, 60, 60));
        saleList.setForeground(Color.WHITE);
        add(new JScrollPane(saleList), BorderLayout.CENTER);

        // 2. Create the "Delete from Selected" button
        JButton deleteButton = new ModernButton("Delete 1 from Selected Sale", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> {
            CafeSale selectedSale = saleList.getSelectedValue();
            if (selectedSale == null) {
                JOptionPane.showMessageDialog(this, "Please select a sale from the list.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                "This will reduce the quantity of '" + itemToDelete + "' by 1 in the selected sale.\nContinue?",
                "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                // 3. Call the data manager to update just this one sale
                firebaseDataManager.deleteSingleItemFromSale(selectedSale.getDocumentId(), itemToDelete);
                dispose(); // Close this dialog
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
}