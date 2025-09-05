// File: AceAlleyOffice/AceAlleyOffice/Core/VenueConfig.java
package AceAlleyOffice.AceAlleyOffice.Core;

// This class must match the structure of your Firestore document
public class VenueConfig {

    private String venueName;
    private int numberOfCourts;
    private String pricingStrategy;
    private Features features;

    // A nested static class for the 'features' map
    public static class Features {
        private boolean hasCafe;
        private boolean hasGearRental;
        private boolean hasMemberships;
        
        public Features() {} // Needed for Firestore
        
        // Getters
        public boolean isHasCafe() { return hasCafe; }
        public boolean isHasGearRental() { return hasGearRental; }
        public boolean isHasMemberships() { return hasMemberships; }
    }

    public VenueConfig() {} // Needed for Firestore

    // Getters for the main class
    public String getVenueName() { return venueName; }
    public int getNumberOfCourts() { return numberOfCourts; }
    public String getPricingStrategy() { return pricingStrategy; }
    public Features getFeatures() { return features; }
}