// File: AceAlleyOffice/AceAlleyOffice/Core/strategy/DefaultPricing.java
package AceAlleyOffice.AceAlleyOffice.Core.strategy;

import java.util.Date;
import AceAlleyOffice.AceAlleyOffice.UI.PricingSettings;

public class DefaultPricing implements PricingStrategy {
    
    private final PricingSettings settings;

    public DefaultPricing(PricingSettings settings) {
        this.settings = settings;
    }

    @Override
    public double calculateCourtPrice(Date selectedDate, String time) {
        // This is a simple fallback strategy that always returns the regular weekday price.
        // You could make this a flat rate or any other simple logic.
        if (settings != null) {
            return settings.getCourt_weekday_regular();
        }
        // A safe default if settings aren't loaded
        return 650.0;
    }
}