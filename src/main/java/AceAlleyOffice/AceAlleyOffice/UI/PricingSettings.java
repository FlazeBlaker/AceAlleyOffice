// File: AceAlleyOffice/AceAlleyOffice/UI/PricingSettings.java
package AceAlleyOffice.AceAlleyOffice.UI;

public class PricingSettings {
	private double court_weekday_regular = 600;
	private double court_weekday_happy = 400;
	private double court_weekend = 650;
	private double racket_regular = 50;
	private double racket_member = 30;
	private double ball_regular = 30;
	private double ball_member = 20;
	private double court_happy_hour_start = 11;
	private double court_happy_hour_end = 15;
	private String admin_password = "password";

    // Field added to remove Firestore warnings
    private String venue;

	// No-argument constructor required by Firestore
	public PricingSettings() {}

	// --- Getters and Setters ---

	public String getAdmin_password() {
		return admin_password;
	}

	public void setAdmin_password(String admin_password) {
		this.admin_password = admin_password;
	}

	public double getCourt_happy_hour_start() {
		return court_happy_hour_start;
	}

	public void setCourt_happy_hour_start(double court_happy_hour_start) {
		this.court_happy_hour_start = court_happy_hour_start;
	}

	public double getCourt_happy_hour_end() {
		return court_happy_hour_end;
	}

	public void setCourt_happy_hour_end(double court_happy_hour_end) {
		this.court_happy_hour_end = court_happy_hour_end;
	}

	public double getCourt_weekday_regular() {
		return court_weekday_regular;
	}

	public void setCourt_weekday_regular(double court_weekday_regular) {
		this.court_weekday_regular = court_weekday_regular;
	}

	public double getCourt_weekday_happy() {
		return court_weekday_happy;
	}

	public void setCourt_weekday_happy(double court_weekday_happy) {
		this.court_weekday_happy = court_weekday_happy;
	}

	public double getCourt_weekend() {
		return court_weekend;
	}

	public void setCourt_weekend(double court_weekend) {
		this.court_weekend = court_weekend;
	}

	public double getRacket_regular() {
		return racket_regular;
	}

	public void setRacket_regular(double racket_regular) {
		this.racket_regular = racket_regular;
	}

	public double getRacket_member() {
		return racket_member;
	}

	public void setRacket_member(double racket_member) {
		this.racket_member = racket_member;
	}

	public double getBall_regular() {
		return ball_regular;
	}

	public void setBall_regular(double ball_regular) {
		this.ball_regular = ball_regular;
	}

	public double getBall_member() {
		return ball_member;
	}

	public void setBall_member(double ball_member) {
		this.ball_member = ball_member;
	}

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }
}