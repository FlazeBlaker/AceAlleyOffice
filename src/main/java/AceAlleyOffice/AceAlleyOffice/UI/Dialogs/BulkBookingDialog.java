// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/BulkBookingDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.toedter.calendar.JDateChooser;

import AceAlleyOffice.AceAlleyOffice.UI.ModernButton;
import AceAlleyOffice.AceAlleyOffice.UI.UIBuilder;

public class BulkBookingDialog extends JDialog {

    private Date startDate, endDate;
    private final JDateChooser startDateChooser;
    private final JDateChooser endDateChooser;

    public BulkBookingDialog(Frame owner) {
        super(owner, "Select Bulk Booking Date Range", true);
        setSize(400, 200);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 45));

        JPanel panel = new JPanel(new GridLayout(2, 2, 10, 10));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Start Date
        panel.add(new JLabel("Start Date:") {{ setForeground(Color.WHITE); }});
        startDateChooser = new JDateChooser();
        startDateChooser.setDate(new Date()); // Default to today
        panel.add(startDateChooser);

        // End Date
        panel.add(new JLabel("End Date:") {{ setForeground(Color.WHITE); }});
        endDateChooser = new JDateChooser();
        panel.add(endDateChooser);
        UIBuilder.styleDateChooser(startDateChooser, BorderFactory.createEmptyBorder(5,5,5,5));
        UIBuilder.styleDateChooser(endDateChooser, BorderFactory.createEmptyBorder(5,5,5,5));
        
        add(panel, BorderLayout.CENTER);

        // --- Button Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton proceedButton = new ModernButton("Proceed", new Color(40, 167, 69));
        JButton cancelButton = new ModernButton("Cancel", new Color(108, 117, 125));
        buttonPanel.add(cancelButton);
        buttonPanel.add(proceedButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // --- Logic to ensure end date is after start date ---
        startDateChooser.addPropertyChangeListener("date", evt -> {
            Date newStartDate = (Date) evt.getNewValue();
            if (newStartDate != null) {
                endDateChooser.setMinSelectableDate(newStartDate);
                if (endDateChooser.getDate() != null && endDateChooser.getDate().before(newStartDate)) {
                    endDateChooser.setDate(newStartDate);
                }
            }
        });
        // Set initial minimum date for the end date chooser
        endDateChooser.setMinSelectableDate(startDateChooser.getDate());

        // --- Action Listeners ---
        cancelButton.addActionListener(e -> dispose());
        proceedButton.addActionListener(e -> {
            if (startDateChooser.getDate() == null || endDateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please select both a start and an end date.", "Input Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            this.startDate = startDateChooser.getDate();
            this.endDate = endDateChooser.getDate();
            dispose();
        });
    }

    public Date getStartDate() { return startDate; }
    public Date getEndDate() { return endDate; }
}