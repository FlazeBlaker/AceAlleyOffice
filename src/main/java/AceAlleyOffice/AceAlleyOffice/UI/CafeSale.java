// File: AceAlleyOffice/AceAlleyOffice/UI/CafeSale.java
package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId; // <-- IMPORT THIS
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.stream.Collectors;

public class CafeSale {

    @DocumentId // <-- ADD THIS ANNOTATION
    private String documentId; // <-- ADD THIS FIELD

    private Map<String, Integer> itemsSold;
    private double totalAmount;
    private double cashPaid;
    private double upiPaid;
    private Timestamp saleTimestamp;
    private String dateString;
 // Add this inside the class definition of each of the 5 files mentioned above

    private String venue;

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
    public CafeSale() {}

    public CafeSale(Map<String, Integer> itemsSold, double totalAmount, double cashPaid, double upiPaid, String dateString) {
        this.itemsSold = itemsSold;
        this.totalAmount = totalAmount;
        this.cashPaid = cashPaid;
        this.upiPaid = upiPaid;
        this.saleTimestamp = Timestamp.now();
        this.dateString = dateString;
    }

    // --- ADD GETTER AND SETTER for the new field ---
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }

    // --- (All other getters and setters remain the same) ---
    public Map<String, Integer> getItemsSold() { return itemsSold; }
    public void setItemsSold(Map<String, Integer> itemsSold) { this.itemsSold = itemsSold; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public double getCashPaid() { return cashPaid; }
    public void setCashPaid(double cashPaid) { this.cashPaid = cashPaid; }
    public double getUpiPaid() { return upiPaid; }
    public void setUpiPaid(double upiPaid) { this.upiPaid = upiPaid; }
    public Timestamp getSaleTimestamp() { return saleTimestamp; }
    public void setSaleTimestamp(Timestamp saleTimestamp) { this.saleTimestamp = saleTimestamp; }
    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }

    @Override
    public String toString() {
        String items = itemsSold.entrySet().stream()
                .map(entry -> entry.getKey() + "x" + entry.getValue())
                .collect(Collectors.joining(", "));
        return String.format("₹%.2f  |  %s", totalAmount, items);
    }
}