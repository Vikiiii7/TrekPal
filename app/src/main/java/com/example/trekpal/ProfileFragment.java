package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvFullname, tvPassword, tvEmail, tvAddress, tvGender, tvPhoneNum, tvUniqueID;
    private FirebaseFirestore db;
    private String uniqueCode, username, fullname, password, email, address, gender, phoneNum;
    private ImageView profileImageView;
    private FirebaseStorage storage;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        tvUsername = view.findViewById(R.id.tvUsername);
        tvFullname = view.findViewById(R.id.tvFullname);
        tvPassword = view.findViewById(R.id.tvPassword);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvGender = view.findViewById(R.id.tvGender);
        tvPhoneNum = view.findViewById(R.id.tvPhoneNum);
        tvUniqueID = view.findViewById(R.id.tvUniqueID);
        ImageButton editProfileBtn = view.findViewById(R.id.editProfileBtn);
        profileImageView = view.findViewById(R.id.profileImageView);

        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        uniqueCode = getArguments().getString("uniqueCode");

        fetchUserProfile(uniqueCode);

        editProfileBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), EditProfile.class);
            intent.putExtra("uniqueCode", uniqueCode);
            intent.putExtra("username", username);
            intent.putExtra("fullname", fullname);
            intent.putExtra("password", password);
            intent.putExtra("email", email);
            intent.putExtra("address", address);
            intent.putExtra("gender", gender);
            intent.putExtra("phoneNum", phoneNum);
            startActivity(intent);
        });

        return view;
    }

    private void fetchUserProfile(String uniqueCode) {
        db.collection("newUsers").document(uniqueCode).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    username = document.getString("username");
                    fullname = document.getString("fullname");
                    password = document.getString("password");
                    email = document.getString("email");
                    address = document.getString("address");
                    gender = document.getString("gender");
                    phoneNum = document.getString("phoneNum");

                    tvUsername.setText(username);
                    tvFullname.setText(fullname);
                    tvPassword.setText(password);
                    tvEmail.setText(email);
                    tvAddress.setText(address);
                    tvGender.setText(gender);
                    tvPhoneNum.setText(phoneNum);

                    // Set the unique code to the tvUniqueID TextView
                    tvUniqueID.setText(uniqueCode);

                    // Load profile picture
                    loadProfileImage(uniqueCode);

                } else {
                    Toast.makeText(getActivity(), "No user found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Error fetching document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Method to load profile image from Firebase Storage
    private void loadProfileImage(String uniqueCode) {
        StorageReference profileImageRef = storage.getReference().child("profilePictures/" + uniqueCode + ".jpg");

        // Load the image using Glide
        profileImageRef.getDownloadUrl().addOnSuccessListener(uri -> {
            Glide.with(this)
                    .load(uri)
                    .placeholder(R.drawable.profileicon) // Placeholder image
                    .circleCrop() // Make the image circular
                    .into(profileImageView);
        }).addOnFailureListener(e -> {
            // Handle any errors (e.g., image not found)
            Toast.makeText(getActivity(), "Failed to load profile image", Toast.LENGTH_SHORT).show();
        });
    }
}
