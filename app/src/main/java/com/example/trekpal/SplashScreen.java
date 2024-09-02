package com.example.trekpal;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        // Delay of 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                // Start LoginScreen activity
                Intent intent = new Intent(SplashScreen.this, LoginScreen.class);
                startActivity(intent);
                // Finish SplashScreen activity so the user can't go back to it
                finish();
            }
        }, 1500); // 3000 milliseconds = 3 seconds
    }
}
