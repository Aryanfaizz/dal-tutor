package com.example.daltutor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText otpEditText;
    private Button sendOtpButton;
    private Button verifyOtpButton;
    private DatabaseReference usersRef;
    private String generatedOtp;
    private String userKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Initialize Firebase reference
        usersRef = FirebaseDatabase.getInstance().getReference("Users");

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username_reset);
        emailEditText = findViewById(R.id.email_reset);
        otpEditText = findViewById(R.id.otp_reset);

        sendOtpButton = findViewById(R.id.send_otp_button);
        verifyOtpButton = findViewById(R.id.verify_otp_button);

        // Initially, hide OTP fields
        otpEditText.setVisibility(View.GONE);
        verifyOtpButton.setVisibility(View.GONE);

        // Send OTP
        sendOtpButton.setOnClickListener(v -> sendOtp());

        // Verify OTP
        verifyOtpButton.setOnClickListener(v -> verifyOtp());
    }

    private void sendOtp() {
        String username = usernameEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();

        if (TextUtils.isEmpty(username) || TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter your username and email.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userExists = false;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String dbUsername = userSnapshot.child("username").getValue(String.class);
                    String dbEmail = userSnapshot.child("email").getValue(String.class);

                    if (dbUsername != null && dbEmail != null && dbUsername.equals(username) && dbEmail.equals(email)) {
                        userExists = true;
                        userKey = userSnapshot.getKey();
                        break;
                    }
                }

                if (userExists) {
                    generatedOtp = generateOtp();
                    Toast.makeText(ResetPasswordActivity.this, "OTP Sent: " + generatedOtp, Toast.LENGTH_LONG).show();

                    // Hide username & email fields and send OTP button
                    usernameEditText.setVisibility(View.GONE);
                    emailEditText.setVisibility(View.GONE);
                    sendOtpButton.setVisibility(View.GONE);

                    // Show OTP input and verify button
                    otpEditText.setVisibility(View.VISIBLE);
                    verifyOtpButton.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(ResetPasswordActivity.this, "No account found with this username and email.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ResetPasswordActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp() {
        String enteredOtp = otpEditText.getText().toString().trim();

        if (TextUtils.isEmpty(enteredOtp)) {
            Toast.makeText(this, "Please enter the OTP.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (enteredOtp.equals(generatedOtp)) {
            Toast.makeText(this, "OTP Verified! Redirecting to create a new password.", Toast.LENGTH_SHORT).show();

            // Redirect to NewPasswordActivity
            Intent intent = new Intent(ResetPasswordActivity.this, NewPasswordActivity.class);
            intent.putExtra("USER_KEY", userKey);  // Pass userKey
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Incorrect OTP. Try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }
}
