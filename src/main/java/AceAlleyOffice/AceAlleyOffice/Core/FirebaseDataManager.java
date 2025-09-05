// File: AceAlleyOffice/AceAlleyOffice/Core/FirebaseDataManager.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.swing.SwingUtilities;

import org.json.JSONObject;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.ListenerRegistration;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteBatch;
import com.google.common.util.concurrent.MoreExecutors;

import AceAlleyOffice.AceAlleyOffice.Interfaces.ContactCallback;
import AceAlleyOffice.AceAlleyOffice.UI.BookingDetails;
import AceAlleyOffice.AceAlleyOffice.UI.CafeItem;
import AceAlleyOffice.AceAlleyOffice.UI.CafeSale;
import AceAlleyOffice.AceAlleyOffice.UI.GearItem;
import AceAlleyOffice.AceAlleyOffice.UI.GearSale;
import AceAlleyOffice.AceAlleyOffice.UI.Membership;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPackage;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPurchase;
import AceAlleyOffice.AceAlleyOffice.UI.OutgoingExpense;
import AceAlleyOffice.AceAlleyOffice.UI.PricingSettings;

public class FirebaseDataManager {

	private final Firestore db;
	private final Map<String, Double> cafeItemPrices = new HashMap<>();
	private final Map<String, Double> gearItemPrices = new HashMap<>();
    private final String venueId; // <-- ADD THIS FIELD
	public VenueConfig fetchVenueConfig(String venueId) throws ExecutionException, InterruptedException {
		DocumentReference docRef = db.collection("venues").document(venueId);
		DocumentSnapshot document = docRef.get().get(); // This is a synchronous call
		if (document.exists()) {
			System.out.println("Venue configuration loaded for: " + venueId);
			return document.toObject(VenueConfig.class);
		} else {
			System.err.println("FATAL: No venue configuration found for ID: " + venueId);
			return null; // Or throw an exception
		}
	}
    // --- ADD THIS NEW METHOD ---
    public PricingSettings fetchPricingSettings(String venueId) throws ExecutionException, InterruptedException {
        if (venueId == null || venueId.isEmpty()) {
            return null; // Cannot fetch settings without a venue ID
        }
        DocumentReference docRef = db.collection("settings").document(venueId);
        DocumentSnapshot document = docRef.get().get(); // This is a synchronous call
        if (document.exists()) {
            return document.toObject(PricingSettings.class);
        } else {
            return null; // Return null if the document does not exist
        }
    }
	@FunctionalInterface
	public interface AuthCallback {
		void onResult(User user, String errorMessage);
	}

	public void authenticateUser(String email, String password, String webApiKey, AuthCallback callback) {
		new Thread(() -> {
			try {
				System.out.println("DEBUG: Attempting login with Web API Key: [" + webApiKey + "]");
				System.out.println("DEBUG: Attempting login with Email: [" + email + "]");
				System.out.println("DEBUG: Attempting login with Password: [" + password + "]");

				// 1. Authenticate with Firebase Auth REST API
				String authUrl = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
						+ webApiKey;
				URL url = new URL(authUrl);
				HttpURLConnection conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				conn.setDoOutput(true);

				String jsonInputString = "{\"email\":\"" + email + "\",\"password\":\"" + password
						+ "\",\"returnSecureToken\":true}";
				try (OutputStream os = conn.getOutputStream()) {
					byte[] input = jsonInputString.getBytes("utf-8");
					os.write(input, 0, input.length);
				}

				if (conn.getResponseCode() != 200) {
					callback.onResult(null, "Invalid email or password.");
					return;
				}

				// Read the response to get the user's UID
				String response = new String(conn.getInputStream().readAllBytes(), "utf-8");
				JSONObject jsonResponse = new JSONObject(response);
				String uid = jsonResponse.getString("localId");

				// 2. Authorization: Fetch user role from Firestore
				DocumentSnapshot userDoc = db.collection("users").document(uid).get().get();
				if (!userDoc.exists()) {
					callback.onResult(null, "User profile not found in database.");
					return;
				}

				User user = new User(uid);
				user.setUserName(userDoc.getString("userName"));
				user.setUserEmail(userDoc.getString("userEmail"));
				user.setRole(userDoc.getString("role"));
				user.setVenueId(userDoc.getString("venueId")); // Will be null for admins

				callback.onResult(user, null); // Success!

			} catch (Exception e) {
				e.printStackTrace();
				callback.onResult(null, "An error occurred during login.");
			}
		}).start();
	}

