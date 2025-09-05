// File: AceAlleyOffice/AceAlleyOffice/Core/NotificationService.java
package AceAlleyOffice.AceAlleyOffice.Core;

import com.twilio.Twilio;
import com.twilio.exception.ApiException;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import org.json.JSONObject;
import org.json.JSONTokener;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class NotificationService {

    // --- Credentials are now instance variables, not static constants ---
    private final String accountSid;
    private final String authToken;
    private final String twilioPhoneNumber;
    private final String whatsappTemplateSid;

    public NotificationService() {
        // Load credentials from the JSON file
        JSONObject credentials = loadCredentials();
        this.accountSid = credentials.getString("twilio_account_sid");
        this.authToken = credentials.getString("twilio_auth_token");
        this.twilioPhoneNumber = credentials.getString("twilio_phone_number");
        this.whatsappTemplateSid = credentials.getString("whatsapp_template_sid");

        // Initialize Twilio with the loaded credentials
        Twilio.init(this.accountSid, this.authToken);
    }

    /**
     * Loads credentials from the credentials.json file in the resources folder.
     * @return A JSONObject containing the credentials.
     */
    private JSONObject loadCredentials() {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.json")) {
            if (inputStream == null) {
                System.err.println("CRITICAL ERROR: credentials.json not found in resources folder.");
                
                return new JSONObject(); 
            }
            return new JSONObject(new JSONTokener(inputStream));
        } catch (Exception e) {
            e.printStackTrace();
            // Handle exceptions (e.g., file not found, JSON parsing error)
            return new JSONObject();
        }
    }

    public void sendBookingConfirmation(BookingDetails details) {
        String formattedNumber = formatPhoneNumber(details.getBookerContact());
        if (formattedNumber == null) {
            System.err.println("Could not format phone number: " + details.getBookerContact());
            return;
        }

        try {
            sendWhatsAppTemplate(formattedNumber, details);
        } catch (ApiException e) {
            System.err.println("WhatsApp failed: " + e.getMessage());
            if (e.getCode() == 63018 || e.getCode() == 21614) { // Not a WhatsApp user
                System.out.println("Number is not on WhatsApp. Falling back to SMS.");
                try {
                    sendSms(formattedNumber, details);
                } catch (ApiException smsException) {
                    System.err.println("SMS fallback also failed: " + smsException.getMessage());
                }
            }
        }
    }

    private void sendWhatsAppTemplate(String to, BookingDetails details) throws ApiException {
        String fromNumber = "whatsapp:" + this.twilioPhoneNumber;
        String toNumber = "whatsapp:" + to;

        Map<String, String> variables = new HashMap<>();
        variables.put("first_name", details.getBookerName() != null ? details.getBookerName() : "Valued Customer");
        variables.put("date", details.getDate() != null ? details.getDate() : "Not specified");
        String timeStr = formatHourToTime(details.getStartTime()) + " - " + formatHourToTime(details.getEndTime());
        variables.put("time", timeStr.contains("N/A") ? "Not specified" : timeStr);
        
        String contentVariables = new JSONObject(variables).toString();
        
        Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(fromNumber),
                "Your booking at Ace Alley is confirmed."
            )
            .setContentSid(this.whatsappTemplateSid)
            .setContentVariables(contentVariables)
            .create();

        System.out.println("WhatsApp Template message sent successfully to " + to);
    }

    private void sendSms(String to, BookingDetails details) throws ApiException {
        String courtsStr = details.getCourts().stream().map(String::valueOf).collect(Collectors.joining(", "));
        String timeStr = formatHourToTime(details.getStartTime());
        String smsBody = String.format("Ace Alley Confirmed: %s, %s at %s, Court(s) %s.",
            details.getBookerName(), details.getDate(), timeStr, courtsStr);
        
        Message.creator(new PhoneNumber(to), new PhoneNumber(this.twilioPhoneNumber), smsBody).create();
        System.out.println("SMS message sent successfully to " + to);
    }

    // --- (formatPhoneNumber and formatHourToTime helper methods remain the same) ---
    private String formatPhoneNumber(String number) {
        if (number == null || number.trim().isEmpty()) return null;
        String digits = number.replaceAll("[^\\d]", "");
        if (digits.length() == 10) return "+91" + digits;
        if (digits.length() == 11 && digits.startsWith("0")) return "+91" + digits.substring(1);
        if (digits.length() == 12 && digits.startsWith("91")) return "+" + digits;
        if (number.startsWith("+") && number.length() > 10) return number;
        return null;
    }
    
    private String formatHourToTime(double d) {
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
            return "N/A";
        }
    }
}
