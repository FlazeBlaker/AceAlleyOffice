package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.firestore.annotation.DocumentId;

public class GearItem {
    @DocumentId
    private String documentId;
    private String name;
    private double price;
    private int quantity;
    private String venue;

    // Required no-argument constructor for Firestore
    public GearItem() {}

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
}