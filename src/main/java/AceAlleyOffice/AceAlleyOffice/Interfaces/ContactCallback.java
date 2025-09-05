// File: AceAlleyOffice/AceAlleyOffice/Core/ContactCallback.java
package AceAlleyOffice.AceAlleyOffice.Interfaces;

import java.util.Map;

@FunctionalInterface
public interface ContactCallback {
    void onResult(Map<String, Object> contactData);
}