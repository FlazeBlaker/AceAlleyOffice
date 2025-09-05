// File: AceAlleyOffice/AceAlleyOffice/Core/ConnectivityManager.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import AceAlleyOffice.AceAlleyOffice.Main.Main;

public class ConnectivityManager {

    private final Main mainApp;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicBoolean isConnected = new AtomicBoolean(true); // Assume connected at start

    public ConnectivityManager(Main mainApp) {
        this.mainApp = mainApp;
    }

    /**
     * Starts the periodic check for internet connectivity.
     */
 // In ConnectivityManager.java

    public void start() {
        // Using new Runnable() here is safer for compatibility
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkInternetStatus();
            }
        }, 5, 5, TimeUnit.SECONDS);
    }

    /**
     * Stops the connectivity checker when the application closes.
     */
    public void stop() {
        scheduler.shutdown();
    }

    /**
     * The core logic that runs periodically to check for a connection.
     */
 // In ConnectivityManager.java, replace your existing checkInternetStatus method

 // In ConnectivityManager.java, replace your existing checkInternetStatus method

    private void checkInternetStatus() {
        // --- NEW: Show the "checking..." indicator BEFORE starting the check ---
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainApp.showConnectivityCheckIndicator();
            }
        });

        boolean currentlyConnected = isInternetAvailable();

        // --- NEW: Hide the indicator AFTER the check is complete ---
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                mainApp.hideConnectivityCheckIndicator();
            }
        });
        
        // The rest of the logic for handling connection status change
        if (isConnected.compareAndSet(!currentlyConnected, currentlyConnected)) {
            if (currentlyConnected) {
                System.out.println("Internet connection RESTORED.");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mainApp.hideOfflineOverlay();
                    }
                });
            } else {
                System.err.println("Internet connection LOST.");
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        mainApp.showOfflineOverlay();
                    }
                });
            }
        }
    }

    /**
     * Tries to open a socket to a reliable host (Google's DNS) to check for a live connection.
     * @return true if the connection is successful, false otherwise.
     */
    private boolean isInternetAvailable() {
        try (Socket socket = new Socket()) {
            // Connect to Google's public DNS server on port 53 (DNS) with a 3-second timeout
            socket.connect(new InetSocketAddress("8.8.8.8", 53), 3000);
            return true;
        } catch (Exception e) {
            return false; // Connection failed
        }
    }
}