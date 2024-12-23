package com.example.trekpal;

import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

public class AddMemberScreen extends AppCompatActivity {

    private FirebaseFirestore firestore;
    private EditText editTextNumber;
    private ImageButton sendInvBtn;
    private String activityName;  // Declare here as a class-level variable
    private String selectedActivityType; // Add this to store the selected activity type (e.g., "Hiking")


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_member_screen);

        firestore = FirebaseFirestore.getInstance();
        editTextNumber = findViewById(R.id.editTextNumber);
        sendInvBtn = findViewById(R.id.sendInvBtn);


        // Retrieve the passed activity details
        activityName = getIntent().getStringExtra("activityName");
        selectedActivityType = getIntent().getStringExtra("activityType");

        // Set an InputFilter to limit the input to 6 digits
        editTextNumber.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6), new DigitsInputFilter()});




        Button btnCancel2 = findViewById(R.id.btnCancel2);
        btnCancel2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the activity and return to the previous one
                finish();
            }
        });

        sendInvBtn.setOnClickListener(v -> sendInvitation());
    }


    private void sendInvitation() {
        String uniqueCode = editTextNumber.getText().toString().trim();

        if (TextUtils.isEmpty(uniqueCode)) {
            Toast.makeText(this, "Please enter a valid Unique ID", Toast.LENGTH_SHORT).show();
            return;
        }

        // Step 1: Check if a document with the given uniqueID exists in newUsers
        firestore.collection("newUsers").document(uniqueCode).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (task.getResult().exists()) {
                            // User exists, proceed to send invitation
                            addInvitationToGroup(uniqueCode);
                        } else {
                            // User does not exist
                            Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "Error checking user", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    private void addInvitationToGroup(String uniqueCode) {
        // Step 2: Add uniqueID to the invitations subcollection under the specific activityName within the selected activityType
        firestore.collection("activityType").document(selectedActivityType)
                .collection("activities").document(activityName)
                .collection("invitations").document(uniqueCode)
                .set(new Invitation(uniqueCode))
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(this, "Invitation sent successfully!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Error sending invitation", Toast.LENGTH_SHORT).show());
    }

    // Define a simple Invitation model class
    public static class Invitation {
        public String uniqueCode;

        public Invitation() {} // Needed for Firestore

        public Invitation(String uniqueCode) {
            this.uniqueCode = uniqueCode;
        }
    }

    // Custom InputFilter to only allow digits
    private static class DigitsInputFilter implements InputFilter {
        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!Character.isDigit(source.charAt(i))) {
                    return ""; // Reject non-digit characters
                }
            }
            return null; // Accept the input
        }
    }
}