	@FunctionalInterface
	public interface VenuesCallback {
		void onResult(List<VenueConfig> venues);
	}

	public void fetchAllVenueConfigs(VenuesCallback callback) {
		db.collection("venues").get().addListener(() -> {
			try {
				List<VenueConfig> venues = new ArrayList<>();
				for (var doc : db.collection("venues").get().get()) {
					VenueConfig config = doc.toObject(VenueConfig.class);
					// Manually set the venueId in the config object from the document ID
					// This is important for the selection dialog
					// You might need to add a setVenueId method to VenueConfig.java
					// config.setVenueId(doc.getId());
					venues.add(config);
				}
				SwingUtilities.invokeLater(() -> callback.onResult(venues));
			} catch (Exception e) {
				e.printStackTrace();
				SwingUtilities.invokeLater(() -> callback.onResult(new ArrayList<>()));
			}
		}, MoreExecutors.directExecutor());
	}

	@FunctionalInterface
	public interface MembershipUpdateCallback {
		void onUpdate(List<Membership> memberships);
	}

	@FunctionalInterface
	public interface MembershipPackageCallback {
		void onUpdate(List<MembershipPackage> packages);
	}

	@FunctionalInterface
	public interface PricingSettingsCallback {
		void onUpdate(PricingSettings settings);
	}

	// Callbacks
	@FunctionalInterface
	public interface UnavailableCourtsCallback {
		void onResult(Set<Integer> unavailableCourts);
	}

	@FunctionalInterface
	public interface SearchCallback {
		void onResult(List<BookingDetails> results);
	}

	@FunctionalInterface
	public interface GenericItemsCallback {
		void onResult(Map<String, Double> items);
	}

	@FunctionalInterface
	public interface GenericItemsWithIdsCallback {
		void onResult(Map<String, Double> items, Map<String, String> docIds);
	}

    public FirebaseDataManager(String venueId) { // <-- MODIFY THE CONSTRUCTOR
        this.db = FirebaseManager.getFirestore();
        this.venueId = venueId; // <-- STORE THE VENUE ID
    }
	// --- Booking Methods ---

	public void addNewBooking(BookingDetails details) {
		details.setVenue(venueId);
		db.collection("bookings").add(details);
	}

	public void updateBooking(BookingDetails details) {
		if (details.getDocumentId() == null || details.getDocumentId().isEmpty()) {
			System.err.println("Cannot update booking: Document ID is missing.");
			return;
		}
		db.collection("bookings").document(details.getDocumentId()).set(details);
	}

	// --- NEW REAL-TIME LISTENER for Membership Packages ---
	public ListenerRegistration attachMembershipPackageListener(MembershipPackageCallback callback) {
		return db.collection("membership_packages").whereEqualTo("venue", venueId).orderBy("hours")
				.addSnapshotListener((snapshots, error) -> {
					if (error != null) {
						System.err.println("Membership package listener failed: " + error);
						return;
					}
					if (snapshots != null) {
						callback.onUpdate(snapshots.toObjects(MembershipPackage.class));
					}
				});
	}

