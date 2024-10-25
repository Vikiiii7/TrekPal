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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
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

        // Initialize location tracking
        LocationTracker locationTracker = new LocationTracker(getContext(), uniqueCode);

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
        if (activity.isInvitation()) {
            // This is an invitation item, open ActInvDetails with the activity details
            Intent intent = new Intent(getActivity(), ActInviteDetails.class);
            intent.putExtra("activityName", activity.getActivityName());
            intent.putExtra("activityType", activity.getActivityType());
            intent.putExtra("activityDate", activity.getActivityDate());
            intent.putExtra("uniqueCode", uniqueCode); // Pass the uniqueCode here
            startActivity(intent);
        } else {
            // This is a regular activity, open GroupChatMain with the activity details
            Intent intent = new Intent(getActivity(), GroupChatMain.class);
            intent.putExtra("activityName", activity.getActivityName());
            intent.putExtra("activityType", activity.getActivityType());
            intent.putExtra("activityDate", activity.getActivityDate());
            intent.putExtra("uniqueCode", uniqueCode); // Pass the uniqueCode here
            startActivity(intent);
        }
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
                String creatorCode = task.getResult().getString("creatorUniqueCode");

                if (creatorCode != null && creatorCode.equals(uniqueCode)) {
                    // If user is the creator, delete the entire activity
                    activityRef.delete().addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Activity deleted successfully by creator.");
                        activityList.remove(position);
                        adapter.notifyItemRemoved(position);
                    }).addOnFailureListener(e -> Log.e("Firestore", "Error deleting activity", e));
                } else {
                    // If user is not the creator, check if they are in groupmembers or invitations
                    // Remove from groupmembers
                    DocumentReference groupMemberRef = activityRef.collection("groupmembers").document(uniqueCode);
                    groupMemberRef.get().addOnCompleteListener(groupMemberTask -> {
                        if (groupMemberTask.isSuccessful() && groupMemberTask.getResult().exists()) {
                            groupMemberRef.delete().addOnSuccessListener(aVoid -> {
                                Log.d("Firestore", "User removed from group members.");
                                activityList.remove(position);
                                adapter.notifyItemRemoved(position);
                            }).addOnFailureListener(e -> Log.e("Firestore", "Error removing user from group members", e));
                        } else {
                            // If not a group member, check invitations
                            DocumentReference invitationRef = activityRef.collection("invitations").document(uniqueCode);
                            invitationRef.get().addOnCompleteListener(invitationTask -> {
                                if (invitationTask.isSuccessful() && invitationTask.getResult().exists()) {
                                    invitationRef.delete().addOnSuccessListener(aVoid -> {
                                        Log.d("Firestore", "Invitation declined by user.");
                                        activityList.remove(position);
                                        adapter.notifyItemRemoved(position);
                                    }).addOnFailureListener(e -> Log.e("Firestore", "Error removing invitation", e));
                                }
                            });
                        }
                    });
                }
            }
        });
    }


    private void loadActivitiesFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Loop through each activity type
        for (String activityType : activityTypes) {
            // Fetch activities
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

                                if (activityName != null && uniqueCode != null) {
                                    // Check if the user is in groupmembers subcollection
                                    db.collection("activityType").document(activityType)
                                            .collection("activities").document(activityName)
                                            .collection("groupmembers").document(uniqueCode)
                                            .get()
                                            .addOnCompleteListener(groupMemberTask -> {
                                                if (groupMemberTask.isSuccessful() && groupMemberTask.getResult().exists()) {
                                                    // User is a group member, add the activity
                                                    Activity activity = new Activity(activityName, actType, activityDate, uniqueCode, false);
                                                    activityList.add(activity);
                                                    Log.d("Firestore", "Added Group Activity: " + activityName + " with Type: " + actType +
                                                            " on Date: " + activityDate);
                                                }

                                                // Now check invitations
                                                db.collection("activityType").document(activityType)
                                                        .collection("activities").document(activityName)
                                                        .collection("invitations").document(uniqueCode)
                                                        .get()
                                                        .addOnCompleteListener(invitationTask -> {
                                                            if (invitationTask.isSuccessful() && invitationTask.getResult().exists()) {
                                                                // User has an invitation, add to activity list
                                                                Activity invitation = new Activity(activityName, actType, activityDate, uniqueCode, true);
                                                                activityList.add(invitation);
                                                                Log.d("Firestore", "Added Invitation: " + activityName + " with Type: " + actType +
                                                                        " on Date: " + activityDate);
                                                            }

                                                            // Sort and notify adapter after loading activities and invitations
                                                            sortActivitiesByDate();
                                                            adapter.notifyDataSetChanged();
                                                            progressBar.setVisibility(View.GONE);
                                                        });
                                            });
                                }
                            }
                        } else {
                            Log.e("Firestore", "Error fetching activities: ", task.getException());
                            progressBar.setVisibility(View.GONE);
                        }
                    });
        }
    }




    // Method to sort activities by date in ascending order
    private void sortActivitiesByDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Collections.sort(activityList, new Comparator<Activity>() {
            @Override
            public int compare(Activity a1, Activity a2) {
                try {
                    Date date1 = dateFormat.parse(a1.getActivityDate());
                    Date date2 = dateFormat.parse(a2.getActivityDate());
                    return date1.compareTo(date2); // Ascending order
                } catch (ParseException e) {
                    e.printStackTrace();
                    return 0; // Keep the original order if there's an error
                }
            }
        });
    }


}
