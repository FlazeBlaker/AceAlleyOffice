// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/ShiftBookingDialog.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.toedter.calendar.JDateChooser;

import AceAlleyOffice.AceAlleyOffice.Core.FirebaseDataManager;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.ModernButton;

public class ShiftBookingDialog extends JDialog {

    private final FirebaseDataManager firebaseDataManager;
    private final BookingDetails bookingToShift;
    private boolean shiftConfirmed = false;

    private final JDateChooser newDateChooser;
    private final JComboBox<String> newTimeComboBox;

    public ShiftBookingDialog(Frame owner, FirebaseDataManager firebaseDataManager, BookingDetails bookingToShift, String[] timeSlots) {
        super(owner, "Shift Booking", true);
        this.firebaseDataManager = firebaseDataManager;
        this.bookingToShift = bookingToShift;

        setSize(450, 250);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(45, 45, 45));

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        double duration = bookingToShift.getEndTime() - bookingToShift.getStartTime();
        JLabel infoLabel = new JLabel(String.format("Shifting booking for %s (%d hr/s)",
                bookingToShift.getBookerName(), duration));
        infoLabel.setForeground(Color.WHITE);
        infoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(infoLabel, gbc);

        gbc.gridy = 1; gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("New Date:") {{ setForeground(Color.WHITE); }}, gbc);
        gbc.gridx = 1;
        newDateChooser = new JDateChooser();
        newDateChooser.setDate(new Date());
        panel.add(newDateChooser, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        panel.add(new JLabel("New Start Time:") {{ setForeground(Color.WHITE); }}, gbc);
        gbc.gridx = 1;
        newTimeComboBox = new JComboBox<>(timeSlots);
        panel.add(newTimeComboBox, gbc);
        
        add(panel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setOpaque(false);
        JButton confirmButton = new ModernButton("Check & Confirm", new Color(40, 167, 69));
        JButton cancelButton = new ModernButton("Cancel", new Color(108, 117, 125));
        
        buttonPanel.add(cancelButton);
        buttonPanel.add(confirmButton);
        add(buttonPanel, BorderLayout.SOUTH);

        cancelButton.addActionListener(e -> dispose());
        confirmButton.addActionListener(e -> confirmShift());
    }

    private void confirmShift() {
        Date newDate = newDateChooser.getDate();
        String newTimeStr = (String) newTimeComboBox.getSelectedItem();
        if (newDate == null || newTimeStr == null) return;

        double duration = bookingToShift.getEndTime() - bookingToShift.getStartTime();
        long newStartTime = convertTimeSlotToHour(newTimeStr);
        double newEndTime = newStartTime + duration;
        String newDateStr = new SimpleDateFormat("dd-MM-yyyy").format(newDate);

        if (newDateStr.equals(bookingToShift.getDate()) && newStartTime == bookingToShift.getStartTime()) {
            JOptionPane.showMessageDialog(this, "The new time is the same as the current time.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        firebaseDataManager.findUnavailableCourts(newDateStr, (int)newStartTime, (int)newEndTime, unavailableCourts -> {
            boolean isAvailable = true;
            for (int courtNum : bookingToShift.getCourts()) {
                if (unavailableCourts.contains(courtNum)) {
                    isAvailable = false;
                    break;
                }
            }
            
            final boolean availabilityResult = isAvailable;
            SwingUtilities.invokeLater(() -> {
                if (availabilityResult) {
                    bookingToShift.setDate(newDateStr);
                    bookingToShift.setTime(newTimeStr);
                    bookingToShift.setStartTime(newStartTime);
                    bookingToShift.setEndTime(newEndTime);
                    shiftConfirmed = true;
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "The selected time slot is not available for all courts in this booking.", "Booking Conflict", JOptionPane.WARNING_MESSAGE);
                }
            });
        });
    }

    public boolean isShiftConfirmed() {
        return shiftConfirmed;
    }

    private long convertTimeSlotToHour(String timeSlot) {
        try {
            return Long.parseLong(new SimpleDateFormat("HH").format(new SimpleDateFormat("hh:mm a").parse(timeSlot)));
        } catch (ParseException e) {
            return -1;
        }
    }
}