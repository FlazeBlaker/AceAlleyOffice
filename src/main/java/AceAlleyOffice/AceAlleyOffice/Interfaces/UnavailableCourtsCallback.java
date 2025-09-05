package AceAlleyOffice.AceAlleyOffice.Interfaces;

import java.util.Set;

// A functional interface to handle the asynchronous result from Firebase
@FunctionalInterface
public interface UnavailableCourtsCallback {
    void onResult(Set<Integer> unavailableCourts);
}