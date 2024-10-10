package com.example.trekpal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import android.location.Address;
import android.location.Geocoder;

public class WeatherFragment extends Fragment {

    private TextView tvTemperature, tvHumidity, tvWindSpeed, tvPrecipitation, tvDay, tvWeatherType;
    private EditText etLocation, etDate;
    private FusedLocationProviderClient fusedLocationClient;
    private ImageButton btnSearchLocation; // Add button for location search
    private final int REQUEST_LOCATION = 100;
    private final String API_KEY = "255e65ce00504ff3b5925741243009";
    private final String BASE_URL = "https://api.weatherapi.com/v1/";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_weather, container, false);

        // Initialize the UI elements
        tvTemperature = view.findViewById(R.id.tvTemperature);
        tvHumidity = view.findViewById(R.id.tvHumidity);
        tvWindSpeed = view.findViewById(R.id.tvWindSpeed);
        tvPrecipitation = view.findViewById(R.id.tvPrecipitation);
        tvDay = view.findViewById(R.id.tvDay);
        etDate = view.findViewById(R.id.etDate);
        etLocation = view.findViewById(R.id.etLocation);
        tvWeatherType = view.findViewById(R.id.tvWeatherType);
        btnSearchLocation = view.findViewById(R.id.searchLocationBtn); // Initialize search button


        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Set current day and date
        setCurrentDayAndDate();

        // Add DatePicker on etDate click
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Check for location permission and fetch weather
        checkLocationPermissionAndFetchWeather();

        // Set up search button click listener
        btnSearchLocation.setOnClickListener(v -> {
            String locationName = etLocation.getText().toString();
            if (!locationName.isEmpty()) {
                fetchWeatherForDateAndLocation(new Date()); // Use today's date for location search
            } else {
                Toast.makeText(getContext(), "Please enter a location to search", Toast.LENGTH_SHORT).show();
            }
        });


        return view;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance(); // Current date
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        // Create DatePickerDialog
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format the date and update the EditText
            calendar.set(selectedYear, selectedMonth, selectedDay);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String selectedDate = dateFormat.format(calendar.getTime());

            etDate.setText(selectedDate);

            // Fetch weather for the selected date and location
            fetchWeatherForDateAndLocation(calendar.getTime());

        }, year, month, day);

        // Set minimum date to today
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis());

        // Set maximum date to 7 days in the future
        Calendar maxDate = Calendar.getInstance();
        maxDate.add(Calendar.DAY_OF_MONTH, 7);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        // Show the DatePickerDialog
        datePickerDialog.show();
    }


    private void fetchWeatherForDateAndLocation(Date date) {
        String locationName = etLocation.getText().toString();

        if (locationName.isEmpty()) {
            Toast.makeText(getContext(), "Please enter a valid location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Convert location name (String) into latitude and longitude
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        List<Address> addresses;

        try {
            addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(getContext(), "Unable to fetch location coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            Address address = addresses.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();

            // Format the date to the required format (YYYY-MM-DD)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            // Prepare the API URL for forecast
            String apiUrl = BASE_URL + "forecast.json?key=" + API_KEY + "&q=" + latitude + "," + longitude + "&dt=" + formattedDate;
            Log.d("WeatherFragment", "API URL: " + apiUrl); // Log the API URL

            // Fetch weather data using the constructed API URL
            new FetchWeatherTask().execute(apiUrl);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void setCurrentDayAndDate() {
        Calendar calendar = Calendar.getInstance();

        // Get the device's current timezone
        TimeZone deviceTimeZone = Calendar.getInstance().getTimeZone();

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.getDefault());
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());

        // Set the timezone to the device's timezone
        dayFormat.setTimeZone(deviceTimeZone);
        dateFormat.setTimeZone(deviceTimeZone);

        String currentDay = dayFormat.format(calendar.getTime());
        String currentDate = dateFormat.format(calendar.getTime());

        // Set the day and date on the UI
        tvDay.setText(currentDay); // Update the UI to display the current day
        etDate.setText(currentDate); // Update the EditText to display the current date
    }


    private void checkLocationPermissionAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            getLocationAndFetchWeather();
        }
    }

    private void fetchLocationName(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String cityName = address.getLocality();
                String regionName = address.getAdminArea(); // State or region
                String countryName = address.getCountryName();

                // Format the location name, e.g., "Penang, Malaysia"
                String locationName = cityName + ", " + countryName;
                etLocation.setText(locationName);  // Set the location name in the EditText field
            } else {
                Toast.makeText(getContext(), "Unable to fetch location name", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            fusedLocationClient.getLastLocation().addOnSuccessListener(getActivity(), location -> {
                if (location != null) {
                    fetchLocationName(location);
                    fetchWeatherForDateAndLocation(new Date());
                    setCurrentDayAndDate(); // Call this to update the date and day
                } else {
                    Toast.makeText(getContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Failed to fetch location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("WeatherFragment", "Location fetch error: " + e.getMessage());
            });
        }
    }


    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                reader.close();
                return response.toString();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e("WeatherFragment", "Error fetching weather data: " + e.getMessage()); // Log error
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseWeatherResponse(result);
            } else {
                Toast.makeText(getContext(), "Failed to fetch weather data", Toast.LENGTH_SHORT).show();
                Log.e("WeatherFragment", "Weather data result is null"); // Log null result
            }
        }

        private void parseWeatherResponse(String response) {
            try {
                JSONObject jsonObject = new JSONObject(response);
                JSONObject currentWeather = jsonObject.getJSONObject("current");
                JSONObject condition = currentWeather.getJSONObject("condition");

                // Extract weather data
                double temperature = currentWeather.getDouble("temp_c");
                double windSpeed = currentWeather.getDouble("wind_kph");
                double humidity = currentWeather.getDouble("humidity");
                double precipitation = currentWeather.getDouble("precip_mm");
                String weatherType = condition.getString("text");

                // Update UI elements with the fetched data
                tvTemperature.setText(String.format(Locale.getDefault(), "%.1f°C", temperature));
                tvWindSpeed.setText(String.format(Locale.getDefault(), "%.1f kph", windSpeed));
                tvHumidity.setText(String.format(Locale.getDefault(), "%.1f%%", humidity));
                tvPrecipitation.setText(String.format(Locale.getDefault(), "%.1f mm", precipitation));
                tvWeatherType.setText(weatherType);

            } catch (JSONException e) {
                e.printStackTrace();
                Log.e("WeatherFragment", "Error parsing weather response: " + e.getMessage()); // Log parsing error
            }
        }
    }
}
