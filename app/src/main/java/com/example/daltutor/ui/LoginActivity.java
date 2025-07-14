package com.example.daltutor.ui;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private DatabaseReference usersRef;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);


        usersRef = FirebaseDatabase.getInstance().getReference("Users");


        emailEditText = findViewById(R.id.emailforlogin);
        passwordEditText = findViewById(R.id.passwordforlogin);
        Button loginButton = findViewById(R.id.loginbutton);
        Button createAccountButton = findViewById(R.id.create_account_button);



        loginButton.setOnClickListener(v -> {
            String email = emailEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
                Toast.makeText(LoginActivity.this, "Please enter both email and password.", Toast.LENGTH_SHORT).show();
            } else {
                authenticateUser(email, password);
            }
        });

        createAccountButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });

        TextView forgotPasswordPrompt = findViewById(R.id.forgot_password_prompt);
        forgotPasswordPrompt.setText(Html.fromHtml("<u>Forgot your password?</u>", Html.FROM_HTML_MODE_LEGACY));

        forgotPasswordPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(this, ResetPasswordActivity.class);
            startActivity(intent);
        });
    }

    //Email and Password were not
    @Override
    protected void onResume() {
        super.onResume();

        // Clear the email and password fields when the activity is resumed
        emailEditText.setText("");
        passwordEditText.setText("");
    }

    private void authenticateUser(String email, String password) {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean userFound = false;
                String username = "";
                String role = "";

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String dbEmail = userSnapshot.child("email").getValue(String.class);
                    String dbPassword = userSnapshot.child("password").getValue(String.class);
                    username = userSnapshot.child("username").getValue(String.class);
                    role = userSnapshot.child("role").getValue(String.class);

                    if (dbEmail != null && dbPassword != null && dbEmail.equals(email) && dbPassword.equals(password)) {
                        userFound = true;
                        break;
                    }
                }

                if (userFound) {
                    Toast.makeText(LoginActivity.this, "Login Successful! Welcome, " + username + "!", Toast.LENGTH_LONG).show();

                    // change activity
                    if (role.equals("Student")) {
                        Intent intent = new Intent(LoginActivity.this, StudentDashboardActivity.class);
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("ROLE", role);
                        startActivity(intent);
                    } else if (role.equals("Tutor")) {
                        Intent intent = new Intent(LoginActivity.this, TutorDashboardActivity.class);
                        intent.putExtra("USERNAME", username);
                        intent.putExtra("ROLE", role);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid email or password.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(LoginActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
