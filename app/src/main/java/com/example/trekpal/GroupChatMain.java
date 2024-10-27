package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class GroupChatMain extends AppCompatActivity {

    private String activityName, selectedActivityType, uniqueCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat_main);

        // Retrieve activity details from intent
        activityName = getIntent().getStringExtra("activityName");
        selectedActivityType = getIntent().getStringExtra("activityType");
        uniqueCode = getIntent().getStringExtra("uniqueCode");

        // Log the received values to check if they are null or contain expected data
        Log.d("GroupChatMain", "Activity Name: " + activityName);
        Log.d("GroupChatMain", "Selected Activity Type: " + selectedActivityType);
        Log.d("GroupChatMain", "Unique Code: " + uniqueCode);

        // Set up the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(activityName);
        }

        // Load ChatScreenFragment on start
        if (savedInstanceState == null) {
            ChatScreenFragment chatScreenFragment = new ChatScreenFragment();
            Bundle args = new Bundle();
            args.putString("activityName", activityName);
            args.putString("activityType", selectedActivityType);
            args.putString("uniqueCode", uniqueCode);
            chatScreenFragment.setArguments(args);
            loadFragment(chatScreenFragment);
        }

        // Initialize and set up the bottom navigation bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_nav_chat);
        bottomNavigationView.setSelectedItemId(R.id.navigation_chat);

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            Bundle bundle = new Bundle();
            bundle.putString("activityName", activityName);
            bundle.putString("activityType", selectedActivityType);
            bundle.putString("uniqueCode", uniqueCode);

            int itemId = item.getItemId();
            if (itemId == R.id.navigation_chat) {
                ChatScreenFragment chatFragment = new ChatScreenFragment();
                chatFragment.setArguments(bundle);
                selectedFragment = chatFragment;

            } else if (itemId == R.id.navigation_map) {
                MapViewFragment mapFragment = new MapViewFragment();
                mapFragment.setArguments(bundle);
                selectedFragment = mapFragment;

            } else if (itemId == R.id.navigation_emergency) {
                EmergencyScreenFragment emergencyFragment = new EmergencyScreenFragment();
                emergencyFragment.setArguments(bundle);
                selectedFragment = emergencyFragment;
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    // Method to load fragments
    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_groupchat, fragment)
                .commit();
    }

    // Inflate the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);  // Inflate the toolbar menu
        return true;
    }

    // Handle toolbar menu item clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menuGroupDetails) {
            Intent intent = new Intent(GroupChatMain.this, GroupDetailsScreen.class);
            intent.putExtra("activityName", activityName);
            intent.putExtra("activityType", selectedActivityType); // Make sure you are passing activity type
            intent.putExtra("uniqueCode", uniqueCode);
            startActivity(intent);
            return true;
        } else if (id == R.id.menuAddMembers) {
            Intent intent = new Intent(GroupChatMain.this, AddMemberScreen.class);
            intent.putExtra("activityName", activityName);
            intent.putExtra("activityType", selectedActivityType); // Pass the selected activity type
            startActivity(intent);
            return true;
        } else if (id == R.id.menuSOSAlerts) {
            Intent intent = new Intent(GroupChatMain.this, SOSAlertScreen.class);
            intent.putExtra("activityName", activityName);
            intent.putExtra("activityType", selectedActivityType); // Pass the selected activity type
            intent.putExtra("uniqueCode", uniqueCode);
            startActivity(intent);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }
}
