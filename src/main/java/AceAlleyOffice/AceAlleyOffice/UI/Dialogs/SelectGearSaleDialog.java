package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.UI.GearSale;
import AceAlleyOffice.AceAlleyOffice.UI.ModernButton;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;

public class SelectGearSaleDialog extends JDialog {

    public SelectGearSaleDialog(JDialog owner, FirebaseDataManager firebaseDataManager, List<GearSale> allSales, String itemToDelete) {
        super(owner, "Select Gear Sale to Modify", true);
        setSize(500, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 45));
        
        DefaultListModel<GearSale> listModel = new DefaultListModel<>();
        List<GearSale> relevantSales = allSales.stream()
            .filter(sale -> sale.getItemsSold() != null && sale.getItemsSold().containsKey(itemToDelete))
            .collect(Collectors.toList());

        if (relevantSales.isEmpty()) {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(owner, "No sales containing '" + itemToDelete + "' were found.", "Info", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            });
            return;
        }
        listModel.addAll(relevantSales);

        JList<GearSale> saleList = new JList<>(listModel);
        saleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        saleList.setBackground(new Color(60, 60, 60));
        saleList.setForeground(Color.WHITE);
        saleList.setCellRenderer(new SaleListCellRenderer());
        add(new JScrollPane(saleList), BorderLayout.CENTER);

        JButton deleteButton = new ModernButton("Delete 1 '" + itemToDelete + "' from Selected Sale", new Color(220, 53, 69));
        deleteButton.addActionListener(e -> {
            GearSale selectedSale = saleList.getSelectedValue();
            if (selectedSale == null) {
                JOptionPane.showMessageDialog(this, "Please select a sale from the list.", "No Selection", JOptionPane.WARNING_MESSAGE);
                return;
            }
            firebaseDataManager.deleteSingleItemFromGearSale(selectedSale.getDocumentId(), itemToDelete);
            dispose();
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setOpaque(false);
        buttonPanel.add(deleteButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private static class SaleListCellRenderer extends DefaultListCellRenderer {
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