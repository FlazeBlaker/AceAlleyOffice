// File: AceAlleyOffice/AceAlleyOffice/UI/MembershipPackage.java
package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.firestore.annotation.DocumentId;

public class MembershipPackage {

	@DocumentId
	private String documentId;
	private String packageName;
	private int hours;
	private double price;
	private String venue;
	private int durationInMonths = 1; // Default to 1 month

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public MembershipPackage() {
	}

	// Getters and Setters
	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public int getHours() {
		return hours;
	}

	public void setHours(int hours) {
		this.hours = hours;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	// This is how the package will appear in the dropdown list
	@Override
	public String toString() {
		return String.format("%d Hours - ₹%,.0f", hours, price);
	}

	public int getDurationInMonths() {
		return durationInMonths;
	}

	public void setDurationInMonths(int durationInMonths) {
		this.durationInMonths = durationInMonths;
	}
}