// File: AceAlleyOffice/AceAlleyOffice/Core/SettingsUploader.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.json.JSONObject;
import com.google.cloud.firestore.Firestore;

/**
 * A one-time use program to upload a venue's pricing settings to Firestore.
 */
public class SettingsUploader {

    public static void main(String[] args) {
        // --- CONFIGURE UPLOAD DETAILS HERE ---
        // Change these values to upload settings for a different venue
        String settingsDocumentId = "viman_nagar"; 
        String configFileName = "settings_aundh.json";
        // --- END OF CONFIGURATION ---

        // 1. Initialize the connection to Firebase
        FirebaseManager.initialize();
        Firestore db = FirebaseManager.getFirestore();

        System.out.println("--- Starting Settings Upload for: " + settingsDocumentId + " ---");

        try {
            // 2. Read the specified JSON file from the resources folder
            InputStream is = SettingsUploader.class.getResourceAsStream("/" + configFileName);
            if (is == null) {
                throw new Exception("Could not find " + configFileName + " in the resources folder.");
            }
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            // 3. Convert the JSON string into a Map
            Map<String, Object> settingsData = new JSONObject(jsonText).toMap();

            // 4. Upload the data to Firestore with the specified document ID
            db.collection("settings").document(settingsDocumentId).set(settingsData).get();
            
            System.out.println("Successfully uploaded settings for venue: " + settingsDocumentId);

        } catch (Exception e) {
            System.err.println("Failed to upload settings configuration.");
            e.printStackTrace();
        }
        
        System.out.println("\nUpload process complete.");
    }
}