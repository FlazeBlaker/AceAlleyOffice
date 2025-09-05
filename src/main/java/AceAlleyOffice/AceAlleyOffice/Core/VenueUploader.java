// File: AceAlleyOffice/AceAlleyOffice/Core/VenueUploader.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.json.JSONObject;
import com.google.cloud.firestore.Firestore;

public class VenueUploader {

    public static void main(String[] args) {
        // --- CONFIGURE UPLOAD DETAILS HERE ---
        // To upload for Aundh, use these values.
        // To upload for Viman Nagar again, change these back.
        String venueDocumentId = "aundh"; // The new Document ID for Aundh
        String configFileName = "venue_config.json"; // The new JSON file to read
        // --- END OF CONFIGURATION ---


        // 1. Initialize the connection to Firebase
        FirebaseManager.initialize();
        Firestore db = FirebaseManager.getFirestore();

        System.out.println("--- Starting Venue Configuration Upload for: " + venueDocumentId + " ---");

        try {
            // 2. Read the specified JSON file from the resources folder
            InputStream is = VenueUploader.class.getResourceAsStream("/" + configFileName);
            if (is == null) {
                throw new Exception(configFileName + " not found in resources folder.");
            }
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            // 3. Convert the JSON string into a Map
            Map<String, Object> venueData = new JSONObject(jsonText).toMap();

            // 4. Upload the data to Firestore with the specified document ID
            db.collection("venues").document(venueDocumentId).set(venueData).get();
            
            System.out.println("Successfully uploaded configuration for venue: " + venueDocumentId);

        } catch (Exception e) {
            System.err.println("Failed to upload venue configuration.");
            e.printStackTrace();
        }
        
        System.out.println("\nUpload process complete.");
    }
}