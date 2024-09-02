package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfile extends AppCompatActivity {

    private EditText updUsername, updFullname, updPassword, updEmail, updAddress, updGender, updPhoneNum;
    private FirebaseFirestore db;
    private String uniqueCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        updUsername = findViewById(R.id.updUsername);
        updFullname = findViewById(R.id.updFullname);
        updPassword = findViewById(R.id.updPassword);
        updEmail = findViewById(R.id.updEmail);
        updAddress = findViewById(R.id.updAddress);
        updGender = findViewById(R.id.updGender);
        updPhoneNum = findViewById(R.id.updPhoneNum);

        db = FirebaseFirestore.getInstance();

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

}
