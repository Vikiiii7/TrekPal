package com.example.trekpal;


import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ActivityAdapter adapter;
    private List<Activity> activityList;
    private String uniqueCode;
    private static final String[] activityTypes = {"Hiking", "Bike Ride", "Camping", "Car Ride", "Kayaking", "Trekking"};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewActivities);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        activityList = new ArrayList<>();
        adapter = new ActivityAdapter(activityList);
        recyclerView.setAdapter(adapter);

        // Fetch uniqueCode from arguments
        if (getArguments() != null) {
            uniqueCode = getArguments().getString("uniqueCode");
            Log.d("HomeFragment", "Unique Code: " + uniqueCode);
        }

        loadActivitiesFromFirestore();
        return view;
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

                                // Extracting actParticipants map
                                Map<String, Object> actParticipants = (Map<String, Object>) document.get("actParticipants");

                                if (activityName != null && actParticipants != null && uniqueCode != null) {
                                    // Checking if the uniqueCode is part of the actParticipants map
                                    if (actParticipants.containsValue(uniqueCode)) {
                                        // Create an Activity object with both activityName and actType
                                        Activity activity = new Activity(activityName, actType);
                                        activityList.add(activity);
                                        Log.d("Firestore", "Added Activity: " + activityName + " with Type: " + actType);
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();
                        } else {
                            Log.e("Firestore", "Error fetching data: ", task.getException());
                        }
                    });
        }
    }


}
