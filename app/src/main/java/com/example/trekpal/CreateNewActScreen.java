package com.example.trekpal;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class CreateNewActScreen extends AppCompatActivity {

    private EditText etActivityName, etActivityDesc, etDatePicker, etTimePicker;
    private String selectedActivityType, selectedDate, selectedTime, selectedWeatherType;
    private FirebaseFirestore db;
    private String uniqueCode; // To store the logged-in user's unique code

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_new_act_screen);

        etActivityName = findViewById(R.id.etActName);
        etActivityDesc = findViewById(R.id.etActDesc);
        etDatePicker = findViewById(R.id.etDatePicker);
        etTimePicker = findViewById(R.id.etTimePicker);
        Spinner actTypeSpinner = findViewById(R.id.actTypeSpinner);
        Spinner weatherSpinner = findViewById(R.id.weatherSpinner);
        ImageButton createActivityButton = findViewById(R.id.btnCreateAct);
        Button cancelButton = findViewById(R.id.button);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get the uniqueCode passed from MainActivity
        uniqueCode = getIntent().getStringExtra("uniqueCode");

        // Setting up the Spinner options
        String[] activityTypes = {"Hiking", "Bike Ride", "Car Ride", "Trekking", "Kayaking", "Camping"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, activityTypes);
        actTypeSpinner.setAdapter(adapter);

        // Spinner item selection logic
        actTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedActivityType = activityTypes[position];
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedActivityType = null;
            }
        });

        // Define the summarized weather options
        String[] weatherOptions = {"Clear/Cloudy", "Foggy", "Rainy", "Snowy", "Stormy"};
        ArrayAdapter<String> weatherAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, weatherOptions);
        weatherSpinner.setAdapter(weatherAdapter);

        // Handle selection of weather type
        weatherSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedWeatherType = weatherOptions[position]; // Store the selected weather type
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedWeatherType = null; // Handle case when no selection is made
            }
        });

        // Date picker logic
        etDatePicker.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(CreateNewActScreen.this, (view, year1, month1, dayOfMonth) -> {
                selectedDate = dayOfMonth + "/" + (month1 + 1) + "/" + year1;
                etDatePicker.setText(selectedDate);
            }, year, month, day);
            datePickerDialog.show();
        });

        // Time picker logic
        etTimePicker.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(CreateNewActScreen.this, (view, hourOfDay, minute1) -> {
                selectedTime = hourOfDay + ":" + (minute1 < 10 ? "0" + minute1 : minute1); // Format with leading zero for minutes
                etTimePicker.setText(selectedTime);
            }, hour, minute, true);
            timePickerDialog.show();
        });

        // Handle the create activity button click
        createActivityButton.setOnClickListener(view -> {
            String activityName = etActivityName.getText().toString().trim();
            String activityDescription = etActivityDesc.getText().toString().trim();

            if (activityName.isEmpty() || activityDescription.isEmpty() || selectedActivityType == null || selectedDate == null || selectedTime == null|| selectedWeatherType == null) {
                Toast.makeText(CreateNewActScreen.this, "Please fill in all the fields", Toast.LENGTH_SHORT).show();
            } else if (activityName.length() > 20) {
                Toast.makeText(CreateNewActScreen.this, "Activity Name cannot exceed 20 characters", Toast.LENGTH_SHORT).show();
            } else if (wordCount(activityDescription) > 50) {
                Toast.makeText(CreateNewActScreen.this, "Activity Description cannot exceed 50 words", Toast.LENGTH_SHORT).show();
            } else {
                // Prepare the data to insert
                Map<String, Object> activityData = new HashMap<>();
                activityData.put("activityName", activityName);
                activityData.put("actDescription", activityDescription);
                activityData.put("actType", selectedActivityType);
                activityData.put("activityDate", selectedDate); // Store selected date
                activityData.put("activityTime", selectedTime); // Store selected time
                activityData.put("expectedWeather", selectedWeatherType); // Store selected weather
                activityData.put("creatorUniqueCode", uniqueCode);

                // Add actParticipants with the uniqueCode
                Map<String, Object> participants = new HashMap<>();
                participants.put("uniqueCode", uniqueCode);
                activityData.put("actParticipants", participants);

                // Use activity name as the document ID inside the selected activity type collection
                db.collection("activityType")
                        .document(selectedActivityType)
                        .collection("activities")
                        .document(activityName)
                        .set(activityData)
                        .addOnSuccessListener(aVoid -> {
                            Toast.makeText(CreateNewActScreen.this, "Activity Created Successfully", Toast.LENGTH_SHORT).show();
                            // Navigate to the main activity or another screen
                            Intent intent = new Intent(CreateNewActScreen.this, MainActivity.class);
                            intent.putExtra("uniqueCode", uniqueCode);
                            startActivity(intent);
                            finish();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(CreateNewActScreen.this, "Error creating activity", Toast.LENGTH_SHORT).show();
                        });
            }
        });

        // Handle the cancel button click
        cancelButton.setOnClickListener(view -> {
            finish();
        });
    }

    // Helper method to count words in the activity description
    private int wordCount(String description) {
        if (TextUtils.isEmpty(description)) {
            return 0;
        }
        String[] words = description.trim().split("\\s+");
        return words.length;
    }
}
