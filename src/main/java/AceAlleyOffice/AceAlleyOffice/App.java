// File: AceAlleyOffice/AceAlleyOffice/App.java
package AceAlleyOffice.AceAlleyOffice;

import javax.swing.*;
import AceAlleyOffice.AceAlleyOffice.Core.*;
import AceAlleyOffice.AceAlleyOffice.Main.Main;
import AceAlleyOffice.AceAlleyOffice.UI.Dialogs.*;
import java.util.List;

public class App {
	public static void main(String[] args) {
		Main.applyModernTheme();
		SwingUtilities.invokeLater(() -> {
			Main.applyModernTheme();
			FirebaseManager.initialize();

			// Create a temporary manager with a null venue, just for authentication
			FirebaseDataManager initialDataManager = new FirebaseDataManager(null);
			String firebaseWebApiKey = "AIzaSyCI1oNwQfXlF48m10c_47NjX-IJbvE-Oz0";

			LoginDialog loginDialog = new LoginDialog(null, initialDataManager, firebaseWebApiKey);
			User loggedInUser = loginDialog.showDialogAndGetUser();

			if (loggedInUser == null) {
				System.exit(0);
				return;
			}

			// Dynamic Venue Logic
			if ("admin".equals(loggedInUser.getRole())) {
				initialDataManager.fetchAllVenueConfigs(venues -> {
					if (venues.isEmpty()) {
						// --- THIS IS THE CORRECTED ERROR HANDLING ---
						JOptionPane.showMessageDialog(null,
								"No venues are configured in the database. Application cannot start.",
								"Configuration Error", JOptionPane.ERROR_MESSAGE);
						System.exit(0);
						return;
					}

					VenueSelectionDialog selectionDialog = new VenueSelectionDialog(null, venues);
					String selectedVenueId = selectionDialog.showDialogAndGetVenueId();

					if (selectedVenueId != null) {
						// Create the REAL data manager with the selected venue ID
						FirebaseDataManager sessionDataManager = new FirebaseDataManager(selectedVenueId);
						launchApplicationForVenue(loggedInUser, sessionDataManager, selectedVenueId);
					} else {
						System.exit(0);
					}
				});
			} else if ("staff".equals(loggedInUser.getRole())) {
				String staffVenueId = loggedInUser.getVenueId();
				if (staffVenueId != null && !staffVenueId.isEmpty()) {
					// Create the REAL data manager with the staff's assigned venue ID
					FirebaseDataManager sessionDataManager = new FirebaseDataManager(staffVenueId);
					launchApplicationForVenue(loggedInUser, sessionDataManager, staffVenueId);
				} else {
					JOptionPane.showMessageDialog(null, "This staff account is not assigned to a venue.",
							"Access Denied", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			} else {
				JOptionPane.showMessageDialog(null, "Unknown user role.", "Access Denied", JOptionPane.ERROR_MESSAGE);
				System.exit(0);
			}
		});
	}

	private static void launchApplicationForVenue(User user, FirebaseDataManager dataManager, String venueId) {
		final LoadingDialog loadingDialog = new LoadingDialog(null);
		AppInitializer initializer = new AppInitializer(loadingDialog, user, dataManager, venueId);
		initializer.execute();
		loadingDialog.setVisible(true);
	}
}