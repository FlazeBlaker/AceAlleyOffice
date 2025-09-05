// File: AceAlleyOffice/AceAlleyOffice/Core/AppInitializer.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.swing.JOptionPane;
import javax.swing.SwingWorker;

import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.PricingSettings;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.LoadingDialog;

// --- THIS IS THE FIX: Changed from SwingWorker<Void, String> to SwingWorker<Main, String> ---
public class AppInitializer extends SwingWorker<Main, String> {

    private final LoadingDialog splash;
    private final User loggedInUser;
    private final FirebaseDataManager dataManager;
    private final String venueIdForSession;

    public AppInitializer(LoadingDialog splash, User user, FirebaseDataManager manager, String venueId) {
        this.splash = splash;
        this.loggedInUser = user;
        this.dataManager = manager;
        this.venueIdForSession = venueId;
    }

 // In AppInitializer.java

    @Override
    protected Main doInBackground() throws Exception {
        publish("Loading venue configuration...");
        VenueConfig venueConfig = dataManager.fetchVenueConfig(venueIdForSession);
        if (venueConfig == null) { throw new Exception("Venue config not found for " + venueIdForSession); }

        // --- NEW: Synchronously fetch pricing settings ---
        publish("Loading pricing settings...");
        PricingSettings settings = dataManager.fetchPricingSettings(venueIdForSession);
        if (settings == null) {
            // If settings are not found, throw a fatal error
            throw new Exception("CRITICAL: Pricing settings not found for venue '" + venueIdForSession + "'");
        }

        publish("Building user interface...");
        CountDownLatch latch = new CountDownLatch(1);
        
        // --- NEW: Pass the loaded settings to the Main constructor ---
        Main mainWindow = new Main(latch, dataManager, venueConfig, loggedInUser, venueIdForSession, settings);
        
        publish("Loading initial data...");
        latch.await();

        publish("Finalizing...");
        Thread.sleep(500);
        
        return mainWindow;
    }

    @Override
    protected void process(List<String> chunks) {
        splash.setStatusMessage(chunks.get(chunks.size() - 1));
    }
    
    @Override
    protected void done() {
        splash.dispose(); // Close the loading screen
        try {
            Main mainWindow = get(); // Get the fully loaded main window from the background task
            
            // Perform final, quick UI setup
            mainWindow.getUiBuilder().getDateChooser().setDate(new Date());
            
            mainWindow.setVisible(true); // And show it
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Failed to start the application after loading:\n" + e.getMessage(), "Application Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
}