package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView tvUsername, tvFullname, tvPassword, tvEmail, tvAddress, tvGender, tvPhoneNum, tvUniqueID;
    private FirebaseFirestore db;
    private String uniqueCode, username, fullname, password, email, address, gender, phoneNum;

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

        db = FirebaseFirestore.getInstance();

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

                } else {
                    Toast.makeText(getActivity(), "No user found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Error fetching document: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
