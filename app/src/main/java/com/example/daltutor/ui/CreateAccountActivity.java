package com.example.daltutor.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.EditText;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.daltutor.R;
import com.example.daltutor.firebase.DatabaseHelper;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {

    // private DatabaseReference usersRef;  // <-- Remove this if no longer needed

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize the Spinner
        Spinner roleSpinner = findViewById(R.id.role_spinner);

        // Define the options for the Spinner (with the default "Choose Your Role" option)
        String[] roles = new String[]{"Choose Your Role", "Student", "Tutor"};

        // Create an ArrayAdapter using the roles array and a simple spinner item layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, roles);

        // Set the style for the dropdown items
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        roleSpinner.setAdapter(adapter);


        EditText usernameEditText = findViewById(R.id.username);
        EditText emailEditText = findViewById(R.id.email);
        EditText phoneEditText = findViewById(R.id.phone_number);
        EditText passwordEditText = findViewById(R.id.password);
        Button createAccountButton = findViewById(R.id.create_account_button);

        TextView loginPrompt = findViewById(R.id.login_prompt);
        loginPrompt.setText(Html.fromHtml("Already have an account? <u>Login</u>", Html.FROM_HTML_MODE_LEGACY));
        loginPrompt.setMovementMethod(LinkMovementMethod.getInstance());

        // Navigate to LoginActivity when the login text is clicked
        loginPrompt.setOnClickListener(v -> {
            Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        // Handle Create Account button click
        createAccountButton.setOnClickListener(view -> {
            // Gather user input
            String username = usernameEditText.getText().toString().trim();
            String email = emailEditText.getText().toString().trim();
            String phone = phoneEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();
            String role = roleSpinner.getSelectedItem().toString();

            // Minimal checks
            if (username.isEmpty() || email.isEmpty() || phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(CreateAccountActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                return;
            }
            if (role.equals("Choose Your Role")) {
                Toast.makeText(CreateAccountActivity.this, "Please select a valid role.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create a Map to hold user data
            Map<String, String> userData = new HashMap<>();
            userData.put("username", username);
            userData.put("email", email);
            userData.put("phone", phone);
            userData.put("password", password);
            userData.put("role", role);

            // Use the DatabaseHelper class to push data to Firebase
            DatabaseHelper.createUser(userData)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(CreateAccountActivity.this, "Account creation failed.", Toast.LENGTH_LONG).show());
        });
    }
}
