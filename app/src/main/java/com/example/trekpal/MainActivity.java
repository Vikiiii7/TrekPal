package com.example.trekpal;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MainActivity extends AppCompatActivity {

    private String uniqueCode;
    private String username;
    private static final String UNIQUE_CODE_KEY = "unique_code_key";
    private static final String USERNAME_KEY = "username_key";
    private FloatingActionButton btnFloatTP, btnLogout, btnCreateActivity;
    private View dimmingView;
    private boolean isFABOpen = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnFloatTP = findViewById(R.id.btnFloatTP);
        btnLogout = findViewById(R.id.btnLogout);
        btnCreateActivity = findViewById(R.id.btnCreateActivity);
        dimmingView = findViewById(R.id.dimmingView);



        // Set up the main FAB click listener
        btnFloatTP.setOnClickListener(view -> {
            if (isFABOpen) {
                closeFABMenu();
            } else {
                openFABMenu();
            }
        });

        // Dimming view click listener to close the menu
        dimmingView.setOnClickListener(view -> closeFABMenu());

        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Redirect to login screen
                Intent intent = new Intent(MainActivity.this, LoginScreen.class);
                startActivity(intent);
                finish(); // Finish the MainActivity so it can't be returned to
            }
        });


        btnCreateActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle create new activity logic
                Intent intent = new Intent(MainActivity.this, CreateNewActScreen.class);
                startActivity(intent);
                closeFABMenu();
            }
        });

        if (savedInstanceState != null) {
            uniqueCode = savedInstanceState.getString(UNIQUE_CODE_KEY);
            username = savedInstanceState.getString(USERNAME_KEY);
        } else {
            Intent intent = getIntent();
            uniqueCode = intent.getStringExtra("uniqueCode");
            username = intent.getStringExtra("username");
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }


        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Intent intent;
                Fragment selectedFragment = null;

                int itemId = item.getItemId();
                if (itemId == R.id.navigation_home) {
                    selectedFragment = new HomeFragment();
                } else if (itemId == R.id.navigation_weather) {
                    intent = new Intent(MainActivity.this, WeatherScreen.class);
                    intent.putExtra("username", MainActivity.this.username);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.navigation_emergency) {
                    intent = new Intent(MainActivity.this, EmergencyScreen.class);
                    intent.putExtra("username", MainActivity.this.username);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.navigation_profile) {
                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("uniqueCode", MainActivity.this.uniqueCode);
                    profileFragment.setArguments(bundle);
                    selectedFragment = profileFragment;
                }

                if (selectedFragment != null) {
                    loadFragment(selectedFragment);
                }
                return true;
            }
        });
    }


    private void openFABMenu() {
        isFABOpen = true;

        // Show the dimming view with fade-in animation
        dimmingView.setVisibility(View.VISIBLE);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        dimmingView.startAnimation(fadeIn);

        // Show the sub-buttons with slide animations
        btnLogout.setVisibility(View.VISIBLE);
        btnCreateActivity.setVisibility(View.VISIBLE);

        // Load animations
        Animation slideRight = AnimationUtils.loadAnimation(this, R.anim.slide_right);
        Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);

        // Start animations with delay for staggered effect
        btnLogout.postDelayed(() -> btnLogout.startAnimation(slideRight), 100);
        btnCreateActivity.postDelayed(() -> btnCreateActivity.startAnimation(slideDown), 100);
    }

    private void closeFABMenu() {
        isFABOpen = false;

        // Fade out and hide the dimming view
        Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
        dimmingView.startAnimation(fadeOut);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                // No action needed
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                dimmingView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // No action needed
            }
        });

        // Slide out and hide the sub-buttons
        Animation slideLeft = AnimationUtils.loadAnimation(this, R.anim.slide_left);
        Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);

        // Start animations with delay for staggered effect
        btnLogout.postDelayed(() -> {
            btnLogout.startAnimation(slideLeft);
            slideLeft.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    btnLogout.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }, 100);

        btnCreateActivity.postDelayed(() -> {
            btnCreateActivity.startAnimation(slideUp);
            slideUp.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) { }

                @Override
                public void onAnimationEnd(Animation animation) {
                    btnCreateActivity.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) { }
            });
        }, 100);
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(UNIQUE_CODE_KEY, uniqueCode);
        outState.putString(USERNAME_KEY, username);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        uniqueCode = savedInstanceState.getString(UNIQUE_CODE_KEY);
        username = savedInstanceState.getString(USERNAME_KEY);
    }

    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }
}
