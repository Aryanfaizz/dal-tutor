package com.example.daltutor.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;

public class MainActivity extends AppCompatActivity {

    @SuppressLint({"MissingInflatedId", "SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize the welcome TextView
        TextView welcomeTextView = findViewById(R.id.welcomeText);

        // Get the username from the intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("USERNAME");

        // Set the welcome message
        if (username != null) {
            welcomeTextView.setText("Welcome, " + username + "!");
        }

        // Initialize the Logout Button
        Button logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(MainActivity.this, LogoutActivity.class);
            startActivity(logoutIntent);
            finish(); // Close the current activity
        });
    }
}
