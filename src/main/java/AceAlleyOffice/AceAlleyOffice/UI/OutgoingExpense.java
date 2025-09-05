// File: AceAlleyOffice/AceAlleyOffice/UI/OutgoingExpense.java
package AceAlleyOffice.AceAlleyOffice.UI;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;

public class OutgoingExpense {

	@DocumentId
	private String documentId; // ID from Firestore, needed for edits/deletes

	private String description;
	private double amount;
	private Timestamp expenseTimestamp;
	private String dateString;
	// Add this inside the class definition of each of the 5 files mentioned above

	private String venue;

	public String getVenue() {
	    return venue;
	}

	public void setVenue(String venue) {
	    this.venue = venue;
	}
	public OutgoingExpense() {
	}

	  // --- THIS IS THE CORRECTED CONSTRUCTOR ---
		public OutgoingExpense(String description, double amount, String dateString) {
			this.description = description;
			this.amount = amount;
			this.expenseTimestamp = Timestamp.now();
	        // It now uses the date that was passed to it from the dialog
			this.dateString = dateString;
		}

	// --- Getters and Setters ---
	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public Timestamp getExpenseTimestamp() {
		return expenseTimestamp;
	}

	public void setExpenseTimestamp(Timestamp expenseTimestamp) {
		this.expenseTimestamp = expenseTimestamp;
	}

	public String getDateString() {
		return dateString;
	}

	public void setDateString(String dateString) {
		this.dateString = dateString;
	}

	@Override
	public String toString() {
		return String.format("%-30s | ₹%.2f", description, amount);
	}
}