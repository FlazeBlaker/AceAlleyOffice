package AceAlleyOffice.AceAlleyOffice.Interfaces;

public interface CourtBookingListener {
	void onBookCourt(int courtNumber);

	void onCancelCourt(int courtNumber);

	void onInfoCourt(int courtNumber);

	void onSlotClicked(int timeIndex, int courtNumber); // Make sure this line exists

	void onAddBookingToSlot(int courtNumber);

	void onNavigateBooking(int courtNumber, int direction); // <<< ADD THIS METHOD

}