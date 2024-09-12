package com.example.trekpal;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.google.android.gms.common.api.ResolvableApiException;



public class WeatherFragment extends Fragment {

    private TextView tvTemperature, tvHumidity, tvWindSpeed, tvPrecipitation, tvDay;
    private EditText etLocation, etDate;
    private FusedLocationProviderClient fusedLocationClient;
    private final int REQUEST_LOCATION = 100;
    private final String API_KEY = "qmiW42oVNaGlAVw7Wt0qRkfCu6eIMAGu";
    private final String BASE_URL = "https://api.tomorrow.io/v4/timelines?location=YOUR_LAT,YOUR_LON&fields=temperature,humidity,windSpeed,precipitationIntensity&units=metric&timesteps=1h&apikey=" + API_KEY;

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

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Set current day and date
        setCurrentDayAndDate();

        // Add DatePicker on etDate click
        etDate.setOnClickListener(v -> showDatePickerDialog());

        // Check for location permission and fetch weather
        checkLocationPermissionAndFetchWeather();

        return view;
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, selectedYear, selectedMonth, selectedDay) -> {
            // Format the date and update the EditText
            calendar.set(selectedYear, selectedMonth, selectedDay);
            SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
            String selectedDate = dateFormat.format(calendar.getTime());

            etDate.setText(selectedDate);

            // Fetch weather for the selected date and location
            fetchWeatherForDateAndLocation(calendar.getTime());

        }, year, month, day);

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
            // Get a list of addresses corresponding to the location name
            addresses = geocoder.getFromLocationName(locationName, 1);
            if (addresses == null || addresses.isEmpty()) {
                Toast.makeText(getContext(), "Unable to fetch location coordinates", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get the first result
            Address address = addresses.get(0);
            double latitude = address.getLatitude();
            double longitude = address.getLongitude();

            // Format the date to the required format (YYYY-MM-DD)
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            String formattedDate = dateFormat.format(date);

            // Prepare the API URL with latitude, longitude, and date
            String apiUrl = BASE_URL.replace("YOUR_LAT", String.valueOf(latitude))
                    .replace("YOUR_LON", String.valueOf(longitude))
                    .replace("1h", "1d");  // For daily forecast

            // Add date to the URL if supported by the API
            apiUrl += "&startTime=" + formattedDate;

            // Fetch weather data using the constructed API URL
            new FetchWeatherTask().execute(apiUrl);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Geocoder service not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchLocationName(Location location) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String locality = address.getLocality(); // City or region
                String country = address.getCountryName(); // Country
                String displayLocation = locality + ", " + country; // Combine city and country

                etLocation.setText(displayLocation);
            } else {
                etLocation.setText("No address found");
            }
        } catch (IOException e) {
            e.printStackTrace();
            etLocation.setText("Geocoder service not available");
        }
    }


    private void setCurrentDayAndDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE");
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy");

        String currentDay = dayFormat.format(calendar.getTime());
        String currentDate = dateFormat.format(calendar.getTime());

        tvDay.setText(currentDay); // Update the UI to display the day
        etDate.setText(currentDate);

    }

    private void checkLocationPermissionAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        } else {
            checkGpsStatusAndFetchLocation();
        }
    }

    private void checkGpsStatusAndFetchLocation() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getActivity());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // GPS is enabled, fetch location
                getLocationAndFetchWeather();
            }
        });

        task.addOnFailureListener(getActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    // Show dialog to ask user to enable GPS
                    ((ResolvableApiException) e).startResolutionForResult(getActivity(), REQUEST_LOCATION);
                } catch (IntentSender.SendIntentException sendEx) {
                    sendEx.printStackTrace();
                }
            }
        });
    }

    private void getLocationAndFetchWeather() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
            return;
        }

        fusedLocationClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if (location != null) {
                    // Use the location coordinates to fetch weather
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
                    String apiUrl = BASE_URL.replace("YOUR_LAT", String.valueOf(latitude))
                            .replace("YOUR_LON", String.valueOf(longitude));
                    new FetchWeatherTask().execute(apiUrl);

                    // Fetch and display location name
                    fetchLocationName(location);
                } else {
                    Toast.makeText(getContext(), "Unable to detect location", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkGpsStatusAndFetchLocation();
            } else {
                Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            StringBuilder result = new StringBuilder();
            try {
                URL url = new URL(strings[0]);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result.toString();
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("API Response", result);

            if (result == null || result.isEmpty()) {
                Toast.makeText(getContext(), "Failed to fetch weather data. Empty response.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                JSONObject jsonObject = new JSONObject(result);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray timelines = data.getJSONArray("timelines");
                JSONObject firstTimeline = timelines.getJSONObject(0);
                JSONArray intervals = firstTimeline.getJSONArray("intervals");
                JSONObject firstInterval = intervals.getJSONObject(0);
                JSONObject values = firstInterval.getJSONObject("values");

                // Extract the required values
                int temperature = values.getInt("temperature");
                double humidity = values.getDouble("humidity");
                double windSpeed = values.getDouble("windSpeed");
                int precipitationIntensity = values.getInt("precipitationIntensity");

                // Update UI elements with fetched data
                tvTemperature.setText(temperature + " °C");
                tvHumidity.setText(humidity + " %");
                tvWindSpeed.setText(windSpeed + " km/h");
                tvPrecipitation.setText(precipitationIntensity + " %");

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Failed to parse weather data", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
