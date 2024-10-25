package com.example.trekpal;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class LocationTracker {

    private FusedLocationProviderClient fusedLocationClient;
    private FirebaseFirestore db;
    private String uniqueCode;

    public LocationTracker(Context context, String uniqueCode) {
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.db = FirebaseFirestore.getInstance();
        this.uniqueCode = uniqueCode;

        requestLocationUpdates(context);
    }

    private void requestLocationUpdates(Context context) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, handle appropriately in the calling activity/fragment
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setInterval(30000);  // 30 seconds
        locationRequest.setFastestInterval(15000);  // 15 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        updateLocationInFirestore(latitude, longitude);
                    }
                }
            }
        }, Looper.getMainLooper());
    }

    private void updateLocationInFirestore(double latitude, double longitude) {
        if (uniqueCode != null) {
            DocumentReference userRef = db.collection("newUsers").document(uniqueCode);
            userRef.update("latitude", latitude, "longitude", longitude)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("Firestore", "Location updated successfully.");
                    })
                    .addOnFailureListener(e -> {
                        Log.e("Firestore", "Error updating location", e);
                    });
        } else {
            Log.e("Firestore", "UniqueCode is null, cannot update location.");
        }
    }
}
