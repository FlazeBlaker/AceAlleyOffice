// File: AceAlleyOffice/AceAlleyOffice/Core/strategy/DynamicPricing.java
package AceAlleyOffice.AceAlleyOffice.Core.strategy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import AceAlleyOffice.AceAlleyOffice.UI.PricingSettings;

// Renamed from VimanNagarPricing to reflect its purpose, not its location.
public class DynamicPricing implements PricingStrategy {

    private final PricingSettings settings;

    public DynamicPricing(PricingSettings settings) {
        this.settings = settings;
    }

    @Override
    public double calculateCourtPrice(Date selectedDate, String time) {
        if (settings == null) {
            return 600.0; // A safe default
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(selectedDate);
        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
        boolean isWeekend = (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY);

        if (isWeekend) {
            return settings.getCourt_weekend();
        }
        
        try {
            Date hourDate = new SimpleDateFormat("hh:mm a").parse(time);
            calendar.setTime(hourDate);
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            double happyHourStart = settings.getCourt_happy_hour_start();
            double happyHourEnd = settings.getCourt_happy_hour_end();

            if (hour >= happyHourStart && hour < happyHourEnd) {
                return settings.getCourt_weekday_happy();
            } else {
                return settings.getCourt_weekday_regular();
            }

        } catch (ParseException e) {
            e.printStackTrace();
            return settings.getCourt_weekday_regular();
        }
    }
}