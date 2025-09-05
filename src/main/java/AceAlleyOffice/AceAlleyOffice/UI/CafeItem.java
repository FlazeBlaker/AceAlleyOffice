package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.firestore.annotation.DocumentId;

public class CafeItem {
    @DocumentId
    private String documentId;
    private String name;
    private double price;
    private int quantity;
    
    // --- THIS IS THE FIX ---
    // Add the 'venue' field to match the data in Firestore.
    private String venue;
    // --- END OF FIX ---

    public CafeItem() {}

    // Getters and Setters
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    // Getter and Setter for the new 'venue' field
    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }
}