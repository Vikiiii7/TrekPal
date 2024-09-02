package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RegisterScreen extends AppCompatActivity {

    private EditText etFullname, etUsername, etPassword, etEmail, etPhoneNum, etAddress;
    private Spinner spinnerGender;
    private Button btnRegister, btnReturn;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        db = FirebaseFirestore.getInstance();

        etFullname = findViewById(R.id.etFullname);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etEmail = findViewById(R.id.etEmail);
        etPhoneNum = findViewById(R.id.etPhoneNum);
        etAddress = findViewById(R.id.etAddress);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnRegister = findViewById(R.id.btnRegister);
        btnReturn = findViewById(R.id.btnReturn);

        // Set up the Spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        // Set onItemSelectedListener for Spinner
        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedGender = (String) parent.getItemAtPosition(position);
                if (!selectedGender.equals("Select Gender")) {
                    Toast.makeText(parent.getContext(), "Selected: " + selectedGender, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Set onClickListener for Register button
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerUser();
            }
        });

        btnReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Close the current activity
            }
        });
    }

    private void registerUser() {
        String fullname = etFullname.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phoneNum = etPhoneNum.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (TextUtils.isEmpty(fullname) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(email) || TextUtils.isEmpty(phoneNum) || TextUtils.isEmpty(address) || gender.equals("Select Gender")) {
            Toast.makeText(RegisterScreen.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a unique 6-digit code
        String uniqueCode = generateUniqueCode();

        CollectionReference usersRef = db.collection("newUsers");

        usersRef.whereEqualTo("email", email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                Toast.makeText(RegisterScreen.this, "Email is already registered", Toast.LENGTH_SHORT).show();
            } else {
                Map<String, Object> user = new HashMap<>();
                user.put("fullname", fullname);
                user.put("username", username);
                user.put("password", password);
                user.put("email", email);
                user.put("phoneNum", phoneNum);
                user.put("address", address);
                user.put("gender", gender);

                // Use the unique code as the document ID
                DocumentReference userDocRef = usersRef.document(uniqueCode);

                userDocRef.set(user).addOnSuccessListener(aVoid -> {
                    Toast.makeText(RegisterScreen.this, "Registration successful", Toast.LENGTH_SHORT).show();
                    // Navigate back to login screen
                    Intent intent = new Intent(RegisterScreen.this, LoginScreen.class);
                    startActivity(intent);
                    finish(); // Close the register activity
                }).addOnFailureListener(e -> {
                    Toast.makeText(RegisterScreen.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(RegisterScreen.this, "Error checking email: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private String generateUniqueCode() {
        Random random = new Random();
        String uniqueCode;
        boolean isUnique = false;

        do {
            int code = 100000 + random.nextInt(900000); // Generate a random 6-digit number
            uniqueCode = String.valueOf(code);

            // Check if the generated code is unique
            isUnique = isUniqueCode(uniqueCode);
        } while (!isUnique);

        return uniqueCode;
    }

    private boolean isUniqueCode(String code) {
        CollectionReference usersRef = db.collection("newUsers");
        boolean[] isUnique = {true};

        usersRef.document(code).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                isUnique[0] = false;
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(RegisterScreen.this, "Error checking unique code: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });

        return isUnique[0];
    }
}
