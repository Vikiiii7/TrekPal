package com.example.trekpal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

public class SOSMapView extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private double latitude;
    private double longitude;
    private String senderUsername, profilePictureUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sosmap_view);

        // Retrieve latitude, longitude, and profile image URL from intent
        Intent intent = getIntent();
        latitude = intent.getDoubleExtra("latitude", 0.0);
        longitude = intent.getDoubleExtra("longitude", 0.0);
        profilePictureUrl = intent.getStringExtra("profilePictureUrl"); // Retrieve the profile image URL

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Back button functionality
        Button backButton = findViewById(R.id.buttonBack);
        backButton.setOnClickListener(v -> finish());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Load the profile image using Glide
        Glide.with(this)
                .asBitmap()
                .load(profilePictureUrl) // Use the fetched profile image URL
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                        // Create a custom marker bitmap
                        Bitmap customMarker = createCustomMarker(resource);

                        LatLng sosLocation = new LatLng(latitude, longitude);
                        mMap.addMarker(new MarkerOptions()
                                .position(sosLocation)
                                .icon(BitmapDescriptorFactory.fromBitmap(customMarker)) // Use custom marker bitmap
                                .title("SOS Location"));
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(sosLocation, 15));
                    }

                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        // Handle cleanup if necessary
                    }
                });
    }

    private Bitmap createCustomMarker(Bitmap profilePicture) {
        // Create a custom layout for the marker
        View markerView = LayoutInflater.from(this).inflate(R.layout.custom_marker_layout, null);

        // Set the profile image
        ImageView markerImageView = markerView.findViewById(R.id.markerImageView);
        markerImageView.setImageBitmap(profilePicture);

        // Measure and layout the view
        markerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        markerView.layout(0, 0, markerView.getMeasuredWidth(), markerView.getMeasuredHeight());
        markerView.setDrawingCacheEnabled(true);
        markerView.buildDrawingCache();

        // Create a bitmap from the layout
        Bitmap markerBitmap = Bitmap.createBitmap(markerView.getDrawingCache());
        markerView.setDrawingCacheEnabled(false); // Disable drawing cache

        return markerBitmap;
    }


}
