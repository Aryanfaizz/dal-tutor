package com.example.daltutor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;

abstract class DashboardActivity extends AppCompatActivity {
    protected String role;
    protected String username;

    abstract void getLayout();
    abstract void setup();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayout();
        setup();

        TextView usernameLabel = findViewById(R.id.usernameLabel);
        TextView roleLabel = findViewById(R.id.roleLabel);

        Intent intent = getIntent();
        this.username = intent.getStringExtra("USERNAME");
        usernameLabel.setText(username);
        this.role = intent.getStringExtra("ROLE");
        roleLabel.setText(role);

        // Initialize the Logout Button
        Button logoutButton = findViewById(R.id.logout_button);

        logoutButton.setOnClickListener(v -> {
            Intent logoutIntent = new Intent(DashboardActivity.this, LogoutActivity.class);
            startActivity(logoutIntent);
            finish(); // Close the current activity
        });
    }


}
