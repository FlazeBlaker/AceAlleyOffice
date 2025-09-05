// File: AceAlleyOffice/AceAlleyOffice/Core/User.java
package AceAlleyOffice.AceAlleyOffice.Core;

public class User {
    private String uid;
    private String userName;
    private String userEmail;
    private String role;
    private String venueId;

    public User(String uid) {
        this.uid = uid;
    }

    // --- Getters and Setters for all fields ---
    public String getUid() { return uid; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getVenueId() { return venueId; }
    public void setVenueId(String venueId) { this.venueId = venueId; }
}