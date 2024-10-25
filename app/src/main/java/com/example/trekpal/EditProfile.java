package com.example.trekpal;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 1; // to only let pick 1 image
    private static final int REQUEST_CODE_READ_EXTERNAL_STORAGE = 100;

    private EditText updUsername, updFullname, updPassword, updEmail, updAddress, updGender, updPhoneNum;
    private FirebaseFirestore db;
    private String uniqueCode;
    // Declare new UI elements
    private ImageView profileImageView;
    private Button uploadPictureBtn;
    // Declare Firebase Storage reference
    private FirebaseStorage storage;
    private StorageReference storageReference;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize UI elements
        profileImageView = findViewById(R.id.profileImageView);
        uploadPictureBtn = findViewById(R.id.uploadPictureBtn);
        updUsername = findViewById(R.id.updUsername);
        updFullname = findViewById(R.id.updFullname);
        updPassword = findViewById(R.id.updPassword);
        updEmail = findViewById(R.id.updEmail);
        updAddress = findViewById(R.id.updAddress);
        updGender = findViewById(R.id.updGender);
        updPhoneNum = findViewById(R.id.updPhoneNum);

        db = FirebaseFirestore.getInstance();

        // Initialize Firebase Storage
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Get the user information from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            uniqueCode = extras.getString("uniqueCode");
            updUsername.setText(extras.getString("username"));
            updFullname.setText(extras.getString("fullname"));
            updPassword.setText(extras.getString("password"));
            updEmail.setText(extras.getString("email"));
            updAddress.setText(extras.getString("address"));
            updGender.setText(extras.getString("gender"));
            updPhoneNum.setText(extras.getString("phoneNum"));
        }

        findViewById(R.id.updBtnProfile).setOnClickListener(this::updateUserProfile);
        findViewById(R.id.goBackBtn).setOnClickListener(v -> finish());

        // Handle image upload button click
        uploadPictureBtn.setOnClickListener(v -> chooseImage());

        // Load and display existing profile picture (if available)
        loadProfileImage();
    }

    private void chooseImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_MEDIA_IMAGES},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_CODE_READ_EXTERNAL_STORAGE);
            } else {
                openImagePicker();
            }
        }
    }


    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle selected image result
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                // Display selected image in the ImageView
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profileImageView.setImageBitmap(bitmap);

                // Upload the image to Firebase Storage
                uploadImageToFirebase(imageUri);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // Upload the image to Firebase Storage
    private void uploadImageToFirebase(Uri imageUri) {
        if (imageUri != null) {
            // Create a reference to the profile pictures folder and unique filename
            StorageReference ref = storageReference.child("profilePictures/" + uniqueCode + ".jpg");

            // Upload the file to Firebase Storage
            ref.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                // Get the download URL and update the user's profile picture in Firestore
                ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Update Firestore with the profile picture URL
                    db.collection("newUsers").document(uniqueCode)
                            .update("profilePictureUrl", imageUrl)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(EditProfile.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(EditProfile.this, "Failed to update profile picture.", Toast.LENGTH_SHORT).show();
                                }
                            });
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(EditProfile.this, "Image upload failed.", Toast.LENGTH_SHORT).show();
            });
        }
    }

    // Load the existing profile image (if available)
    private void loadProfileImage() {
        db.collection("newUsers").document(uniqueCode)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists() && documentSnapshot.contains("profilePictureUrl")) {
                        String imageUrl = documentSnapshot.getString("profilePictureUrl");
                        // Load image using a library like Glide
                        Glide.with(this).load(imageUrl).into(profileImageView);
                    }
                });
    }

    private void updateUserProfile(View view) {
        String newUsername = updUsername.getText().toString();
        String newFullname = updFullname.getText().toString();
        String newPassword = updPassword.getText().toString();
        String newEmail = updEmail.getText().toString();
        String newAddress = updAddress.getText().toString();
        String newGender = updGender.getText().toString();
        String newPhoneNum = updPhoneNum.getText().toString();

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put("username", newUsername);
        userUpdates.put("fullname", newFullname);
        userUpdates.put("password", newPassword);
        userUpdates.put("email", newEmail);
        userUpdates.put("address", newAddress);
        userUpdates.put("gender", newGender);
        userUpdates.put("phoneNum", newPhoneNum);

        db.collection("newUsers").document(uniqueCode).update(userUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(EditProfile.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        Exception e = task.getException();
                        if (e != null) {
                            Toast.makeText(EditProfile.this, "Error updating profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(EditProfile.this, "Error updating profile: Unknown error", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }



    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, proceed with opening image picker
                openImagePicker();
            } else {
                Toast.makeText(this, "Permission denied to access external storage", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
