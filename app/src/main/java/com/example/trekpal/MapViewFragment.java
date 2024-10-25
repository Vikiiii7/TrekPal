package com.example.trekpal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MapViewFragment extends Fragment implements OnMapReadyCallback {

    private MapView mapView;
    private GoogleMap googleMap;
    private FirebaseFirestore db;
    private Map<String, LatLng> userLocations = new HashMap<>();
    private Set<String> groupMemberUniqueCodes = new HashSet<>();  // Store group members' uniqueCodes
    private String activityName, selectedActivityType, uniqueCode;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment layout
        return inflater.inflate(R.layout.fragment_map_view, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Retrieve activity details from arguments
        if (getArguments() != null) {
            activityName = getArguments().getString("activityName");
            selectedActivityType = getArguments().getString("activityType");
            uniqueCode = getArguments().getString("uniqueCode");
        }

        // Initialize the MapView
        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this); // Load the map asynchronously

        // Initialize the Google Maps API
        MapsInitializer.initialize(requireActivity().getApplicationContext());
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        googleMap.getUiSettings().setZoomControlsEnabled(true);

        // Load group members from Firestore and then fetch their locations
        loadGroupMembersAndLocations();
    }

    private void loadGroupMembersAndLocations() {
        // Step 1: Retrieve the uniqueCodes of users in the selected activity's groupmembers subcollection
        db.collection("activityType")
                .document(selectedActivityType)
                .collection("activities")
                .document(activityName)
                .collection("groupmembers")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.w("MapViewFragment", "No group members found.");
                    } else {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String uniqueCode = document.getId();
                            Log.d("MapViewFragment", "Group member uniqueCode: " + uniqueCode);
                            groupMemberUniqueCodes.add(uniqueCode);  // Collect group member uniqueCodes
                        }
                        // Step 2: Now fetch the live locations of those group members
                        loadUserLocations();
                    }
                })
                .addOnFailureListener(e -> Log.w("MapViewFragment", "Failed to load group members.", e));

    }


    private void loadUserLocations() {
        // Step 3: Listen for changes in the newUsers collection and filter by uniqueCode
        db.collection("newUsers").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot snapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    Log.w("MapViewFragment", "Listen failed.", e);
                    return;
                }

                if (snapshots != null && !snapshots.isEmpty()) {
                    userLocations.clear(); // Clear previous locations
                    Log.d("MapViewFragment", "Snapshot received for live locations");

                    for (QueryDocumentSnapshot doc : snapshots) {
                        String uniqueCode = doc.getId();
                        if (groupMemberUniqueCodes.contains(uniqueCode)) {  // Only add if user is in groupmembers
                            Double latitude = doc.getDouble("latitude");
                            Double longitude = doc.getDouble("longitude");

                            if (latitude != null && longitude != null) {
                                LatLng latLng = new LatLng(latitude, longitude);
                                userLocations.put(uniqueCode, latLng);
                            }
                        }
                    }
                    updateMapMarkers();


                    zoomToUserLocation();

                } else {
                    Log.w("MapViewFragment", "No live locations found or snapshots empty.");
                }
            }
        });

    }

    private void zoomToUserLocation() {
        // Get the logged-in user's location (assuming you store it in userLocations with their uniqueCode)
        LatLng loggedInUserLocation = userLocations.get(uniqueCode); // Assuming uniqueCode is the logged-in user's unique code

        if (loggedInUserLocation != null && googleMap != null) {
            // Move the camera to the user's location and set a zoom level (e.g., 15f for a close-up)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loggedInUserLocation, 15f));
        } else {
            Log.w("MapViewFragment", "Logged-in user location not found or GoogleMap is null.");
        }
    }

    private void updateMapMarkers() {
        if (googleMap != null) {
            googleMap.clear(); // Clear existing markers
            Log.d("MapViewFragment", "Updating map markers...");

            for (Map.Entry<String, LatLng> entry : userLocations.entrySet()) {
                String uniqueCode = entry.getKey();
                LatLng location = entry.getValue();

                // Step 1: Fetch the profile picture
                db.collection("newUsers").document(uniqueCode)
                        .get().addOnSuccessListener(documentSnapshot -> {
                            if (documentSnapshot.exists() && documentSnapshot.contains("profilePictureUrl")) {
                                String username = documentSnapshot.getString("username");
                                String profilePictureUrl = documentSnapshot.getString("profilePictureUrl");

                                // Step 2: Load the profile picture using Glide and convert it to a Bitmap
                                Glide.with(requireContext())
                                        .asBitmap()
                                        .load(profilePictureUrl)
                                        .into(new CustomTarget<Bitmap>() {
                                            @Override
                                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                                // Step 3: Create custom marker with the profile picture
                                                Bitmap customMarker = createCustomMarker(resource);

                                                // Step 4: Add marker to the map with the profile picture
                                                googleMap.addMarker(new MarkerOptions()
                                                        .position(location)
                                                        .title(username + ": " + uniqueCode)
                                                        .icon(BitmapDescriptorFactory.fromBitmap(customMarker)));
                                            }

                                            @Override
                                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                                // Handle if needed
                                            }
                                        });
                            } else {
                                // If no profile picture found, add a default marker
                                googleMap.addMarker(new MarkerOptions()
                                        .position(location)
                                        .title("User: " + uniqueCode));
                            }
                        });
            }
        } else {
            Log.w("MapViewFragment", "GoogleMap is null, can't add markers.");
        }
    }

    private Bitmap createCustomMarker(Bitmap profilePicture) {
        // Create a custom layout for the marker
        View markerView = LayoutInflater.from(requireContext()).inflate(R.layout.custom_marker_layout, null);

        // Set the profile image
        ImageView markerImageView = markerView.findViewById(R.id.markerImageView);
        markerImageView.setImageBitmap(profilePicture);

        // Convert the layout into a bitmap
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.buildDrawingCache();
        Bitmap markerBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(markerBitmap);
        markerView.draw(canvas);

        return markerBitmap;
    }



    // Override lifecycle methods to manage the MapView lifecycle
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
