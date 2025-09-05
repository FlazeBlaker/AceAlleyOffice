package AceAlleyOffice.AceAlleyOffice.Core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONObject;

import com.google.cloud.firestore.Firestore;

// This is a one-time use program to upload your items to Firestore.
public class FirestoreUploader {

    public static void main(String[] args) {
        FirebaseManager.initialize();
        Firestore db = FirebaseManager.getFirestore();

        System.out.println("Starting Firestore upload...");

        // 2. Upload items from each file
        uploadItems(db, "cafe_items", "cafe_item_prices.json");
        uploadItems(db, "gear_items", "gear_item_prices.json");

        System.out.println("\nUpload process complete. You can now close this program.");
    }

    /**
     * Reads a JSON file, parses it, and uploads each item as a new document
     * to the specified Firestore collection.
     */
    private static void uploadItems(Firestore db, String collectionName, String jsonFileName) {
        System.out.println("\n--- Uploading to collection: '" + collectionName + "' ---");
        try {
            // 3. Read the JSON file from the resources folder
            InputStream is = FirestoreUploader.class.getResourceAsStream("/" + jsonFileName);
            String jsonText = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            
            // 4. Parse the JSON string
            JSONObject jsonObject = new JSONObject(jsonText);

            // 5. Loop through each item in the JSON and upload it
            Iterator<String> keys = jsonObject.keys();
            while(keys.hasNext()) {
                String itemName = keys.next();
                double itemPrice = jsonObject.getDouble(itemName);

                // Create a Map representing the document to be saved
                Map<String, Object> docData = new HashMap<>();
                docData.put("name", itemName);
                docData.put("price", itemPrice);

                // Add the new document to the collection
                db.collection(collectionName).add(docData);
                System.out.println("Uploaded: " + itemName);
            }

        } catch (Exception e) {
            System.err.println("Failed to upload items from " + jsonFileName);
            e.printStackTrace();
        }
    }
}