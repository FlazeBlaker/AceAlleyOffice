// File: AceAlleyOffice/AceAlleyOffice/Core/FirebaseManager.java
package AceAlleyOffice.AceAlleyOffice.Core;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import java.io.IOException;
import java.io.InputStream;

public class FirebaseManager {

    private static Firestore firestoreDb;

    public static void initialize() {
        try (InputStream serviceAccount = FirebaseManager.class.getResourceAsStream("/serviceAccountKey.json")) {
            
            if (serviceAccount == null) {
                throw new IOException("FATAL ERROR: serviceAccountKey.json not found in resources folder.");
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
            
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }
            
            firestoreDb = FirestoreClient.getFirestore();
            System.out.println("Cloud Firestore has been initialized successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            // In a real app, show a JOptionPane error and call System.exit(1)
        }
    }

    public static Firestore getFirestore() {
        if (firestoreDb == null) {
            throw new IllegalStateException("Firestore must be initialized before getting a reference.");
        }
        return firestoreDb;
    }
}