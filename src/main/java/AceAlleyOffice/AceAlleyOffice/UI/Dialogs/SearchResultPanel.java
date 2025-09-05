// File: AceAlleyOffice/AceAlleyOffice/UI/Dialogs/SearchResultPanel.java
package AceAlleyOffice.AceAlleyOffice.UI.Dialogs;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;

public class SearchResultPanel extends JPanel {

    public SearchResultPanel(BookingDetails details) {
        setLayout(new GridBagLayout());
        setBackground(new Color(60, 60, 60));
        setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(80, 80, 80)),
            new EmptyBorder(10, 15, 10, 15)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 5, 2, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // --- Booker Details ---
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        topPanel.setOpaque(false);

        JLabel nameLabel = new JLabel(details.getBookerName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 18));
        nameLabel.setForeground(Color.WHITE);
        topPanel.add(nameLabel);

        String platform = getBookingPlatform(details);
        if (platform != null && !platform.isEmpty()) {
            JLabel platformLabel = new JLabel(platform.toUpperCase());
            platformLabel.setFont(new Font("Arial", Font.BOLD, 11));
            platformLabel.setForeground(Color.WHITE);
            platformLabel.setOpaque(true);
            platformLabel.setBackground(getPlatformColor(platform));
            platformLabel.setBorder(new EmptyBorder(2, 6, 2, 6));
            topPanel.add(platformLabel);
        }

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        add(topPanel, gbc);

        JLabel phoneLabel = new JLabel(details.getBookerContact());
        phoneLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        phoneLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 1;
        add(phoneLabel, gbc);

        String startTime = formatTime(details.getStartTime());
        String endTime = formatTime(details.getEndTime());

        String courtsStr = "N/A";
        if (details.getCourts() != null && !details.getCourts().isEmpty()) {
            courtsStr = details.getCourts().stream()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
        }
        
        JLabel timeLabel = new JLabel(String.format("Time: %s to %s on Court(s): %s", startTime, endTime, courtsStr));
        timeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        timeLabel.setForeground(Color.LIGHT_GRAY);
        gbc.gridy = 2;
        add(timeLabel, gbc);

        // --- Status Panel (Right Aligned) ---
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusPanel.setOpaque(false);

        JLabel totalLabel = new JLabel(String.format("Total: ₹%.2f", details.getPrice()));
        totalLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        totalLabel.setForeground(new Color(200, 200, 200));
        statusPanel.add(totalLabel);

        boolean isPaid = (details.getCashPaid() + details.getUpiPaid()) >= details.getPrice();
        JLabel paymentStatusLabel = new JLabel(isPaid ? "PAID" : "UNPAID");
        paymentStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        paymentStatusLabel.setForeground(Color.WHITE);
        paymentStatusLabel.setOpaque(true);
        paymentStatusLabel.setBackground(isPaid ? new Color(34, 139, 34) : new Color(220, 20, 60));
        paymentStatusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        statusPanel.add(paymentStatusLabel);

        BookingStatus status = getBookingStatus(details.getDate(), details.getStartTime(), details.getEndTime());
        JLabel bookingStatusLabel = new JLabel(status.getText());
        bookingStatusLabel.setFont(new Font("Arial", Font.BOLD, 14));
        bookingStatusLabel.setForeground(Color.WHITE);
        bookingStatusLabel.setOpaque(true);
        bookingStatusLabel.setBackground(status.getColor());
        bookingStatusLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
        statusPanel.add(bookingStatusLabel);

        // --- THIS IS THE FIX ---
        // Add a "DNS" label if the booking was marked as Did Not Show.
        if (details.isDidNotShow()) {
            JLabel dnsLabel = new JLabel("DNS");
            dnsLabel.setFont(new Font("Arial", Font.BOLD, 14));
            dnsLabel.setForeground(Color.WHITE);
            dnsLabel.setOpaque(true);
            dnsLabel.setBackground(new Color(108, 117, 125)); // Gray
            dnsLabel.setBorder(new EmptyBorder(3, 8, 3, 8));
            statusPanel.add(dnsLabel);
        }
        // --- END OF FIX ---

        gbc.gridx = 2; gbc.gridy = 0; gbc.gridheight = 3; gbc.anchor = GridBagConstraints.EAST; gbc.weightx = 1.0;
        add(statusPanel, gbc);
    }

    // NEW HELPER METHOD: Determines the booking platform string from the BookingDetails object.
    private String getBookingPlatform(BookingDetails details) {
        if (details.getMembershipIdUsed() != null && !details.getMembershipIdUsed().isEmpty()) {
            return "Membership";
        }
        if (details.isPlayoSelected()) {
            return "Playo";
        }
        if (details.isKhelomoreSelected()) {
            return "Khelomore";
        }
        return "Regular";
    }

    private Color getPlatformColor(String platform) {
        if (platform == null) return new Color(108, 117, 125); // Default gray

        switch (platform.toLowerCase()) {
            case "playo":
                return new Color(40, 167, 69); // Green
            case "khelomore":
                return new Color(253, 126, 20); // Orange
            case "membership":
                return new Color(13, 110, 253); // Blue
            case "regular":
            default:
                return new Color(108, 117, 125); // Gray
        }
    }

    // CRITICAL FIX: The original implementation would fail for times like 8.5 (8:30).
    // This new version correctly handles floating-point hours.
    private String formatTime(double d) {
        if (d == 24.0) return "12:00 AM";
        if (d < 0 || d >= 24) return "N/A";
        
        try {
            int hour = (int) d;
            int minute = (int) Math.round((d - hour) * 60);

            if (minute == 60) {
                hour += 1;
                minute = 0;
            }

            String time24h = String.format("%02d:%02d", hour, minute);
            SimpleDateFormat twentyFourHourFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat twelveHourFormat = new SimpleDateFormat("hh:mm a");
            Date date = twentyFourHourFormat.parse(time24h);
            return twelveHourFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
    
    private enum BookingStatus {
        COMPLETED("Completed", new Color(108, 117, 125)), // Gray
        ONGOING("Ongoing", new Color(0, 123, 255)),     // Blue
        UPCOMING("Upcoming", new Color(25, 135, 84));   // Dark Green

        private final String text;
        private final Color color;
        BookingStatus(String text, Color color) { this.text = text; this.color = color; }
        public String getText() { return text; }
        public Color getColor() { return color; }
    }

    private BookingStatus getBookingStatus(String dateStr, double startTime, double endTime) {
        try {
            Date bookingDate = new SimpleDateFormat("dd-MM-yyyy").parse(dateStr);
            Calendar now = Calendar.getInstance();
            Calendar bookingCal = Calendar.getInstance();
            bookingCal.setTime(bookingDate);

            if (now.get(Calendar.YEAR) > bookingCal.get(Calendar.YEAR) || now.get(Calendar.DAY_OF_YEAR) > bookingCal.get(Calendar.DAY_OF_YEAR)) {
                return BookingStatus.COMPLETED;
            }
            if (now.get(Calendar.YEAR) < bookingCal.get(Calendar.YEAR) || now.get(Calendar.DAY_OF_YEAR) < bookingCal.get(Calendar.DAY_OF_YEAR)) {
                return BookingStatus.UPCOMING;
            }

            // If it's today, check the time
            double currentHourDecimal = now.get(Calendar.HOUR_OF_DAY) + (now.get(Calendar.MINUTE) / 60.0);
            if (currentHourDecimal >= endTime) {
                return BookingStatus.COMPLETED;
            }
            if (currentHourDecimal < startTime) {
                return BookingStatus.UPCOMING;
            }
            return BookingStatus.ONGOING;

        } catch (Exception e) {
            return BookingStatus.UPCOMING; // Default
        }
    }
}