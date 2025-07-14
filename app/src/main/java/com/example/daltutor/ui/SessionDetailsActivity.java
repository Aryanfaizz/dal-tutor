package com.example.daltutor.ui;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;

import java.math.BigDecimal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.daltutor.R;
import com.example.daltutor.core.Posting;
import com.example.daltutor.firebase.DatabaseHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.paypal.android.sdk.payments.PayPalConfiguration;

import java.io.IOException;

import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;


public class SessionDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {
    // TUTORIAL REGISTRATION: variables for
    private Button registerButton;
    private TextView registrationCountText;
    private String postingId;
    private String username;
    private String userRole;
    private Button paymentButton;
    private TextView paymentConfirmationText;
    private PayPalConfiguration payPalConfig;
    private ActivityResultLauncher<Intent> activityResultLauncher;
    private static final String TAG = MainActivity.class.getName();


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Intent intent = getIntent();
        postingId = intent.getStringExtra("POSTING_ID");

        DatabaseHelper.getPostingFromID(postingId, new DatabaseHelper.PostingCallback() {
            @Override
            public void onPostingFound(Posting p) {
                String address = p.getAddress();
                Geocoder geo = new Geocoder(SessionDetailsActivity.this);
                Address geoAddress = null;
                try {
                    if (!address.isEmpty()) {
                        geoAddress = geo.getFromLocationName(address, 1).get(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (geoAddress != null) {
                    MarkerOptions markerOptions = new MarkerOptions();
                    LatLng latLng = new LatLng(geoAddress.getLatitude(), geoAddress.getLongitude());
                    markerOptions.position(latLng);
                    markerOptions.title(address);
                    googleMap.addMarker(markerOptions);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_details);

        configPayPal();
        initActivityLauncher();


        // Get username, role, and posting ID from intent
        Intent intent = getIntent();
        postingId = intent.getStringExtra("POSTING_ID");
        username = intent.getStringExtra("USERNAME");
        userRole = intent.getStringExtra("ROLE");

        // Initialize registration UI elements
        registerButton = findViewById(R.id.register_button);
        registrationCountText = findViewById(R.id.registration_count_text);
        paymentButton = findViewById(R.id.payment_button);
        paymentConfirmationText = findViewById(R.id.payment_confirmation_text);

        // Set up back button
        Button backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // Initialize map fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_frag);
        mapFragment.getMapAsync(this);

        // If user is a tutor or username is not provided, hide register button
        if ("Tutor".equals(userRole) || username == null || username.isEmpty()) {
            registerButton.setVisibility(View.GONE);
            registrationCountText.setVisibility(View.GONE);
            paymentButton.setVisibility((View.GONE));
            paymentConfirmationText.setVisibility((View.GONE));
        } else {
            // For students, check if they're already registered
            checkRegistrationStatus();
            updateRegistrationCount();

            // Set click listener for registration button
            registerButton.setOnClickListener(v -> registerForTutorial());
            paymentButton.setOnClickListener(v -> processPayment());
        }

        // Load posting details
        loadPostingDetails();
    }

    private void loadPostingDetails() {
        DatabaseHelper.getPostingFromID(postingId, new DatabaseHelper.PostingCallback() {
            @Override
            public void onPostingFound(Posting posting) {
                TextView desc_text = findViewById(R.id.description_text);
                TextView fee_text = findViewById(R.id.fee_text);
                TextView duration_text = findViewById(R.id.duration_text);
                TextView topic_text = findViewById(R.id.topic_text);
                TextView tutor_text = findViewById(R.id.tutor_text);
                TextView datetime_text = findViewById(R.id.datetime_text);
                TextView address_text = findViewById(R.id.address_text);

                desc_text.setText(posting.getDescription());
                fee_text.setText("$" + posting.getFee());
                duration_text.setText(posting.getDuration() + " minutes");
                topic_text.setText(posting.getTopic());
                tutor_text.setText(posting.getTutor());
                datetime_text.setText(posting.getDatetime());
                address_text.setText(posting.getAddress());

                if (posting.getFee() != null && !posting.getFee().isEmpty()) {
                    paymentButton.setText("$" + posting.getFee());
                } else {
                    paymentButton.setText("Payment");
                }
            }
        });
    }

    // TUTORIAL REGISTRATION helper methods
    private void checkRegistrationStatus() {
        DatabaseHelper.isStudentRegistered(username, postingId, new DatabaseHelper.RegistrationCheckCallback() {
            @Override
            public void onResult(boolean isRegistered) {
                if (isRegistered) {
                    registerButton.setText("Already Registered");
                    registerButton.setEnabled(false);
                    registerButton.setBackgroundTintList(getResources().getColorStateList(android.R.color.darker_gray));
                    paymentButton.setVisibility((View.VISIBLE));
                    paymentConfirmationText.setVisibility((View.VISIBLE));
                } else {
                    registerButton.setText("Register for Tutorial");
                    registerButton.setEnabled(true);
                    paymentButton.setVisibility((View.GONE));
                    paymentConfirmationText.setVisibility((View.GONE));
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(SessionDetailsActivity.this,
                        "Error checking registration: " + errorMessage,
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateRegistrationCount() {
        DatabaseHelper.getRegistrationCount(postingId, new DatabaseHelper.RegistrationCountCallback() {
            @Override
            public void onCountReceived(int count) {
                registrationCountText.setText(count + " student(s) registered");
                registrationCountText.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String errorMessage) {
                registrationCountText.setVisibility(View.GONE);
            }
        });
    }

    private void registerForTutorial() {
        DatabaseHelper.registerForTutorial(username, postingId)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(SessionDetailsActivity.this,
                            "Successfully registered for the tutorial!",
                            Toast.LENGTH_LONG).show();
                    checkRegistrationStatus();
                    updateRegistrationCount();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(SessionDetailsActivity.this,
                            "Registration failed: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void initActivityLauncher() {
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        final PaymentConfirmation confirmation = result.getData().getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                        if (confirmation != null) {
                            try {
                                // Get the payment details
                                String paymentDetails = confirmation.toJSONObject().toString(4);
                                Log.i(TAG, paymentDetails);
                                // Extract json response and display it in a text view.
                                JSONObject payObj = new JSONObject(paymentDetails);
                                String payID = payObj.getJSONObject("response").getString("id");
                                String state = payObj.getJSONObject("response").getString("state");
                                paymentConfirmationText.setText(String.format("Payment %s%n with payment id is %s", state, payID));
                            } catch (JSONException e) {
                                Log.e("Error", "an extremely unlikely failure occurred: ", e);
                            }
                        }
                    } else if (result.getResultCode() == PaymentActivity.RESULT_EXTRAS_INVALID) {
                        Log.d(TAG, "Launcher Result Invalid");
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        Log.d(TAG, "Launcher Result Cancelled");
                    }
                });
    }

    private void configPayPal() {

        String PAYPAL_CLIENT_ID = "AdnRx4d8lGoIKWHbM1BD8Z1CAytLNnvOwE4IOsyA6OHZyi8H0sjNaFvZ1nd9jdOM7l5WziaSv7mjVADO";
        payPalConfig = new PayPalConfiguration()
                .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
                .clientId(PAYPAL_CLIENT_ID);

        // Start PayPalService
        Intent serviceConfig = new Intent(this, PayPalService.class);
        serviceConfig.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        startService(serviceConfig);
    }


    private void processPayment(){

        TextView feeText = findViewById(R.id.fee_text);
        paymentButton.setText(feeText.getText().toString());

        String rawFee = feeText.getText().toString().replace("$", "").trim();
        BigDecimal feeDecimal = new BigDecimal(rawFee);

        final PayPalPayment payPalPayment = new PayPalPayment(
                feeDecimal,
                "CAD",
                "Purchase Goods",
                PayPalPayment.PAYMENT_INTENT_SALE
        );


        // Create Paypal Payment activity intent
        final Intent intent = new Intent(this, PaymentActivity.class);
        // Adding paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfig);
        // Adding paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        // Starting Activity Request launcher
        activityResultLauncher.launch(intent);

    }

    @Override
    public void onDestroy() {
        // Stop service when done
        stopService(new Intent(this, PayPalService.class));
        super.onDestroy();
    }

}