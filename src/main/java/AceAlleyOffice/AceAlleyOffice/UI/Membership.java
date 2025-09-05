// File: AceAlleyOffice/AceAlleyOffice/UI/Membership.java
package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.firestore.annotation.DocumentId;
import com.google.cloud.firestore.annotation.Exclude;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Membership {

	@DocumentId
	private String documentId;
	private String memberId;
	private String name;
	private String phoneNumber;
	private String email;
	private String startDate;
	private List<HourPackage> hourPackages;
	private String venue;

	public Membership() {
		this.hourPackages = new ArrayList<>();
	}

	public Membership(String id, String name, String phone, String email, String startDate) {
		this.memberId = id;
		this.name = name;
		this.phoneNumber = phone;
		this.email = email;
		this.startDate = startDate;
		this.hourPackages = new ArrayList<>();
	}

	// --- Getters and Setters ---
	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public List<HourPackage> getHourPackages() {
		return hourPackages;
	}

	public void setHourPackages(List<HourPackage> hourPackages) {
		this.hourPackages = hourPackages;
	}

	// --- Business Logic Methods ---

	@Exclude
	public double getTotalHoursRemaining() {
		if (hourPackages == null)
			return 0.0;
		Date now = new Date();
		return hourPackages.stream().filter(p -> p.getExpiryDate() != null && p.getExpiryDate().after(now))
				.mapToDouble(HourPackage::getHours).sum();
	}

	@Exclude
	public void addHoursForNewPurchase(double hoursToAdd, int durationInMonths) {
		if (hourPackages == null) {
			hourPackages = new ArrayList<>();
		}
		hourPackages.add(new HourPackage(hoursToAdd, new Date(), durationInMonths));
	}

	// --- THIS OLD, OUTDATED METHOD HAS BEEN REMOVED ---
	// @Exclude
	// public void addHoursForNewPurchase(double hoursToAdd) { ... }
	// --- END OF FIX ---

	@Exclude
	public Map<String, Double> deductHours(double hoursToDeduct) {
		Map<String, Double> ledger = new HashMap<>();
		if (hourPackages == null)
			return ledger;

		hourPackages.sort(Comparator.comparing(HourPackage::getExpiryDate));

		for (HourPackage pkg : hourPackages) {
			if (hoursToDeduct <= 0)
				break;
			if (pkg.getHours() > 0 && pkg.getExpiryDate().after(new Date())) {
				double hoursAvailable = pkg.getHours();
				double hoursToTake = Math.min(hoursAvailable, hoursToDeduct);
				pkg.setHours(hoursAvailable - hoursToTake);
				hoursToDeduct -= hoursToTake;
				ledger.put(pkg.getPackageId(), hoursToTake);
			}
		}
		return ledger;
	}

	@Exclude
	public void refundHours(Map<String, Double> ledger) {
		if (ledger == null || ledger.isEmpty())
			return;
		for (Map.Entry<String, Double> entry : ledger.entrySet()) {
			String packageIdFromLedger = entry.getKey();
			double hoursToRefund = entry.getValue();
			hourPackages.stream()
					.filter(pkg -> pkg.getPackageId() != null && pkg.getPackageId().equals(packageIdFromLedger))
					.findFirst()
					.ifPresent(targetPackage -> targetPackage.setHours(targetPackage.getHours() + hoursToRefund));
		}
	}

	@Override
	public String toString() {
		return name + " (" + phoneNumber + ")";
	}

	// --- Inner Class for Hour Packages ---
	public static class HourPackage {
		private String packageId;
		private double hours;
		private Date purchaseDate;
		private Date expiryDate;

		public HourPackage() {
		}

		public HourPackage(double hours, Date purchaseDate, int durationInMonths) {
			this.packageId = UUID.randomUUID().toString();
			this.hours = hours;
			this.purchaseDate = purchaseDate;
			Calendar cal = Calendar.getInstance();
			cal.setTime(purchaseDate);
			cal.add(Calendar.MONTH, durationInMonths);
			this.expiryDate = cal.getTime();
		}

		public String getPackageId() {
			return packageId;
		}

		public void setPackageId(String packageId) {
			this.packageId = packageId;
		}

		public double getHours() {
			return hours;
		}

		public void setHours(double hours) {
			this.hours = hours;
		}

		public Date getPurchaseDate() {
			return purchaseDate;
		}

		public void setPurchaseDate(Date purchaseDate) {
			this.purchaseDate = purchaseDate;
		}

		public Date getExpiryDate() {
			return expiryDate;
		}

		public void setExpiryDate(Date expiryDate) {
			this.expiryDate = expiryDate;
		}
	}
}