// File: AceAlleyOffice/AceAlleyOffice/Core/MembershipManager.java
package AceAlleyOffice.AceAlleyOffice.Core;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.google.cloud.firestore.Firestore;

import AceAlleyOffice.AceAlleyOffice.UI.Membership;
import AceAlleyOffice.AceAlleyOffice.UI.MembershipPackage;

public class MembershipManager {

	private final Firestore db;
	private final Map<String, Membership> allMemberships = new ConcurrentHashMap<>();
	private MembershipUpdateListener uiListener;
	private final String venueId;

	@FunctionalInterface
	public interface MembershipUpdateListener {
		void onMembershipsUpdated();
	}

	public void setListener(MembershipUpdateListener listener) {
		this.uiListener = listener;
	}

	public MembershipManager(String venueId) { // <-- MODIFY THE CONSTRUCTOR
		this.db = FirebaseManager.getFirestore();
		this.venueId = venueId; // <-- STORE THE VENUE ID
	}

	// In MembershipManager.java

	public void attachMembershipListener(CountDownLatch latch) {
		final AtomicBoolean firstLoad = new AtomicBoolean(true);
		db.collection("memberships").whereEqualTo("venue", venueId).addSnapshotListener((snapshot, error) -> {
			if (error != null) {
				System.err.println("Membership listener failed: " + error);
				return;
			}
			if (snapshot != null) {
				// --- THIS IS THE MODIFIED LINE ---
				System.out.println("Memberships loaded from Firestore. Total members: " + snapshot.size());

				allMemberships.clear();
				for (var doc : snapshot.getDocuments()) {
					Membership member = doc.toObject(Membership.class);
					member.setMemberId(doc.getId());
					allMemberships.put(member.getMemberId(), member);
				}
				if (uiListener != null) {
					uiListener.onMembershipsUpdated();
				}

				if (latch != null && firstLoad.getAndSet(false)) {
					latch.countDown();
				}
			}
		});
	}

	public void addMember(String name, String phone, String email, MembershipPackage pkg) {
		String memberId = phone + "_" + System.currentTimeMillis();
		String startDate = new SimpleDateFormat("dd-MM-yyyy").format(new Date());
		Membership newMember = new Membership(memberId, name, phone, email, startDate);

		// Pass both hours and duration to the add hours method
		newMember.addHoursForNewPurchase(pkg.getHours(), pkg.getDurationInMonths());

		newMember.setVenue(venueId);
		db.collection("memberships").document(memberId).set(newMember);
	}

	public void addPackageToMember(String memberId, MembershipPackage pkg) {
		if (memberId == null || pkg == null)
			return;

		db.runTransaction(transaction -> {
			var docRef = db.collection("memberships").document(memberId);
			var snapshot = transaction.get(docRef).get();
			Membership member = snapshot.toObject(Membership.class);

			if (member != null) {
				member.addHoursForNewPurchase(pkg.getHours(), pkg.getDurationInMonths());
				transaction.set(docRef, member);
			}
			return null;
		});
	}

	public void editMember(String memberId, String newName, String newPhone, String newEmail) {
		if (memberId == null)
			return;
		db.collection("memberships").document(memberId).update("name", newName, "phoneNumber", newPhone, "email",
				newEmail);
	}

	public List<Membership> findMembersByName(String query) {
		if (query == null || query.trim().isEmpty()) {
			return List.copyOf(allMemberships.values());
		}
		String lowerCaseQuery = query.toLowerCase();
		return allMemberships.values().stream().filter(member -> member.getName().toLowerCase().contains(lowerCaseQuery)
				|| member.getPhoneNumber().contains(lowerCaseQuery)).collect(Collectors.toList());
	}

	public Collection<Membership> getAllMembers() {
		return allMemberships.values();
	}

	public Membership getMember(String memberId) {
		return allMemberships.get(memberId);
	}

    public Map<String, Double> deductHoursForBooking(String memberId, double hoursToDeduct) {
        if (memberId == null || hoursToDeduct <= 0) {
            return java.util.Collections.emptyMap();
        }
        try {
            return db.runTransaction(transaction -> {
                var docRef = db.collection("memberships").document(memberId);
                var snapshot = transaction.get(docRef).get();
                Membership member = snapshot.toObject(Membership.class);

                if (member != null && member.getTotalHoursRemaining() >= hoursToDeduct) {
                    Map<String, Double> ledger = member.deductHours(hoursToDeduct);
                    transaction.set(docRef, member);
                    return ledger;
                }
                // Return null to indicate member not found or insufficient hours
                return null;
            }).get(); // .get() waits for the transaction to complete
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // Return null to indicate a database or concurrency error
            return null;
        }
    }

	public void refundMemberHours(String memberId, Map<String, Double> ledger) {
		if (memberId == null || ledger == null || ledger.isEmpty()) return;
		db.runTransaction(transaction -> {
			var docRef = db.collection("memberships").document(memberId);
			var snapshot = transaction.get(docRef).get();
			Membership member = snapshot.toObject(Membership.class);

			if (member != null) {
				member.refundHours(ledger);
				transaction.set(docRef, member);
			}
			return null;
		});
	}
}