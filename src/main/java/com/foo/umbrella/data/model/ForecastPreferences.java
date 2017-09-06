package com.foo.umbrella.data.model;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.foo.umbrella.R;

/**
 * Created by Gina on 9/4/17.
 */

public class ForecastPreferences {

    /*
     * Default values
     */
    private static final String DEFAULT_WEATHER_LOCATION = "94043";

    /**
     * Returns the location currently set in Preferences.
     *
     * @param context Context used to get the SharedPreferences
     * @return Location The current user has set in SharedPreferences.
     */
    public static String getPreferredWeatherLocation(Context context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String keyForLocation = context.getString(R.string.pref_location_key);
        String location = context.getString(R.string.pref_location_default);

        return prefs.getString(keyForLocation, location);
    }

    /**
     * Returns true if the user has selected temperature units to display.
     *
     * @param context Context used to get the SharedPreferences
     *
     * @return true If metric display should be used
     */
    public static boolean isFahrenheit(Context context) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String keyForUnits = context.getString(R.string.pref_units_key);
        String defaultUnits = context.getString(R.string.pref_units_fahrenheit);
        String preferredUnits = prefs.getString(keyForUnits, defaultUnits);
        String metric = context.getString(R.string.pref_units_fahrenheit);
        boolean userPrefersFahrenheit;

        if (metric.equals(preferredUnits)) {
            userPrefersFahrenheit = true;
        } else {
            userPrefersFahrenheit = false;
        }
        return userPrefersFahrenheit;
    }


    private static String getDefaultWeatherLocation() {
        return DEFAULT_WEATHER_LOCATION;
    }


}
