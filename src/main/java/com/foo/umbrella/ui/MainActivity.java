package com.foo.umbrella.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.foo.umbrella.R;
import com.foo.umbrella.UmbrellaApp;
import com.foo.umbrella.data.SharePreferences;
import com.foo.umbrella.data.model.CurrentObservation;
import com.foo.umbrella.data.model.ForecastCondition;
import com.foo.umbrella.data.model.WeatherData;

import org.threeten.bp.LocalDate;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.format.TextStyle;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements
        SharedPreferences.OnSharedPreferenceChangeListener {

  static {
    AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
  }

  private LinearLayout mCurrentConditionSection;
  private TextView mCurrentTemperatureText;
  private TextView mCurrentConditionText;
  private LinearLayout mWeatherConditionList;


  private static final String TAG = MainActivity.class.getSimpleName();
  private static boolean PREFERENCES_UPDATED = false;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mCurrentConditionSection = (LinearLayout) findViewById(R.id.current_weather_condition_section);
    mCurrentTemperatureText = (TextView) mCurrentConditionSection.findViewById(R.id.current_weather_temperature_text);
    mCurrentConditionText = (TextView) mCurrentConditionSection.findViewById(R.id.current_weather_condition_text);
    mWeatherConditionList = (LinearLayout) findViewById(R.id.weather_condition_list);
    mWeatherConditionList.findViewById(R.id.weather_condition_hour_text);

    Log.d(TAG, "onCreate: registering preference changed listener");
    PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this);

    refreshWeatherData();

  }

  private void refreshWeatherData() {
    String locationQuery = SharePreferences
            .getPreferredWeatherLocation(MainActivity.this);

    Log.d(TAG, "locationQuery is "+locationQuery);

    Call<WeatherData> call = ((UmbrellaApp)getApplication())
            .getApiServicesProvider().getWeatherService()
            .forecastForZipCallable(locationQuery);
    call.enqueue(new Callback<WeatherData>() {

      @Override
      public void onResponse(Call<WeatherData> call, Response<WeatherData> response) {
        Log.d(TAG, "onResponse");
        if (response.isSuccessful()) {
          setWeatherData(response.body());
        }
      }

      @Override
      public void onFailure(Call<WeatherData> call, Throwable t) {
        Log.d(TAG, "onFailure");
      }
    });
  }

  private void setWeatherData(WeatherData data) {

    ActionBar actionBar= getSupportActionBar();
    CurrentObservation currentObservation = data.getCurrentObservation();

    actionBar.setTitle(currentObservation.getDisplayLocation().getFullName());
    actionBar.setElevation(0.0f);



    int currentTempFahrenheit = new Double(currentObservation.getTempFahrenheit()).intValue();

    if (currentTempFahrenheit>=60) {
      mCurrentConditionSection.setBackgroundResource(R.color.weather_warm);
      actionBar.setDisplayShowCustomEnabled(true);
      actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
              .getColor(R.color.weather_warm)));
    } else {
      mCurrentConditionSection.setBackgroundResource(R.color.weather_cool);
      actionBar.setDisplayShowCustomEnabled(true);
      actionBar.setBackgroundDrawable(new ColorDrawable(getResources()
              .getColor(R.color.weather_cool)));

    }

    String temp = null;
    if (SharePreferences
            .isFahrenheit(MainActivity.this)) {
      temp = currentObservation.getTempFahrenheit();
    } else {
      temp = currentObservation.getTempCelsius();
    }

    mCurrentTemperatureText.setText(temp + (char) 0x00B0);
    mCurrentConditionText.setText(currentObservation.getWeatherDescription());

    List<ForecastCondition> forecast = data.getForecast();

    int dayIndex = -1;
    LocalDate lastDay = null;
    int hourIndex = -1;
    for (int i=0 ; i<forecast.size(); i++) {
      ForecastCondition condition = forecast.get(i);
      LocalDateTime d = condition.getDateTime();
      Log.d("UmbrellaApp", "dateTime="+d);
      if (lastDay == null || !lastDay.equals(d.toLocalDate())) {
        dayIndex ++;
        lastDay = d.toLocalDate();
        hourIndex = 0;
      }
      CardView child = (CardView)mWeatherConditionList.getChildAt(dayIndex);
      if (child == null) {
        getLayoutInflater().inflate(R.layout.weather_condition_day, mWeatherConditionList);
        child = (CardView)mWeatherConditionList.getChildAt(dayIndex);
      }
      TextView title = (TextView)child.findViewById(R.id.weather_condition_day_title);
      if (dayIndex == 0) {
        title.setText("Today");
      } else if (dayIndex == 1) {
        title.setText("Tomorrow");
      } else {
        title.setText(d.getDayOfWeek().getDisplayName(TextStyle.FULL_STANDALONE, Locale.getDefault()));
      }

      GridLayout list = (GridLayout)child.findViewById(R.id.current_weather_condition_hour_list);
      LinearLayout grandchild = (LinearLayout)list.getChildAt(hourIndex);
      if (grandchild == null) {
        getLayoutInflater().inflate(R.layout.weather_condition_hour, list);
        grandchild = (LinearLayout)list.getChildAt(hourIndex);
      }
      hourIndex++;
      Log.d("UmbrellaApp", "displayTime="+condition.getDisplayTime());
      TextView hour_text = (TextView)grandchild.findViewById(R.id.weather_condition_hour_text);
      hour_text.setText(condition.getDisplayTime());

      if (SharePreferences
              .isFahrenheit(MainActivity.this)) {
        temp = condition.getTempFahrenheit();
      } else {
        temp = condition.getTempCelsius();
      }

      Log.d("UmbrellaApp", "temp="+temp);
      TextView hour_temperature = (TextView)grandchild.findViewById(R.id.weather_condition_hour_temperature);
      hour_temperature.setText(temp + (char) 0x00B0);
      Log.d("UmbrellaApp", "icon="+condition.getIcon());
      AppCompatImageView hour_image = (AppCompatImageView)grandchild.findViewById(R.id.weather_condition_hour_image);
      String icon = condition.getIcon();
      ((UmbrellaApp)getApplication()).getApiServicesProvider().getPicasso().
              load("https://icons.wxug.com/i/c/j/"+icon+".gif").resize(24, 24).into(hour_image);
    }
  }

  @Override
  protected void onStart() {
    super.onStart();
    if (PREFERENCES_UPDATED) {
      Log.d(TAG, "onStart: preferences were updated");
      refreshWeatherData();
      PREFERENCES_UPDATED = false;
    }
  }


  @Override
  protected void onDestroy() {
    super.onDestroy();

    PreferenceManager.getDefaultSharedPreferences(this)
            .unregisterOnSharedPreferenceChangeListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.menu, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    try {
      if (id == R.id.action_settings) {
        Intent startSettingsActivity = new Intent(this, SettingActivity.class);
        startActivity(startSettingsActivity);
        return true;
      }
    }catch (Throwable e){
      e.printStackTrace();
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    PREFERENCES_UPDATED = true;
    Log.d(TAG, "PREFERENCES_UPDATED is "+PREFERENCES_UPDATED);
  }
}
