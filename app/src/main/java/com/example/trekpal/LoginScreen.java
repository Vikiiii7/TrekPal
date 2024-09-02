package com.example.trekpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class LoginScreen extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, gotoRegister;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);

        db = FirebaseFirestore.getInstance();

        etUsername = findViewById(R.id.etUname);
        etPassword = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);
        gotoRegister = findViewById(R.id.gotoRegister);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        gotoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreen.this, RegisterScreen.class);
                startActivity(intent);
            }
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password)) {
            Toast.makeText(LoginScreen.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference usersRef = db.collection("newUsers");

        usersRef.whereEqualTo("username", username).whereEqualTo("password", password).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && !task.getResult().isEmpty()) {
                // User authenticated successfully
                QueryDocumentSnapshot document = (QueryDocumentSnapshot) task.getResult().getDocuments().get(0);
                String uniqueCode = document.getId(); // Get the unique code (document ID)


                // Pass the unique code and username to the main activity
                Intent intent = new Intent(LoginScreen.this, MainActivity.class);
                intent.putExtra("uniqueCode", uniqueCode);
                intent.putExtra("username", username); // Use the original username variable
                startActivity(intent);
                finish(); // Close the login activity
            } else {
                // Authentication failed
                Toast.makeText(LoginScreen.this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(LoginScreen.this, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }


}