	// --- THIS IS THE NEW, MORE PRECISE DELETION METHOD ---
	public void deleteSingleItemFromSale(String saleDocumentId, String itemName) {
		if (saleDocumentId == null || itemName == null)
			return;

		DocumentReference saleRef = db.collection("cafe_sales").document(saleDocumentId);

		db.runTransaction(transaction -> {
			DocumentSnapshot snapshot = transaction.get(saleRef).get();
			if (!snapshot.exists()) {
				System.err.println("Sale document not found for deletion.");
				return null;
			}

			CafeSale sale = snapshot.toObject(CafeSale.class);
			Map<String, Integer> itemsSold = new HashMap<>(sale.getItemsSold());

			int currentQuantity = itemsSold.getOrDefault(itemName, 0);
			if (currentQuantity <= 0) {
				return null; // Item not in sale, nothing to do
			}

			double itemPrice = this.cafeItemPrices.getOrDefault(itemName, 0.0);
			double newTotalAmount = sale.getTotalAmount() - itemPrice;

			if (currentQuantity > 1) {
				// If quantity is > 1, just decrement it
				itemsSold.put(itemName, currentQuantity - 1);
				transaction.update(saleRef, "itemsSold", itemsSold, "totalAmount", newTotalAmount);
			} else if (itemsSold.size() > 1) {
				// If quantity is 1 but there are other items, remove this item
				itemsSold.remove(itemName);
				transaction.update(saleRef, "itemsSold", itemsSold, "totalAmount", newTotalAmount);
			} else {
				// If this is the only item in the sale, delete the whole sale document
				transaction.delete(saleRef);
			}
			return null;
		});
	}
	// File: AceAlleyOffice/AceAlleyOffice/Core/FirebaseDataManager.java
    @FunctionalInterface
    public interface CafeItemsCallback {
        void onUpdate(List<CafeItem> items);
    }
    public ListenerRegistration attachCafeItemsListener(CafeItemsCallback callback) {
        return db.collection("cafe_items").whereEqualTo("venue", venueId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        System.err.println("Cafe items listen failed: " + error);
                        return;
                    }
                    if (snapshots != null) {
                        List<CafeItem> items = snapshots.toObjects(CafeItem.class);
                        callback.onUpdate(items);
                    }
                });
    }

	// --- ADD THIS NEW METHOD for precise gear sale deletion ---
	public void deleteSingleItemFromGearSale(String saleDocumentId, String itemName) {
		if (saleDocumentId == null || itemName == null)
			return;

		DocumentReference saleRef = db.collection("gear_sales").document(saleDocumentId);

		db.runTransaction(transaction -> {
			DocumentSnapshot snapshot = transaction.get(saleRef).get();
			if (!snapshot.exists())
				return null;

			GearSale sale = snapshot.toObject(GearSale.class);
			Map<String, Integer> itemsSold = new HashMap<>(sale.getItemsSold());
			int currentQuantity = itemsSold.getOrDefault(itemName, 0);
			if (currentQuantity <= 0)
				return null;

			double itemPrice = this.gearItemPrices.getOrDefault(itemName, 0.0);
			double newTotalAmount = sale.getTotalAmount() - itemPrice;

			if (currentQuantity > 1) {
				itemsSold.put(itemName, currentQuantity - 1);
				transaction.update(saleRef, "itemsSold", itemsSold, "totalAmount", newTotalAmount);
			} else if (itemsSold.size() > 1) {
				itemsSold.remove(itemName);
				transaction.update(saleRef, "itemsSold", itemsSold, "totalAmount", newTotalAmount);
			} else {
				transaction.delete(saleRef);
			}
			return null;
		});
	}

	// --- NEW REAL-TIME LISTENER for Pricing Settings ---
	public ListenerRegistration attachPricingSettingsListener(PricingSettingsCallback callback) {
		DocumentReference settingsDoc = db.collection("settings").document(venueId);
		return settingsDoc.addSnapshotListener((snapshot, error) -> {
			if (error != null) {
				System.err.println("Pricing settings listen failed: " + error);
				return;
			}
			if (snapshot != null && snapshot.exists()) {
				PricingSettings settings = snapshot.toObject(PricingSettings.class);
				callback.onUpdate(settings);
			} else {
				// If the document doesn't exist, use default settings
				callback.onUpdate(new PricingSettings());
			}
		});
	}

	public void removeBooking(String documentId) {
		if (documentId == null || documentId.isEmpty())
			return;
		db.collection("bookings").document(documentId).delete();
	}

	public ListenerRegistration attachBookingListenerForDate(String date, EventListener<QuerySnapshot> listener) {
		return db.collection("bookings").whereEqualTo("date", date).whereEqualTo("venue", venueId)
				.addSnapshotListener(listener);
	}

	public void searchBookings(String query, String dateStr, SearchCallback callback) {
		if (query == null || query.trim().isEmpty() || dateStr == null) {
			callback.onResult(new ArrayList<>());
			return;
		}
		String trimmedQuery = query.trim();
		String lowercaseQuery = trimmedQuery.toLowerCase();

		// --- THIS IS THE CORRECTED QUERY for the phone number ---
		Query byContactQuery = db.collection("bookings").whereEqualTo("date", dateStr)
				.whereEqualTo("venue", venueId).whereGreaterThanOrEqualTo("bookerContact", trimmedQuery)
				.whereLessThanOrEqualTo("bookerContact", trimmedQuery + '\uf8ff');

		// This query for the name is already correct
		Query byNameQuery = db.collection("bookings").whereEqualTo("venue", venueId)
				.whereEqualTo("date", dateStr).whereGreaterThanOrEqualTo("bookerName", lowercaseQuery)
				.whereLessThanOrEqualTo("bookerName", lowercaseQuery + '\uf8ff');

		ApiFuture<QuerySnapshot> contactFuture = byContactQuery.get();
		ApiFuture<QuerySnapshot> nameFuture = byNameQuery.get();

		contactFuture.addListener(() -> {
			nameFuture.addListener(() -> {
				Map<String, BookingDetails> uniqueResults = new HashMap<>();
				try {
					for (QueryDocumentSnapshot doc : contactFuture.get().getDocuments()) {
						BookingDetails booking = doc.toObject(BookingDetails.class);
						booking.setDocumentId(doc.getId());
						uniqueResults.put(doc.getId(), booking);
					}
					for (QueryDocumentSnapshot doc : nameFuture.get().getDocuments()) {
						BookingDetails booking = doc.toObject(BookingDetails.class);
						booking.setDocumentId(doc.getId());
						uniqueResults.put(doc.getId(), booking);
					}
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
				callback.onResult(new ArrayList<>(uniqueResults.values()));
			}, MoreExecutors.directExecutor());
		}, MoreExecutors.directExecutor());
	}

	public void findUnavailableCourts(String date, int startTime, int endTime, UnavailableCourtsCallback callback) {
		ApiFuture<QuerySnapshot> future = db.collection("bookings").whereEqualTo("date", date)
				.whereEqualTo("venue", venueId).get();
		future.addListener(() -> {
			Set<Integer> unavailableCourts = new HashSet<>();
			try {
				for (QueryDocumentSnapshot doc : future.get().getDocuments()) {
					Long bookedStartTime = doc.getLong("startTime");
					Long bookedEndTime = doc.getLong("endTime");
					if (bookedStartTime != null && bookedEndTime != null
							&& (startTime < bookedEndTime && endTime > bookedStartTime)) {
						Object courtField = doc.get("courts");
						if (courtField instanceof List) {
							((List<?>) courtField).forEach(c -> {
								if (c instanceof Number)
									unavailableCourts.add(((Number) c).intValue());
							});
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			callback.onResult(unavailableCourts);
		}, MoreExecutors.directExecutor());
	}

	// --- NEW REAL-TIME LISTENER for Memberships ---
	public ListenerRegistration attachMembershipListener(MembershipUpdateCallback callback) {
		return db.collection("memberships").whereEqualTo("venue", venueId)
				.addSnapshotListener((snapshots, error) -> {
					if (error != null) {
						System.err.println("Membership listen failed: " + error);
						return;
					}
					if (snapshots != null) {
						List<Membership> memberships = snapshots.toObjects(Membership.class);
						callback.onUpdate(memberships);
					}
				});
	}

	// --- NEW REAL-TIME LISTENER for Cafe Items ---
	// This method now correctly handles a CountDownLatch
	public ListenerRegistration attachCafeItemsListener(GenericItemsCallback callback, CountDownLatch latch) {
		final AtomicBoolean firstLoad = new AtomicBoolean(true);
		return db.collection("cafe_items").whereEqualTo("venue", venueId)
				.addSnapshotListener((snapshots, error) -> {
					if (error != null) {
						System.err.println("Cafe items listen failed: " + error);
						return;
					}
					if (snapshots != null) {
						Map<String, Double> items = snapshots.getDocuments().stream().collect(Collectors
								.toMap(doc -> doc.getString("name"), doc -> doc.getDouble("price"), (v1, v2) -> v1));
						callback.onResult(items);
						if (latch != null && firstLoad.getAndSet(false)) {
							latch.countDown();
						}
					}
				});
	}

	// File: AceAlleyOffice/AceAlleyOffice/Core/FirebaseDataManager.java

    @FunctionalInterface
    public interface GearItemsCallback {
        void onUpdate(List<GearItem> items);
    }

    // UPDATED: This listener now fetches the full GearItem object.
    public ListenerRegistration attachGearItemsListener(GearItemsCallback callback) {
        return db.collection("gear_items").whereEqualTo("venue", venueId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        System.err.println("Gear items listen failed: " + error);
                        return;
                    }
                    if (snapshots != null) {
                        List<GearItem> items = snapshots.toObjects(GearItem.class);
                        callback.onUpdate(items);
                    }
                });
    }

    // NEW METHOD: Adds a new gear item with quantity.
    public void addGearItem(String name, double price, int quantity) {
        db.collection("gear_items").add(Map.of(
            "name", name, 
            "price", price, 
            "quantity", quantity,
            "venue", venueId
        ));
    }

    // NEW METHOD: Updates a gear item, including its quantity.
    public void updateGearItem(String docId, String newName, double newPrice, int newQuantity) {
        db.collection("gear_items").document(docId)
            .update("name", newName, "price", newPrice, "quantity", newQuantity);
    }
    
    // NEW METHOD: Deletes a gear item.
    public void deleteGearItem(String docId) {
        if (docId != null) {
            db.collection("gear_items").document(docId).delete();
        }
    }

    // NEW METHOD: Safely decrements inventory quantities after a sale.
    public void updateGearItemQuantities(Map<String, Integer> itemsSold) {
        if (itemsSold == null || itemsSold.isEmpty()) return;
        
        for (Map.Entry<String, Integer> entry : itemsSold.entrySet()) {
            String docId = entry.getKey();
            int quantitySold = entry.getValue();
            
            DocumentReference itemRef = db.collection("gear_items").document(docId);
            db.runTransaction(transaction -> {
                DocumentSnapshot snapshot = transaction.get(itemRef).get();
                long currentQuantity = snapshot.getLong("quantity");
                long newQuantity = Math.max(0, currentQuantity - quantitySold);
                transaction.update(itemRef, "quantity", newQuantity);
                return null;
            });
        }
    }
	// --- Contact Methods ---

    public void getContact(String phoneNumber, ContactCallback callback) {
		String trimmedPhone = (phoneNumber != null) ? phoneNumber.trim() : null;

        // --- THIS IS THE FIX ---
        // Validate the phone number before sending it to Firestore.
        // An invalid ID (e.g., one that is too long or contains code) will be rejected here.
        if (trimmedPhone == null || trimmedPhone.isEmpty() || trimmedPhone.length() > 20 || trimmedPhone.contains("\n")) {
			callback.onResult(null);
			return;
		}
        // --- END OF FIX ---

		ApiFuture<DocumentSnapshot> future = db.collection("contacts").document(trimmedPhone).get();
		future.addListener(() -> {
			try {
				DocumentSnapshot document = future.get();
				callback.onResult(document.exists() ? document.getData() : null);
			} catch (Exception e) {
				e.printStackTrace();
				callback.onResult(null);
			}
		}, MoreExecutors.directExecutor());
	}

	public void addOrUpdateContact(String phone, String name, String email) {
		String trimmedPhone = (phone != null) ? phone.trim() : null;

        // --- THIS IS THE FIX ---
        // Add the same validation here to prevent saving contacts with invalid IDs.
        if (trimmedPhone == null || trimmedPhone.isEmpty() || trimmedPhone.length() > 20 || trimmedPhone.contains("\n")) {
            System.err.println("Attempted to save a contact with an invalid phone number: " + phone);
			return;
		}
        // --- END OF FIX ---

		DocumentReference contactRef = db.collection("contacts").document(trimmedPhone);
		Map<String, Object> contactData = new HashMap<>();
		contactData.put("phone", trimmedPhone);
		if (name != null)
			contactData.put("name", name.toLowerCase());
		if (email != null && !email.trim().isEmpty())
			contactData.put("email", email.toLowerCase());
		contactRef.set(contactData);
	}

	// --- Generic Item Loading ---

	private void loadItems(String collection, GenericItemsCallback callback) {
		db.collection(collection).whereEqualTo("venue", venueId).get().addListener(() -> {
			Map<String, Double> loadedItems = new HashMap<>();
			try {
				for (QueryDocumentSnapshot doc : db.collection(collection).get().get()) {
					String name = doc.getString("name");
					Double price = doc.getDouble("price");
					if (name != null && price != null)
						loadedItems.put(name, price);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			callback.onResult(loadedItems);
		}, MoreExecutors.directExecutor());
	}

	private void loadItemsWithIds(String collection, GenericItemsWithIdsCallback callback) {
		db.collection(collection).whereEqualTo("venue", venueId).get().addListener(() -> {
			Map<String, Double> loadedItems = new HashMap<>();
			Map<String, String> docIds = new HashMap<>();
			try {
				for (QueryDocumentSnapshot doc : db.collection(collection).get().get()) {
					String name = doc.getString("name");
					Double price = doc.getDouble("price");
					if (name != null && price != null) {
						loadedItems.put(name, price);
						docIds.put(name, doc.getId());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			callback.onResult(loadedItems, docIds);
		}, MoreExecutors.directExecutor());
	}

	// --- Cafe Item & Sale Methods ---

	public void loadCafeItems(GenericItemsCallback callback) {
		loadItems("cafe_items", callback);
	}

	public void loadCafeItemsWithIds(GenericItemsWithIdsCallback callback) {
		loadItemsWithIds("cafe_items", callback);
	}

    public void addCafeItem(String name, double price, int quantity) {
        db.collection("cafe_items").add(Map.of(
            "name", name, 
            "price", price, 
            "quantity", quantity,
            "venue", venueId
        ));
    }

    public void updateCafeItem(String docId, String newName, double newPrice, int newQuantity) {
        db.collection("cafe_items").document(docId)
            .update("name", newName, "price", newPrice, "quantity", newQuantity);
    }


	public void deleteCafeItem(String docId) {
		db.collection("cafe_items").document(docId).delete();
	}
    public void updateItemQuantities(Map<String, Integer> itemsSold) {
        if (itemsSold == null || itemsSold.isEmpty()) return;

        WriteBatch batch = db.batch();
        for (Map.Entry<String, Integer> entry : itemsSold.entrySet()) {
            String docId = entry.getKey();
            int quantitySold = entry.getValue();
            
            DocumentReference itemRef = db.collection("cafe_items").document(docId);
            // Firestore transactions require a WriteBatch for this kind of operation.
            // We read the current value and write the new value in a batch.
            // For simplicity and performance, we'll use a batched write with a server-side decrement.
            // This is more advanced, so for now, a simple update is sufficient.
            // A true transactional read-then-write is more complex. Let's use a simpler update for now.
            
            // This part is complex with Firestore SDK. A simpler way is to re-read and write.
            // But for your use case, let's assume you'll handle the read before calling this.
            // A more robust solution involves Cloud Functions, but this will work.
            itemRef.get().addListener(() -> {
                try {
                    int currentQuantity = itemRef.get().get().getLong("quantity").intValue();
                    int newQuantity = Math.max(0, currentQuantity - quantitySold);
                    itemRef.update("quantity", newQuantity);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, MoreExecutors.directExecutor());
        }
    }
	public void addCafeSale(CafeSale sale) {
		sale.setVenue(venueId);
		db.collection("cafe_sales").add(sale);
	}

	public ListenerRegistration attachCafeSaleListenerForDate(String dateStr, EventListener<QuerySnapshot> listener) {
		return db.collection("cafe_sales").whereEqualTo("dateString", dateStr).whereEqualTo("venue", venueId)
				.addSnapshotListener(listener);
	}

	public void batchUpdateCafeItems(Map<String, Object> newItems) {
		WriteBatch batch = db.batch();
		db.collection("cafe_items").get().addListener(() -> {
			try {
				QuerySnapshot existingDocs = db.collection("cafe_items").get().get();
				for (DocumentSnapshot doc : existingDocs.getDocuments()) {
					batch.delete(doc.getReference());
				}
				for (Map.Entry<String, Object> entry : newItems.entrySet()) {
					Map<String, Object> itemData = new HashMap<>();
					itemData.put("name", entry.getKey());
					itemData.put("price", entry.getValue());
					DocumentReference newDocRef = db.collection("cafe_items").document();
					batch.set(newDocRef, itemData);
				}
				batch.commit();
				System.out.println("Cafe items batch update successful!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, MoreExecutors.directExecutor());
	}

	public void deleteAllSalesOfItem(String dateStr, String itemName) {
		new Thread(() -> {
			try {
				WriteBatch batch = db.batch();
				QuerySnapshot salesForDay = db.collection("cafe_sales").whereEqualTo("dateString", dateStr).get().get();
				for (QueryDocumentSnapshot doc : salesForDay.getDocuments()) {
					CafeSale sale = doc.toObject(CafeSale.class);
					Map<String, Integer> itemsSold = sale.getItemsSold();
					if (itemsSold != null && itemsSold.containsKey(itemName)) {
						if (itemsSold.size() == 1) {
							batch.delete(doc.getReference());
						} else {
							double itemPrice = this.cafeItemPrices.getOrDefault(itemName, 0.0);
							int quantity = itemsSold.get(itemName);
							double newTotalAmount = sale.getTotalAmount() - (itemPrice * quantity);
							itemsSold.remove(itemName);
							batch.update(doc.getReference(), "itemsSold", itemsSold, "totalAmount", newTotalAmount);
						}
					}
				}
				batch.commit().get();
				System.out.println("Successfully deleted all sales of '" + itemName + "' for " + dateStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void deleteMostRecentSaleOfItem(String dateStr, String itemName) {
		new Thread(() -> {
			try {
				Query query = db.collection("cafe_sales").whereEqualTo("dateString", dateStr).orderBy("saleTimestamp",
						Query.Direction.DESCENDING);
				ApiFuture<QuerySnapshot> querySnapshot = query.get();
				for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
					CafeSale sale = doc.toObject(CafeSale.class);
					Map<String, Integer> itemsSold = sale.getItemsSold();
					if (itemsSold != null && itemsSold.containsKey(itemName)) {
						int currentQuantity = itemsSold.get(itemName);
						double itemPrice = this.cafeItemPrices.getOrDefault(itemName, 0.0);
						if (currentQuantity > 1) {
							itemsSold.put(itemName, currentQuantity - 1);
							double newTotalAmount = sale.getTotalAmount() - itemPrice;
							doc.getReference().update("itemsSold", itemsSold, "totalAmount", newTotalAmount).get();
						} else if (itemsSold.size() > 1) {
							itemsSold.remove(itemName);
							double newTotalAmount = sale.getTotalAmount() - itemPrice;
							doc.getReference().update("itemsSold", itemsSold, "totalAmount", newTotalAmount).get();
						} else {
							doc.getReference().delete().get();
						}
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	// --- Gear Item & Sale Methods ---

	public void loadGearItems(GenericItemsCallback callback) {
		loadItems("gear_items", callback);
	}

	public void loadGearItemsWithIds(GenericItemsWithIdsCallback callback) {
		loadItemsWithIds("gear_items", callback);
	}

	public void addGearItem(String name, double price) {
		db.collection("gear_items").add(Map.of("name", name, "price", price, "venue", venueId // ADD THIS
																											// LINE
		));
	}

	public void updateGearItem(String docId, String newName, double newPrice) {
		db.collection("gear_items").document(docId).update("name", newName, "price", newPrice);
	}



	public void addGearSale(GearSale sale) {
		sale.setVenue(venueId);
		db.collection("gear_sales").add(sale);
	}

	public ListenerRegistration attachGearSaleListenerForDate(String dateStr, EventListener<QuerySnapshot> listener) {
		return db.collection("gear_sales").whereEqualTo("dateString", dateStr).whereEqualTo("venue", venueId)
				.addSnapshotListener(listener);
	}

	public void batchUpdateGearItems(Map<String, Object> newItems) {
		WriteBatch batch = db.batch();
		db.collection("gear_items").get().addListener(() -> {
			try {
				QuerySnapshot existingDocs = db.collection("gear_items").get().get();
				for (DocumentSnapshot doc : existingDocs.getDocuments()) {
					batch.delete(doc.getReference());
				}
				for (Map.Entry<String, Object> entry : newItems.entrySet()) {
					Map<String, Object> itemData = new HashMap<>();
					itemData.put("name", entry.getKey());
					itemData.put("price", entry.getValue());
					DocumentReference newDocRef = db.collection("gear_items").document();
					batch.set(newDocRef, itemData);
				}
				batch.commit();
				System.out.println("Gear items batch update successful!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}, MoreExecutors.directExecutor());
	}

	public void deleteAllSalesOfGearItem(String dateStr, String itemName) {
		new Thread(() -> {
			try {
				WriteBatch batch = db.batch();
				QuerySnapshot salesForDay = db.collection("gear_sales").whereEqualTo("dateString", dateStr).get().get();
				for (QueryDocumentSnapshot doc : salesForDay.getDocuments()) {
					GearSale sale = doc.toObject(GearSale.class);
					Map<String, Integer> itemsSold = sale.getItemsSold();
					if (itemsSold != null && itemsSold.containsKey(itemName)) {
						if (itemsSold.size() == 1) {
							batch.delete(doc.getReference());
						} else {
							double itemPrice = this.gearItemPrices.getOrDefault(itemName, 0.0);
							int quantity = itemsSold.get(itemName);
							double newTotalAmount = sale.getTotalAmount() - (itemPrice * quantity);
							itemsSold.remove(itemName);
							batch.update(doc.getReference(), "itemsSold", itemsSold, "totalAmount", newTotalAmount);
						}
					}
				}
				batch.commit().get();
				System.out.println("Successfully deleted all sales of '" + itemName + "' for " + dateStr);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	public void deleteMostRecentSaleOfGearItem(String dateStr, String itemName) {
		new Thread(() -> {
			try {
				Query query = db.collection("gear_sales").whereEqualTo("dateString", dateStr).orderBy("saleTimestamp",
						Query.Direction.DESCENDING);
				ApiFuture<QuerySnapshot> querySnapshot = query.get();
				for (DocumentSnapshot doc : querySnapshot.get().getDocuments()) {
					GearSale sale = doc.toObject(GearSale.class);
					Map<String, Integer> itemsSold = sale.getItemsSold();
					if (itemsSold != null && itemsSold.containsKey(itemName)) {
						int currentQuantity = itemsSold.get(itemName);
						double itemPrice = this.gearItemPrices.getOrDefault(itemName, 0.0);
						if (currentQuantity > 1) {
							itemsSold.put(itemName, currentQuantity - 1);
							double newTotalAmount = sale.getTotalAmount() - itemPrice;
							doc.getReference().update("itemsSold", itemsSold, "totalAmount", newTotalAmount).get();
						} else if (itemsSold.size() > 1) {
							itemsSold.remove(itemName);
							double newTotalAmount = sale.getTotalAmount() - itemPrice;
							doc.getReference().update("itemsSold", itemsSold, "totalAmount", newTotalAmount).get();
						} else {
							doc.getReference().delete().get();
						}
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}).start();
	}

	// --- Other Financial Methods ---

	public void addMembershipPurchase(MembershipPurchase purchase) {
		purchase.setVenue(venueId);
		db.collection("membership_purchases").add(purchase);
	}

	public void addOutgoingExpense(OutgoingExpense expense) {
		expense.setVenue(venueId);
		db.collection("outgoing_expenses").add(expense);
	}

	public ListenerRegistration attachOutgoingExpenseListenerForDate(String dateStr,
			EventListener<QuerySnapshot> listener) {
		return db.collection("outgoing_expenses").whereEqualTo("dateString", dateStr)
				.whereEqualTo("venue", venueId).addSnapshotListener(listener);
	}

	public void removeOutgoingExpense(String documentId) {
		if (documentId != null)
			db.collection("outgoing_expenses").document(documentId).delete();
	}

	public void updateOutgoingExpense(OutgoingExpense expense) {
		if (expense.getDocumentId() != null)
			db.collection("outgoing_expenses").document(expense.getDocumentId()).set(expense);
	}
}