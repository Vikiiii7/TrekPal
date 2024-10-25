package com.example.trekpal;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EmergencyScreenFragment extends Fragment {

    private Button btnSOS, btnCancel, btnConfirm;
    private TextView sosStatus;
    private CountDownTimer countDownTimer;
    private Drawable group_33;
    private String activityName, selectedActivityType, uniqueCode, username;
    private FirebaseFirestore firestore;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationClient;

    private static final String CHANNEL_ID = "sos_alert_channel";
    private static final String CHANNEL_NAME = "SOS Alert Notifications";
    private static final String CHANNEL_DESC = "Notifications for SOS alerts";

    public EmergencyScreenFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        // Retrieve details passed from arguments
        if (getArguments() != null) {
            activityName = getArguments().getString("activityName");
            selectedActivityType = getArguments().getString("activityType");
            uniqueCode = getArguments().getString("uniqueCode");
            username = getArguments().getString("username");
        }

        if (username == null || username.isEmpty()) {
            fetchUsernameFromFirestore();
        }

        // Create notification channel
        createNotificationChannel();

    }

    private void fetchUsernameFromFirestore() {
        DocumentReference userRef = firestore.collection("newUsers").document(uniqueCode);

        userRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                username = documentSnapshot.getString("username");
                Log.d("EmergencyScreen", "Fetched username: " + username);
            } else {
                Log.e("EmergencyScreen", "User not found in Firestore");
            }
        }).addOnFailureListener(e -> Log.e("EmergencyScreen", "Failed to fetch username", e));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_emergency_screen, container, false);

        btnSOS = view.findViewById(R.id.btnSOS);
        btnCancel = view.findViewById(R.id.btnCancel);
        btnConfirm = view.findViewById(R.id.btnConfirm);
        sosStatus = view.findViewById(R.id.textView29);

        group_33 = btnSOS.getForeground();
        btnConfirm.setEnabled(false);

        btnSOS.setOnClickListener(v -> startCountdown());

        btnCancel.setOnClickListener(v -> cancelCountdown());

        btnConfirm.setOnClickListener(v -> {
            if (countDownTimer != null) {
                countDownTimer.cancel();
            }
            sosStatus.setText("SOS Confirmed and sent!");
            fetchLocationAndSendSOS();
        });

        return view;
    }

    private void cancelCountdown() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        resetSOSButton();
        sosStatus.setText("SOS canceled");
    }

    private void fetchLocationAndSendSOS() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        currentLocation = location;
                        sendSOSToFirestore(currentLocation.getLatitude(), currentLocation.getLongitude());
                    } else {
                        Log.e("EmergencyScreen", "Failed to retrieve location");
                    }
                });
    }

    // Method to create a notification channel
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableLights(true);
            channel.enableVibration(true);
            channel.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM), null);

            NotificationManager notificationManager = getActivity().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }


    private void sendSOSToFirestore(double latitude, double longitude) {
        String sosMessage = username + " is in Danger, Need Help ASAP!";
        String currentTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

        Map<String, Object> sosData = new HashMap<>();
        sosData.put("message", sosMessage);
        sosData.put("senderUsername", username);
        sosData.put("uniqueCode", uniqueCode);
        sosData.put("latitude", latitude);
        sosData.put("longitude", longitude);
        sosData.put("locationDescription", "Near " + getApproximateLocation(latitude, longitude));
        sosData.put("timeSent", currentTime);
        sosData.put("isSOS", true);

        CollectionReference messagesRef = firestore.collection("activityType")
                .document(selectedActivityType)
                .collection("activities")
                .document(activityName)
                .collection("messages");

        messagesRef.add(sosData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("EmergencyScreen", "SOS Message sent successfully");
                    sendSMSNotificationToGroupMembers(sosMessage);


                })
                .addOnFailureListener(e -> Log.e("EmergencyScreen", "Error sending SOS message", e));
    }

    private void sendSMSNotificationToGroupMembers(String sosMessage) {
        // Assuming group members are stored under activityType -> activities -> activityName -> groupmembers
        CollectionReference groupMembersRef = firestore.collection("activityType")
                .document(selectedActivityType)
                .collection("activities")
                .document(activityName)
                .collection("groupmembers");

        groupMembersRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (DocumentSnapshot document : task.getResult()) {
                    String memberUniqueCode = document.getId();

                    // Fetch user's phone number from newUsers collection
                    DocumentReference userRef = firestore.collection("newUsers").document(memberUniqueCode);
                    userRef.get().addOnSuccessListener(userDocument -> {
                        if (userDocument.exists()) {
                            String phoneNum = userDocument.getString("phoneNum");
                            if (phoneNum != null) { // Ensure phoneNum is not null
                                sendSMS(phoneNum, sosMessage);
                            } else {
                                Log.e("EmergencyScreen", "Phone number is null for uniqueCode: " + memberUniqueCode);
                            }
                        } else {
                            Log.e("EmergencyScreen", "User not found in newUsers for uniqueCode: " + memberUniqueCode);
                        }
                    }).addOnFailureListener(e -> Log.e("EmergencyScreen", "Failed to fetch user phone number", e));
                }
            } else {
                Log.e("EmergencyScreen", "Error fetching group members", task.getException());
            }
        });
    }




    private void sendSMS(String phoneNumber, String message) {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.SEND_SMS}, 2);
            return;
        }

        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        Log.d("EmergencyScreen", "SMS sent to: " + phoneNumber);
    }





    private String getApproximateLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        String locationDescription = "Unknown Location";

        try {
            // Get a list of addresses from the Geocoder based on the latitude and longitude
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);

                // Extract detailed information
                String streetAddress = address.getThoroughfare();  // Street name
                String subLocality = address.getSubLocality();     // Neighborhood
                String locality = address.getLocality();           // City
                String postalCode = address.getPostalCode();       // Postal code
                String adminArea = address.getAdminArea();         // State/Region

                // Build a more detailed location string
                StringBuilder detailedLocation = new StringBuilder();

                if (streetAddress != null) {
                    detailedLocation.append(streetAddress).append(", ");
                }
                if (subLocality != null) {
                    detailedLocation.append(subLocality).append(", ");
                }
                if (locality != null) {
                    detailedLocation.append(locality).append(", ");
                }
                if (postalCode != null) {
                    detailedLocation.append(postalCode).append(", ");
                }
                if (adminArea != null) {
                    detailedLocation.append(adminArea).append(", ");
                }

                // Remove trailing commas and whitespace if any
                locationDescription = detailedLocation.toString().trim().replaceAll(", $", "");
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Log the error and use latitude/longitude as fallback
            locationDescription = "Lat: " + latitude + ", Long: " + longitude;
        }

        return locationDescription;
    }


    private void startCountdown() {
        btnSOS.setEnabled(false);
        btnConfirm.setEnabled(true);
        sosStatus.setText("Ready to Send SOS. Press Confirm");
        btnSOS.setForeground(null);
        btnSOS.setText("10");

        countDownTimer = new CountDownTimer(10000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                btnSOS.setText(String.valueOf(millisUntilFinished / 1000));
            }

            @Override
            public void onFinish() {
                sosStatus.setText("SOS sent automatically!");
                resetSOSButton();
                fetchLocationAndSendSOS();
            }
        };

        countDownTimer.start();
    }

    private void resetSOSButton() {
        btnSOS.setText("");
        btnSOS.setForeground(group_33);
        btnConfirm.setEnabled(false);
        btnSOS.setEnabled(true);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fetchLocationAndSendSOS();
            } else {
                Log.e("EmergencyScreen", "Location permission denied");
            }
        }else if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // SMS permission granted
            } else {
                Log.e("EmergencyScreen", "SMS permission denied");
            }
        }
    }
}
