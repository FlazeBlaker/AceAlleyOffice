// File: AceAlleyOffice/AceAlleyOffice/Core/strategy/PricingStrategy.java
package AceAlleyOffice.AceAlleyOffice.Core.strategy;

import java.util.Date;

/**
 * An interface that defines the contract for any pricing calculation strategy.
 * This allows different venues to have completely different pricing logic.
 */
public interface PricingStrategy {
    double calculateCourtPrice(Date selectedDate, String time);
}