package com.example.trekpal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class SOSAlertAdapter extends RecyclerView.Adapter<SOSAlertAdapter.SOSAlertViewHolder> {

    private final ArrayList<SOSAlert> sosAlertList;
    private final Context context;

    public SOSAlertAdapter(Context context, ArrayList<SOSAlert> sosAlertList) {
        this.context = context;
        this.sosAlertList = sosAlertList;
    }

    @NonNull
    @Override
    public SOSAlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sos_alert, parent, false);
        return new SOSAlertViewHolder(view);
    }

    private void fetchProfileImageUrl(String username, OnImageUrlFetchedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("newUsers") // Use your actual collection name
                .whereEqualTo("username", username)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String profileImageUrl = document.getString("profilePictureUrl");
                            listener.onImageUrlFetched(profileImageUrl);
                        }
                    } else {
                        listener.onImageUrlFetched(null); // No image found
                    }
                });
    }

    public interface OnImageUrlFetchedListener {
        void onImageUrlFetched(String imageUrl);
    }


    @Override
    public void onBindViewHolder(@NonNull SOSAlertViewHolder holder, int position) {
        SOSAlert sosAlert = sosAlertList.get(position);
        holder.timeSent.setText(sosAlert.getTimeSent());
        holder.senderUsername.setText(sosAlert.getSenderUsername());
        holder.locationDescription.setText(sosAlert.getLocationDescription());
        holder.message.setText(sosAlert.getMessage());

        // Set up button click to open map with location
        holder.buttonTrackLocation.setOnClickListener(v -> {
            // Fetch the profile image URL based on sender's username
            fetchProfileImageUrl(sosAlert.getSenderUsername(), profilePictureUrl -> {
                Intent intent = new Intent(context, SOSMapView.class);
                intent.putExtra("latitude", sosAlert.getLatitude());
                intent.putExtra("longitude", sosAlert.getLongitude());
                intent.putExtra("profilePictureUrl", profilePictureUrl); // Pass the profile image URL
                context.startActivity(intent);
            });
        });
    }

    @Override
    public int getItemCount() {
        return sosAlertList.size();
    }

    static class SOSAlertViewHolder extends RecyclerView.ViewHolder {
        TextView timeSent, senderUsername, locationDescription, message;
        Button buttonTrackLocation; // New button to track location

        SOSAlertViewHolder(View itemView) {
            super(itemView);
            timeSent = itemView.findViewById(R.id.textViewTimeSent);
            senderUsername = itemView.findViewById(R.id.textViewSenderUsername);
            locationDescription = itemView.findViewById(R.id.textViewLocationDescription);
            message = itemView.findViewById(R.id.textViewMessage);
            buttonTrackLocation = itemView.findViewById(R.id.buttonTrackLocation); // Initialize button
        }
    }
}
