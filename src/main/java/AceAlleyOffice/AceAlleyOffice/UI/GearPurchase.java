// File: AceAlleyOffice/AceAlleyOffice/UI/GearPurchase.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.util.Map;

import com.google.cloud.Timestamp;

public class GearPurchase {
    private String customerName;
    private String customerPhone;
    private Map<String, Integer> itemsPurchased;
    private double finalAmount;
    private String paymentMethod;
    private Timestamp purchaseDate;

    // Required no-argument constructor for Firestore
    public GearPurchase() {}

    public GearPurchase(String customerName, String customerPhone, Map<String, Integer> itemsPurchased, double finalAmount, String paymentMethod) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.itemsPurchased = itemsPurchased;
        this.finalAmount = finalAmount;
        this.paymentMethod = paymentMethod;
        this.purchaseDate = Timestamp.now();
    }

    // --- Getters and Setters for all fields ---
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }
    public Map<String, Integer> getItemsPurchased() { return itemsPurchased; }
    public void setItemsPurchased(Map<String, Integer> itemsPurchased) { this.itemsPurchased = itemsPurchased; }
    public double getFinalAmount() { return finalAmount; }
    public void setFinalAmount(double finalAmount) { this.finalAmount = finalAmount; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public Timestamp getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Timestamp purchaseDate) { this.purchaseDate = purchaseDate; }
}