package com.example.trekpal;


import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment implements ActivityAdapter.OnActivityClickListener{

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;
    private ProgressBar progressBar;
    private String uniqueCode;
    private static final String[] activityTypes = {"Hiking", "Bike Ride", "Camping", "Car Ride", "Kayaking", "Trekking"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(activityList, this);
        recyclerView.setAdapter(adapter);

        // Initialize ProgressBar
        progressBar = view.findViewById(R.id.progressBar);

        // Fetch uniqueCode from arguments
        if (getArguments() != null) {
            uniqueCode = getArguments().getString("uniqueCode");
            Log.d("HomeFragment", "Unique Code: " + uniqueCode);
        }

        // Show ProgressBar before loading data
        progressBar.setVisibility(View.VISIBLE);

        loadActivitiesFromFirestore();

        // Add the swipe-to-delete functionality
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want drag & drop, just swipe
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Activity activity = activityList.get(position);
                handleDeleteActivity(activity, position);
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                                    @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                    int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                // Optional: Customize swipe background, e.g., color for delete
                c.drawColor(Color.RED);
            }

        }).attachToRecyclerView(recyclerView);

        return view;

    }

    @Override
    public void onActivityClick(Activity activity) {
        // Start GroupChatMain activity with the selected activity details
        Intent intent = new Intent(getActivity(), GroupChatMain.class);
        intent.putExtra("activityName", activity.getActivityName());
        intent.putExtra("activityType", activity.getActivityType());
        intent.putExtra("activityDate", activity.getActivityDate());
        startActivity(intent);
    }

    private void handleDeleteActivity(Activity activity, int position) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String actType = activity.getActivityType(); // Get activity type
        String activityName = activity.getActivityName();
        DocumentReference activityRef = db.collection("activityType")
                .document(actType)
                .collection("activities")
                .document(activityName);

        activityRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                Map<String, Object> actParticipants = (Map<String, Object>) task.getResult().get("actParticipants");
                String creatorCode = task.getResult().getString("creatorUniqueCode");

                if (creatorCode != null && creatorCode.equals(uniqueCode)) {
                    // If the user is the creator, delete the entire activity
                    activityRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Activity deleted successfully by creator.");
                        activityList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error deleting activity", e));
                } else {
                    // If the user is a participant, remove them from the participants
                    if (actParticipants != null && actParticipants.containsValue(uniqueCode)) {
                        activityRef.update("actParticipants." + uniqueCode, FieldValue.delete())
                                .addOnSuccessListener(aVoid -> {
                                    Log.d("Firestore", "User removed from activity.");
                                    activityList.remove(position);
                                    adapter.notifyItemRemoved(position);
                                }).addOnFailureListener(e -> Log.e("Firestore", "Error removing user from activity", e));
                    }
                }
            }
        });
    }


    private void loadActivitiesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        for (String activityType : activityTypes) {
            db.collection("activityType").document(activityType).collection("activities")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            QuerySnapshot result = task.getResult();
                            if (result.isEmpty()) {
                                Log.d("Firestore", "No matching activities found for type: " + activityType);
                            }
                            for (QueryDocumentSnapshot document : result) {
                                String activityName = document.getString("activityName");
                                String actType = document.getString("actType");
                                String activityDate = document.getString("activityDate");

                                // Extracting actParticipants map
                                Map<String, Object> actParticipants = (Map<String, Object>) document.get("actParticipants");

                                if (activityName != null && actParticipants != null && uniqueCode != null) {
                                    // Checking if the uniqueCode is part of the actParticipants map
                                    if (actParticipants.containsValue(uniqueCode)) {
                                        // Create an Activity object with both activityName and actType
                                        Activity activity = new Activity(activityName, actType, activityDate, uniqueCode);
                                        activityList.add(activity);
                                        Log.d("Firestore", "Added Activity: " + activityName + " with Type: " + actType +
                                                "in Date " + activityDate);
                                    }
                                }
                            }

                            // Hide ProgressBar after data is loaded
                            progressBar.setVisibility(View.GONE);
                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("Firestore", "Error fetching data: ", task.getException());
                            // Hide ProgressBar if there's an error
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }


}
