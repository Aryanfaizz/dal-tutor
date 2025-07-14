package com.example.daltutor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewPasswordActivity extends AppCompatActivity {

    private EditText newPasswordEditText;
    private DatabaseReference usersRef;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_password);

        // Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Get userKey from intent
        userKey = getIntent().getStringExtra("USER_KEY");

        // Initialize UI elements
        newPasswordEditText = findViewById(R.id.new_password_reset);
        Button resetPasswordButton = findViewById(R.id.reset_password_button);

        // Reset password
        resetPasswordButton.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String newPassword = newPasswordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(newPassword)) {
            Toast.makeText(this, "Please enter a new password.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (userKey == null) {
            Toast.makeText(this, "Error: User not found. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.child(userKey).child("password").setValue(newPassword)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(NewPasswordActivity.this, "Password Reset Successfully!", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(NewPasswordActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(NewPasswordActivity.this, "Password reset failed. Try again.", Toast.LENGTH_SHORT).show());
    }
}
