// File: AceAlleyOffice/AceAlleyOffice/UI/BookingDetails.java
package AceAlleyOffice.AceAlleyOffice.UI;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.DocumentId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class BookingDetails {

	@DocumentId
	private String documentId;

	private String bookingId;
	private double price;
	private Map<String, Integer> cafeItemsMap;
	private String bookerName;
	private String bookerContact;
	private String bookerEmail;
	private int racketsUsed;
	private int ballsUsed;
	private double discountAmount;
	private boolean isPlayoSelected;
	private boolean isKhelomoreSelected;
	private double cashPaid;
	private double upiPaid;
	private String membershipIdUsed;
	private Timestamp timestamp;
	private String status;
	private String date;
	private String time;
	private List<Integer> courts;
	private double startTime, endTime;
	private String bookerName_lowercase;
	private String venue;
	private Map<String, Double> hoursDeductionLedger;
	private boolean didNotShow = false;
	private boolean isHalfHour = false;

	public BookingDetails() {
		this.cafeItemsMap = new HashMap<>();
		this.courts = new ArrayList<>();
	}

	public BookingDetails(String bookingId, double price, Map<String, Integer> cafeItemsMap, String bookerName,
			String bookerContact, String bookerEmail, int racketsUsed, int ballsUsed, double discountAmount,
			boolean isPlayo, boolean isKhelomore, double cashPaid, double upiPaid) {
		this();
		this.bookingId = bookingId;
		this.price = price;
		this.cafeItemsMap = cafeItemsMap != null ? new HashMap<>(cafeItemsMap) : new HashMap<>();
		setBookerName(bookerName); // Use setter to handle lowercase
		this.bookerContact = bookerContact;
		this.bookerEmail = bookerEmail;
		this.racketsUsed = racketsUsed;
		this.ballsUsed = ballsUsed;
		this.discountAmount = discountAmount;
		this.isPlayoSelected = isPlayo;
		this.isKhelomoreSelected = isKhelomore;
		this.cashPaid = cashPaid;
		this.upiPaid = upiPaid;
		this.membershipIdUsed = null;
		this.timestamp = Timestamp.now();
		this.status = "completed";
	}

	public BookingDetails(BookingDetails other) {
		this.documentId = other.documentId;
		this.bookingId = other.bookingId;
		this.price = other.price;
		this.cafeItemsMap = new HashMap<>(other.cafeItemsMap != null ? other.cafeItemsMap : new HashMap<>());
		this.bookerName = other.bookerName;
		this.bookerName_lowercase = other.bookerName_lowercase;
		this.bookerContact = other.bookerContact;
		this.bookerEmail = other.bookerEmail;
		this.racketsUsed = other.racketsUsed;
		this.ballsUsed = other.ballsUsed;
		this.discountAmount = other.discountAmount;
		this.isPlayoSelected = other.isPlayoSelected;
		this.isKhelomoreSelected = other.isKhelomoreSelected;
		this.cashPaid = other.cashPaid;
		this.upiPaid = other.upiPaid;
		this.membershipIdUsed = other.membershipIdUsed;
		this.timestamp = other.timestamp;
		this.status = other.status;
		this.date = other.date;
		this.time = other.time;
		this.courts = new ArrayList<>(other.courts != null ? other.courts : new ArrayList<>());
		this.startTime = other.startTime;
		this.endTime = other.endTime;
		this.didNotShow = other.didNotShow;
		this.isHalfHour = other.isHalfHour;
		this.venue = other.venue;
		this.hoursDeductionLedger = other.hoursDeductionLedger != null ? new HashMap<>(other.hoursDeductionLedger)
				: null;
	}

	// --- Getters and Setters ---

	public String getDocumentId() {
		return documentId;
	}

	public void setDocumentId(String documentId) {
		this.documentId = documentId;
	}

	public String getBookingId() {
		return bookingId;
	}

	public void setBookingId(String bookingId) {
		this.bookingId = bookingId;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public Map<String, Integer> getCafeItemsMap() {
		return cafeItemsMap;
	}

	public void setCafeItemsMap(Map<String, Integer> cafeItemsMap) {
		this.cafeItemsMap = cafeItemsMap;
	}

	public String getBookerName() {
		return bookerName;
	}

	public void setBookerName(String bookerName) {
		this.bookerName = bookerName;
		if (bookerName != null) {
			this.bookerName_lowercase = bookerName.toLowerCase();
		} else {
			this.bookerName_lowercase = null;
		}
	}

	public String getBookerContact() {
		return bookerContact;
	}

	public void setBookerContact(String bookerContact) {
		this.bookerContact = bookerContact;
	}

	public String getBookerEmail() {
		return bookerEmail;
	}

	public void setBookerEmail(String bookerEmail) {
		this.bookerEmail = bookerEmail;
	}

	public int getRacketsUsed() {
		return racketsUsed;
	}

	public void setRacketsUsed(int racketsUsed) {
		this.racketsUsed = racketsUsed;
	}

	public int getBallsUsed() {
		return ballsUsed;
	}

	public void setBallsUsed(int ballsUsed) {
		this.ballsUsed = ballsUsed;
	}

	public double getDiscountAmount() {
		return discountAmount;
	}

	public void setDiscountAmount(double discountAmount) {
		this.discountAmount = discountAmount;
	}

	public boolean isPlayoSelected() {
		return isPlayoSelected;
	}

	public void setPlayoSelected(boolean isPlayoSelected) {
		this.isPlayoSelected = isPlayoSelected;
	}

	public boolean isKhelomoreSelected() {
		return isKhelomoreSelected;
	}

	public void setKhelomoreSelected(boolean isKhelomoreSelected) {
		this.isKhelomoreSelected = isKhelomoreSelected;
	}

	public double getCashPaid() {
		return cashPaid;
	}

	public void setCashPaid(double cashPaid) {
		this.cashPaid = cashPaid;
	}

	public double getUpiPaid() {
		return upiPaid;
	}

	public void setUpiPaid(double upiPaid) {
		this.upiPaid = upiPaid;
	}

	public String getMembershipIdUsed() {
		return membershipIdUsed;
	}

	public void setMembershipIdUsed(String membershipIdUsed) {
		this.membershipIdUsed = membershipIdUsed;
	}

	public Timestamp getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Timestamp timestamp) {
		this.timestamp = timestamp;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public List<Integer> getCourts() {
		return courts;
	}

	public void setCourts(List<Integer> courts) {
		this.courts = courts;
	}

	public double getStartTime() {
		return startTime;
	}

	public void setStartTime(double startTime) {
		this.startTime = startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public void setEndTime(double endTime) {
		this.endTime = endTime;
	}

	public String getBookerName_lowercase() {
		return bookerName_lowercase;
	}

	public void setBookerName_lowercase(String bookerName_lowercase) {
		this.bookerName_lowercase = bookerName_lowercase;
	}

	public String getVenue() {
		return venue;
	}

	public void setVenue(String venue) {
		this.venue = venue;
	}

	public Map<String, Double> getHoursDeductionLedger() {
		return hoursDeductionLedger;
	}

	public void setHoursDeductionLedger(Map<String, Double> hoursDeductionLedger) {
		this.hoursDeductionLedger = hoursDeductionLedger;
	}

	public boolean isDidNotShow() {
		return didNotShow;
	}

	public void setDidNotShow(boolean didNotShow) {
		this.didNotShow = didNotShow;
	}

	public boolean isHalfHour() {
		return isHalfHour;
	}

	public void setHalfHour(boolean isHalfHour) {
		this.isHalfHour = isHalfHour;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		BookingDetails that = (BookingDetails) o;
		return Objects.equals(documentId, that.documentId) && Objects.equals(bookingId, that.bookingId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(documentId, bookingId);
	}
}