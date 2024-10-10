package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class ActInviteDetails extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView invActName, invActDesc, invActType, invActDate, invActTime, invExpWeather;
    private String activityType, uniqueCode; // Declare activityType here



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_act_invite_details);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize TextViews
        invActName = findViewById(R.id.invActName);
        invActDesc = findViewById(R.id.invActDesc);
        invActType = findViewById(R.id.invActType);
        invActDate = findViewById(R.id.invActDate);
        invActTime = findViewById(R.id.invActTime);
        invExpWeather = findViewById(R.id.invExpWeather);

        // Disable the buttons initially
        findViewById(R.id.btnAcceptInv).setEnabled(false);
        findViewById(R.id.btnDeclineInv).setEnabled(false);

        // Retrieve uniqueCode and activityName from the Intent
        uniqueCode = getIntent().getStringExtra("uniqueCode");
        String activityName = getIntent().getStringExtra("activityName");

        //  and load views, and set click listeners for accept/decline buttons
        loadActivityDetails(activityName);
        setupButtons(uniqueCode, activityName);
    }


    private void setupButtons(String uniqueCode, String activityName) {
        findViewById(R.id.btnAcceptInv).setOnClickListener(v -> acceptInvitation(uniqueCode, activityName));
        findViewById(R.id.btnDeclineInv).setOnClickListener(v -> declineInvitation(uniqueCode, activityName));
    }

    private void loadActivityDetails(String activityName) {
        // Iterate through all activity types to find the correct document
        db.collection("activityType")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                        String actType = document.getId();

                        // Check if the document exists in the specific activity type subcollection
                        db.collection("activityType")
                                .document(actType)
                                .collection("activities")
                                .document(activityName)
                                .get()
                                .addOnSuccessListener(activitySnapshot -> {
                                    if (activitySnapshot.exists()) {
                                        // Populate TextViews with activity details
                                        invActName.setText(activitySnapshot.getString("activityName"));
                                        invActDesc.setText(activitySnapshot.getString("actDescription"));
                                        invActType.setText(activitySnapshot.getString("actType"));
                                        invActDate.setText(activitySnapshot.getString("activityDate"));
                                        invActTime.setText(activitySnapshot.getString("activityTime"));
                                        invExpWeather.setText(activitySnapshot.getString("expectedWeather"));

                                        // Set the activityType and enable buttons
                                        activityType = actType;
                                        findViewById(R.id.btnAcceptInv).setEnabled(true);
                                        findViewById(R.id.btnDeclineInv).setEnabled(true);
                                    }
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to load activity details.", Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to find activity type.", Toast.LENGTH_SHORT).show()
                );
    }



    private void acceptInvitation(String uniqueCode, String activityName) {
        // Move the uniqueCode to the groupmembers collection
        db.collection("activityType").document(activityType).collection("activities")
                .document(activityName).collection("groupmembers").document(uniqueCode)
                .set(new HashMap<>()) // Add an empty document for the member
                .addOnSuccessListener(aVoid -> {
                    // Remove only the uniqueCode document from invitations collection after successful addition to groupmembers
                    db.collection("activityType").document(activityType).collection("activities")
                            .document(activityName).collection("invitations").document(uniqueCode)
                            .delete()
                            .addOnSuccessListener(aVoid1 -> {
                                Toast.makeText(this, "Invitation accepted.", Toast.LENGTH_SHORT).show();
                                goToHomeScreen(); // Navigate to HomeScreen
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to remove uniqueCode from invitations.", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to accept invitation.", Toast.LENGTH_SHORT).show()
                );
    }


    private void declineInvitation(String uniqueCode, String activityName) {
        // Remove the uniqueCode document from invitations subcollection only
        db.collection("activityType").document(activityType).collection("activities")
                .document(activityName).collection("invitations").document(uniqueCode)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Invitation declined.", Toast.LENGTH_SHORT).show();
                    goToHomeScreen(); // Navigate to HomeScreen
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to remove uniqueCode from invitations.", Toast.LENGTH_SHORT).show()
                );
    }

    private void goToHomeScreen() {
        Intent intent = new Intent(ActInviteDetails.this, MainActivity.class);
        intent.putExtra("uniqueCode", uniqueCode); // Pass the uniqueCode here

        startActivity(intent);
        finish(); // Optional: Close ActInviteDetails activity
    }
}
