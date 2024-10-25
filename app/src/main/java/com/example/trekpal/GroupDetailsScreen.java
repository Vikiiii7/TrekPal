package com.example.trekpal;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class GroupDetailsScreen extends AppCompatActivity {

    private FirebaseFirestore db;
    private TextView tvActivityName, tvActivityType, tvActivityDescription, tvActivityDate, tvActivityTime, tvExpectedWeather;
    private RecyclerView rvGroupMembers;
    private GroupMemberAdapter memberAdapter;
    private List<GroupMember> groupMembersList = new ArrayList<>();
    private String activityName, activityType, uniqueCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_details_screen);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve activity name from the Intent
        activityName = getIntent().getStringExtra("activityName");
        activityType = getIntent().getStringExtra("activityType");
        uniqueCode = getIntent().getStringExtra("uniqueCode");

        Log.d("GroupChatMain", "Activity Name: " + activityName);

        // Initialize UI components
        tvActivityName = findViewById(R.id.tvActivityName);
        tvActivityType = findViewById(R.id.tvActivityType);
        tvActivityDescription = findViewById(R.id.tvActivityDescription);
        tvActivityDate = findViewById(R.id.tvActivityDate);
        tvActivityTime = findViewById(R.id.tvActivityTime);
        tvExpectedWeather = findViewById(R.id.tvExpectedWeather);
        rvGroupMembers = findViewById(R.id.rvGroupMembers);

        // Set up RecyclerView for group members
        rvGroupMembers.setLayoutManager(new LinearLayoutManager(this));
        memberAdapter = new GroupMemberAdapter(groupMembersList);
        rvGroupMembers.setAdapter(memberAdapter);

        // Load activity details and group members
        loadActivityDetails();
        loadGroupMembers();
    }

    private void loadActivityDetails() {
        db.collection("activityType")
                .document(activityType)  // Use the activityType to fetch the document
                .collection("activities")
                .document(activityName)
                .get() 
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        tvActivityName.setText("Activity Name: " + documentSnapshot.getString("activityName"));
                        tvActivityType.setText("Activity Type: " + documentSnapshot.getString("actType"));
                        tvActivityDescription.setText("Activity Description: " + documentSnapshot.getString("actDescription"));
                        tvActivityDate.setText("Date: " + documentSnapshot.getString("activityDate"));
                        tvActivityTime.setText("Time: " + documentSnapshot.getString("activityTime"));
                        tvExpectedWeather.setText("Expected Weather: " + documentSnapshot.getString("expectedWeather"));
                    } else {
                        Toast.makeText(GroupDetailsScreen.this, "Activity does not exist", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(GroupDetailsScreen.this, "Failed to load activity details", Toast.LENGTH_SHORT).show());
    }

    private void loadGroupMembers() {
        db.collection("activityType")
                .document(activityType)
                .collection("activities")
                .document(activityName)
                .collection("groupmembers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(GroupDetailsScreen.this, "No group members found", Toast.LENGTH_SHORT).show();
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String uniqueCode = document.getId();  // Use the document ID directly

                            if (uniqueCode != null) {  // Ensure uniqueCode is not null
                                fetchUsername(uniqueCode);
                            } else {
                                Log.e("GroupDetailsScreen", "uniqueCode is null for document: " + document.getId());
                                Toast.makeText(GroupDetailsScreen.this, "Error: Invalid group member data", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("GroupDetailsScreen", "Failed to load group members", e);
                    Toast.makeText(GroupDetailsScreen.this, "Failed to load group members", Toast.LENGTH_SHORT).show();
                });
    }




    private void fetchUsername(String uniqueCode) {
        db.collection("newUsers")
                .document(uniqueCode)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        groupMembersList.add(new GroupMember(username, uniqueCode));
                        memberAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(GroupDetailsScreen.this, "Failed to load user data", Toast.LENGTH_SHORT).show());
    }
}
