package com.foo.umbrella.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.foo.umbrella.R;

/**
 * Created by Gina on 9/5/17.
 */

public class SharePreferences {

    public static String getPreferredWeatherLocation(Context context) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        String keyForLocation = context.getString(R.string.pref_location_key);
        String defaultLocation = context.getString(R.string.pref_location_default);
        return prefs.getString(keyForLocation, defaultLocation);
    }

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
}
