// File: AceAlleyOffice/AceAlleyOffice/UI/MembershipPurchase.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.util.Calendar;
import java.util.Date;

import com.google.cloud.Timestamp;

public class MembershipPurchase {

    private String name;
    private String phone;
    private int hoursPurchased;
    private double amountPaid;
    private Timestamp purchaseDate;
    private Timestamp expiryDate; // Added expiry date field
 // Add this inside the class definition of each of the 5 files mentioned above

    private String venue;

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
    // No-argument constructor is required for Firestore
    public MembershipPurchase() {}

    public MembershipPurchase(String name, String phone, int hoursPurchased, double amountPaid) {
        this.name = name;
        this.phone = phone;
        this.hoursPurchased = hoursPurchased;
        this.amountPaid = amountPaid;
        
        // Set the purchase date to the current moment
        this.purchaseDate = Timestamp.now();
        
        // Calculate the expiry date (1 year from purchase)
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date()); // Start with today's date
        cal.add(Calendar.YEAR, 1); // Add one year
        this.expiryDate = Timestamp.of(cal.getTime());
    }

    // --- Getters and Setters for all fields ---

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public int getHoursPurchased() { return hoursPurchased; }
    public void setHoursPurchased(int hoursPurchased) { this.hoursPurchased = hoursPurchased; }

    public double getAmountPaid() { return amountPaid; }
    public void setAmountPaid(double amountPaid) { this.amountPaid = amountPaid; }

    public Timestamp getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(Timestamp purchaseDate) { this.purchaseDate = purchaseDate; }
    
    public Timestamp getExpiryDate() { return expiryDate; }
    public void setExpiryDate(Timestamp expiryDate) { this.expiryDate = expiryDate; }
}