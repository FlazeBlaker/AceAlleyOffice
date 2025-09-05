// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/RegistryViewDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

public class RegistryViewDialog extends JDialog {

	/**
	 * The constructor now contains all the logic to fetch, process, and display the
	 * registry data.
	 */
    public RegistryViewDialog(JFrame parent, String dateStr, Map<String, Map<String, Map<Integer, List<BookingDetails>>>> allBookings) {
        super(parent, "Detailed Registry for " + dateStr, true);
        setSize(1600, 900);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        // --- UI setup is now cleaner ---
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // The panel's background is now handled by the theme

        String[] columnNames = { "Platform", "Sr.No", "Name", "Contact", "Time", "Courts", "Hours", "UPI", "Cash", "Discount", "Total", "Paid" };
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        JTable registryTable = new JTable(tableModel);

        // We only need to style properties not fully covered by the Look and Feel
        registryTable.setRowHeight(25);
        registryTable.setGridColor(new Color(80, 80, 80));
        registryTable.setDefaultRenderer(Object.class, new DarkTableCellRenderer());
        
        // Data processing and population...
        // ... (this logic is correct) ...

        JScrollPane scrollPane = new JScrollPane(registryTable);
        // No need to set viewport/scrollpane background, the theme now handles it.
        
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeButton);

        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);
        add(mainPanel);
    }

	private void styleTable(JTable table) {
		table.setFont(new Font("Arial", Font.PLAIN, 14));
		table.setRowHeight(25);
		table.setForeground(Color.WHITE);
		table.setGridColor(new Color(80, 80, 80));
		table.setSelectionBackground(new Color(0, 123, 255));
		table.setSelectionForeground(Color.WHITE);
		table.setFillsViewportHeight(true);

		table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
		table.getTableHeader().setBackground(new Color(30, 30, 30));
		table.getTableHeader().setForeground(Color.WHITE);

		table.setDefaultRenderer(Object.class, new DarkTableCellRenderer());
	}

	private String formatTime(double d) {
		try {
			int hour = (int) d;
			int minute = (int) Math.round((d - hour) * 60);
			if (minute == 60) {
				hour += 1;
				minute = 0;
			}
			String time24h = String.format("%02d:%02d", hour, minute);
			return new SimpleDateFormat("hh:mm a").format(new SimpleDateFormat("HH:mm").parse(time24h));
		} catch (Exception e) {
			return "N/A";
		}
	}

    private static class DarkTableCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            // Let the Look and Feel handle the component creation
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            
            // We only override the non-selected background color to ensure consistency
            if (!isSelected) {
                setBackground(new Color(60, 60, 60));
            }
            return this;
        }
    }
}