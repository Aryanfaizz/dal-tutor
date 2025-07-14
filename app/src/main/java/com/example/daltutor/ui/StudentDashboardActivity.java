package com.example.daltutor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Message;
import android.util.Log;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.daltutor.R;
import com.example.daltutor.firebase.DatabaseHelper;
import com.example.daltutor.notifs.MessagingService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class StudentDashboardActivity extends DashboardActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final LatLng defaultLocation = new LatLng(44.6375, -63.5750);

    private TableLayout tutorTable;
    private TableLayout preferredTutorContainer;
    private Button showPreferredTutorsButton;
    private Button showTutorsButton;
    private DatabaseReference usersRef;
    private DatabaseReference  preferredTutorsRef;
    private TextView roleLabel;
    private String userNameStu;

    @Override
    void getLayout() {
        setContentView(R.layout.activity_student_dashboard);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    void setup() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
        Intent intent = getIntent();
        userNameStu = intent.getStringExtra("USERNAME");

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        preferredTutorsRef = DatabaseHelper.getStudentPreferredTutorsRef();

        tutorTable = findViewById(R.id.tutor_table);
        preferredTutorContainer = findViewById(R.id.preferred_tutors_container);
        showPreferredTutorsButton = findViewById(R.id.show_preferred_tutors_button);
        showTutorsButton = findViewById(R.id.show_tutors_button);
        roleLabel = findViewById(R.id.roleLabel);

        // Set the welcome message with username and role
        setWelcomeMessage();

        setupShowTutorsButton();
        setupShowPreferredTutorsButton();
        setupSearchButton();

        Button logoutButton = findViewById(R.id.logout_button);
        if (logoutButton != null)
            logoutButton.setOnClickListener(v -> finish());

        Log.d("Notification", "Subscribing to preferred tutors");
        DatabaseHelper.getPreferredTutors(userNameStu).addOnSuccessListener(new OnSuccessListener<ArrayList<String>>() {
            @Override
            public void onSuccess(ArrayList<String> strings) {
                for (String tutor : strings) {
                    FirebaseMessaging.getInstance().subscribeToTopic(tutor).addOnCompleteListener(task -> {
                        Log.d("Notification", "subscription was " + task.isSuccessful() + " for tutor " + tutor);
                    });
                }
            }
        });
    }

    private void setWelcomeMessage() {
        usersRef.orderByChild("username").equalTo(userNameStu)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String role = userSnapshot.child("role").getValue(String.class);
                                if (role != null) {
                                    roleLabel.setText("Welcome, " + userNameStu + " (" + role + ")");
                                } else {
                                    roleLabel.setText("Welcome, " + userNameStu + " (Unknown Role)");
                                }
                            }
                        } else {
                            roleLabel.setText("Welcome, " + userNameStu + " (Role Not Found)");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentDashboardActivity.this, "Failed to load role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        roleLabel.setText("Welcome, " + userNameStu + " (Error)");
                    }
                });
    }

    private void setupSearchButton() {
        Button searchButton = findViewById(R.id.search_tutorials_button);
        searchButton.setOnClickListener(v -> {
            Intent intent = new Intent(StudentDashboardActivity.this, SearchActivity.class);
            // TUTORIAL REGISTRATION: Pass username and role to SearchActivity
            intent.putExtra("USERNAME", userNameStu);
            intent.putExtra("ROLE", "Student");
            startActivity(intent);
        });
    }

    private void setupShowTutorsButton() {
        showTutorsButton.setOnClickListener(v -> {
            tutorTable.removeAllViews();
            loadTutorList();
        });
    }

    private void loadTutorList() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tutorTable.removeAllViews();
                addTableHeader(tutorTable, "Username", "Email", "Action");

                preferredTutorsRef.child(userNameStu).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot preferredSnapshot) {
                        Set<String> preferredTutors = new HashSet<>();
                        for (DataSnapshot tutorSnapshot : preferredSnapshot.getChildren()) {
                            preferredTutors.add(tutorSnapshot.getValue(String.class));
                        }

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String role = userSnapshot.child("role").getValue(String.class);
                            String tutorUsername = userSnapshot.child("username").getValue(String.class);
                            String tutorEmail = userSnapshot.child("email").getValue(String.class);

                            if ("Tutor".equals(role) && !preferredTutors.contains(tutorUsername)) {
                                addTutorRow(tutorUsername, tutorEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentDashboardActivity.this, "Failed to load preferred tutors.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(StudentDashboardActivity.this, "Failed to load tutors.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addTableHeader(TableLayout table, String... headers) {
        TableRow headerRow = new TableRow(this);
        headerRow.setBackgroundColor(0xFFBBDEFB);
        headerRow.setPadding(4, 4, 4, 4);

        for (String header : headers) {
            TextView headerView = new TextView(this);
            headerView.setText(header);
            headerView.setTextColor(0xFF1976D2);
            headerView.setTextSize(16);
            headerView.setTypeface(null, android.graphics.Typeface.BOLD);
            headerView.setPadding(8, 8, 8, 8);
            headerRow.addView(headerView);
        }
        table.addView(headerRow);
    }

    private void addTutorRow(String username, String email) {
        TableRow row = new TableRow(this);
        row.setBackgroundColor(0xFFFFFFFF);
        row.setPadding(4, 4, 4, 4);

        TextView usernameView = new TextView(this);
        usernameView.setText(username);
        usernameView.setTextColor(0xFF212121);
        usernameView.setPadding(8, 8, 8, 8);

        TextView emailView = new TextView(this);
        emailView.setText(email);
        emailView.setTextColor(0xFF212121);
        emailView.setPadding(8, 8, 8, 8);

        Button addButton = new Button(this);
        addButton.setText("Add");
        addButton.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.holo_blue_light));
        addButton.setTextColor(0xFFFFFFFF);
        addButton.setPadding(4, 2, 4, 2);
        addButton.setTextSize(12);
        addButton.setOnClickListener(v -> addTutorToPreferred(username));

        row.addView(usernameView);
        row.addView(emailView);
        row.addView(addButton);

        tutorTable.addView(row);
    }

    private void addTutorToPreferred(String tutorUsername) {
        FirebaseMessaging.getInstance().subscribeToTopic(tutorUsername).addOnCompleteListener(task -> {
            Log.d("Notification", "subscription was " + task.isSuccessful() + " for tutor " + tutorUsername);
        });
        addPreferredTutor(userNameStu, tutorUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(StudentDashboardActivity.this, "Added " + tutorUsername, Toast.LENGTH_SHORT).show();
                    loadTutorList();
                    if (preferredTutorContainer.getChildCount() > 0) {
                        setupShowPreferredTutorsButton();
                        showPreferredTutorsButton.performClick();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(StudentDashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupShowPreferredTutorsButton() {
        showPreferredTutorsButton.setOnClickListener(v -> {
            preferredTutorContainer.removeAllViews();
            addTableHeader(preferredTutorContainer, "Username", "Email", "Action");

            preferredTutorsRef.child(userNameStu).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot tutorSnapshot : snapshot.getChildren()) {
                        String tutorUsername = tutorSnapshot.getValue(String.class);
                        addPreferredTutorToList(tutorUsername, tutorSnapshot.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(StudentDashboardActivity.this, "Failed to load preferred tutors.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addPreferredTutorToList( String tutorUsername, String tutorKey) {
        usersRef.orderByChild("username").equalTo(tutorUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String tutorEmail = userSnapshot.child("email").getValue(String.class);

                                TableRow row = new TableRow(StudentDashboardActivity.this);
                                row.setBackgroundColor(0xFFFFFFFF);
                                row.setPadding(4, 4, 4, 4);

                                TextView usernameView = new TextView(StudentDashboardActivity.this);
                                usernameView.setText(tutorUsername);
                                usernameView.setTextColor(0xFF212121);
                                usernameView.setPadding(8, 8, 8, 8);

                                TextView emailView = new TextView(StudentDashboardActivity.this);
                                emailView.setText(tutorEmail);
                                emailView.setTextColor(0xFF212121);
                                emailView.setPadding(8, 8, 8, 8);

                                Button removeButton = new Button(StudentDashboardActivity.this);
                                removeButton.setText("Remove");
                                removeButton.setBackgroundTintList(ContextCompat.getColorStateList(
                                        StudentDashboardActivity.this, android.R.color.holo_red_dark));
                                removeButton.setTextColor(0xFFFFFFFF);
                                removeButton.setTextSize(12);
                                removeButton.setPadding(4, 2, 4, 2);
                                removeButton.setOnClickListener(v -> removeTutorFromPreferred(tutorKey, tutorUsername, row));

                                row.addView(usernameView);
                                row.addView(emailView);
                                row.addView(removeButton);
                                preferredTutorContainer.addView(row);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(StudentDashboardActivity.this, "Failed to load tutor email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeTutorFromPreferred(String tutorKey, String tutorUsername, TableRow row) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(tutorUsername).addOnCompleteListener(task -> {
            Log.d("Notification", "unsubscription was " + task.isSuccessful() + " for tutor " + tutorUsername);
        });
        removePreferredTutor(userNameStu, tutorKey)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(StudentDashboardActivity.this, "Removed " + tutorUsername, Toast.LENGTH_SHORT).show();
                    preferredTutorContainer.removeView(row);
                    loadTutorList();
                })
                .addOnFailureListener(e -> Toast.makeText(StudentDashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            updateLocation();
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                LatLng targetLocation = (location != null)
                        ? new LatLng(location.getLatitude(), location.getLongitude())
                        : defaultLocation;
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(targetLocation, 15f));
                }
            });
        } catch (SecurityException e) {
            e.printStackTrace();
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED && mMap != null) {
                    mMap.setMyLocationEnabled(true);
                    updateLocation();
                }
            } else {
                if (mMap != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f));
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Adds a tutor to a student's preferred list.
     * @param studentUsername The student's username.
     * @param tutorUsername The tutor's username.
     * @return A Task object to handle success/failure callbacks.
     */
    public Task<Void> addPreferredTutor(String studentUsername, String tutorUsername) {
        return preferredTutorsRef.child(studentUsername).push().setValue(tutorUsername);
    }

    /**
     * Removes a tutor from a student's preferred list.
     * @param studentUsername The student's username.
     * @param tutorKey The Firebase-generated key for the preferred tutor entry.
     * @return A Task object to handle success/failure callbacks.
     */
    public Task<Void> removePreferredTutor(String studentUsername, String tutorKey) {
        return preferredTutorsRef.child(studentUsername).child(tutorKey).removeValue();
    }

}