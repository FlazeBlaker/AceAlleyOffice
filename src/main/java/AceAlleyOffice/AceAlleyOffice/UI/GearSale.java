package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import java.util.Map;

public class GearSale {
    @DocumentId
    private String documentId;
    private Map<String, Integer> itemsSold;
    private double totalAmount;
    private String dateString;
    private Timestamp saleTimestamp;
    private String venue;
    private String customerName;
    private String customerPhone;
    private String paymentMethod;

    public GearSale() {}

    public GearSale(String customerName, String customerPhone, Map<String, Integer> itemsSold, double totalAmount, String paymentMethod, String dateString) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.itemsSold = itemsSold;
        this.totalAmount = totalAmount;
        this.paymentMethod = paymentMethod;
        this.dateString = dateString;
        this.saleTimestamp = Timestamp.now();
    }

    // Add all necessary Getters and Setters for every field below
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public Map<String, Integer> getItemsSold() { return itemsSold; }
    public void setItemsSold(Map<String, Integer> itemsSold) { this.itemsSold = itemsSold; }
    public double getTotalAmount() { return totalAmount; }
    public void setTotalAmount(double totalAmount) { this.totalAmount = totalAmount; }
    public String getDateString() { return dateString; }
    public void setDateString(String dateString) { this.dateString = dateString; }
    public Timestamp getSaleTimestamp() { return saleTimestamp; }
    public void setSaleTimestamp(Timestamp saleTimestamp) { this.saleTimestamp = saleTimestamp; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
}