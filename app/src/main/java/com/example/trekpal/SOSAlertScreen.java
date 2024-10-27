package com.example.trekpal;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class SOSAlertScreen extends AppCompatActivity {

    private RecyclerView recyclerViewSOSAlerts;
    private SOSAlertAdapter sosAlertAdapter;
    private ArrayList<SOSAlert> sosAlertList = new ArrayList<>();
    private String activityName, selectedActivityType, uniqueCode;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosalert_screen);

        // Initialize RecyclerView
        recyclerViewSOSAlerts = findViewById(R.id.recyclerViewSOSAlerts);
        recyclerViewSOSAlerts.setLayoutManager(new LinearLayoutManager(this));
        sosAlertAdapter = new SOSAlertAdapter(this, sosAlertList);
        recyclerViewSOSAlerts.setAdapter(sosAlertAdapter);

        // Retrieve activity details from intent
        activityName = getIntent().getStringExtra("activityName");
        selectedActivityType = getIntent().getStringExtra("activityType");
        uniqueCode = getIntent().getStringExtra("uniqueCode");

        // Log the received values to check if they are null or contain expected data
        Log.d("GroupChatMain", "Activity Name: " + activityName);
        Log.d("GroupChatMain", "Selected Activity Type: " + selectedActivityType);
        Log.d("GroupChatMain", "Unique Code: " + uniqueCode);

        // In SOSAlertScreen.java
        ImageButton imgBackBtn = findViewById(R.id.imgBackBtn);
        imgBackBtn.setOnClickListener(view -> {

            finish();
        });


        // Initialize Firestore
        db = FirebaseFirestore.getInstance();
        fetchSOSAlerts();
    }

    private void fetchSOSAlerts() {
        // Update the reference to the correct collection for SOS alerts
        CollectionReference sosAlertsRef = db.collection("activityType")
                .document(selectedActivityType)
                .collection("activities")
                .document(activityName)
                .collection("sosalerts"); // Fetching from the sosalerts subcollection

        Query query = sosAlertsRef.orderBy("timeSent", Query.Direction.DESCENDING);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                sosAlertList.clear();  // Clear the list to avoid duplicates on reload
                for (DocumentSnapshot document : task.getResult()) {
                    SOSAlert sosAlert = document.toObject(SOSAlert.class);
                    if (sosAlert != null) {
                        sosAlertList.add(sosAlert);
                    }
                }
                sosAlertAdapter.notifyDataSetChanged();  // Refresh RecyclerView
                Log.d("SOSAlertScreen", "SOS alerts fetched successfully: " + sosAlertList.size());
            } else {
                Log.e("SOSAlertScreen", "Error fetching SOS alerts: " +
                        (task.getException() != null ? task.getException().getMessage() : "Unknown error"));
            }
        });
    }

}
