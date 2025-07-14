package com.example.daltutor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashSet;
import java.util.Set;

public class TutorDashboardActivity extends DashboardActivity implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private final LatLng defaultLocation = new LatLng(44.6375, -63.5750);

    private TableLayout studentTable;
    private TableLayout preferredStudentContainer;
    private Button showPreferredButton;
    private Button showStudentsButton;
    private DatabaseReference usersRef;
    private DatabaseReference preferredListRef;
    private TextView roleLabel; // Added for roleLabel

    private String usernameTut;

    @Override
    void getLayout() {
        setContentView(R.layout.activity_tutor_dashboard);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    void setup() {
        Intent intent = getIntent();
        usernameTut = intent.getStringExtra("USERNAME");

        Button createPostingButton = findViewById(R.id.create_posting_button);
        if (createPostingButton != null) {
            createPostingButton.setOnClickListener(v -> {
                Intent postingIntent = new Intent(TutorDashboardActivity.this, TutorialPostingActivity.class);
                postingIntent.putExtra("USERNAME", usernameTut);
                startActivity(postingIntent);
            });
        }

        usersRef = FirebaseDatabase.getInstance().getReference("Users");
        preferredListRef = DatabaseHelper.getPreferredStudentsRef();

        studentTable = findViewById(R.id.student_table);
        preferredStudentContainer = findViewById(R.id.preferred_students_container);
        showPreferredButton = findViewById(R.id.show_preferred_button);
        showStudentsButton = findViewById(R.id.show_students_button);
        roleLabel = findViewById(R.id.roleLabel); // Initialize roleLabel

        // Set the welcome message with username and role
        setWelcomeMessage();

        setupShowStudentsButton();
        setupShowPreferredButton();
    }

    private void setWelcomeMessage() {
        usersRef.orderByChild("username").equalTo(usernameTut)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String role = userSnapshot.child("role").getValue(String.class);
                                if (role != null) {
                                    roleLabel.setText("Welcome, " + usernameTut + " (" + role + ")");
                                } else {
                                    roleLabel.setText("Welcome, " + usernameTut + " (Unknown Role)");
                                }
                            }
                        } else {
                            roleLabel.setText("Welcome, " + usernameTut + " (Role Not Found)");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TutorDashboardActivity.this, "Failed to load role: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        roleLabel.setText("Welcome, " + usernameTut + " (Error)");
                    }
                });
    }

    private void setupShowStudentsButton() {
        showStudentsButton.setOnClickListener(v -> {
            studentTable.removeAllViews();
            loadStudentList();
        });
    }

    private void loadStudentList() {
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                studentTable.removeAllViews();
                addTableHeader(studentTable, "Username", "Email", "Action");

                preferredListRef.child(usernameTut).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot preferredSnapshot) {
                        Set<String> preferredStudents = new HashSet<>();
                        for (DataSnapshot studentSnapshot : preferredSnapshot.getChildren()) {
                            preferredStudents.add(studentSnapshot.getValue(String.class));
                        }

                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            String role = userSnapshot.child("role").getValue(String.class);
                            String studentUsername = userSnapshot.child("username").getValue(String.class);
                            String studentEmail = userSnapshot.child("email").getValue(String.class);

                            if ("Student".equals(role) && !preferredStudents.contains(studentUsername)) {
                                addStudentRow(studentUsername, studentEmail);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TutorDashboardActivity.this, "Failed to load preferred students.", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(TutorDashboardActivity.this, "Failed to load students.", Toast.LENGTH_SHORT).show();
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

    private void addStudentRow(String username, String email) {
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
        addButton.setOnClickListener(v -> addStudentToPreferred(username));

        row.addView(usernameView);
        row.addView(emailView);
        row.addView(addButton);

        studentTable.addView(row);
    }

    private void addStudentToPreferred(String studentUsername) {
        preferredListRef.child(usernameTut).push().setValue(studentUsername)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TutorDashboardActivity.this, "Added " + studentUsername, Toast.LENGTH_SHORT).show();
                    loadStudentList();
                    if (preferredStudentContainer.getChildCount() > 0) {
                        setupShowPreferredButton();
                        showPreferredButton.performClick();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(TutorDashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void setupShowPreferredButton() {
        showPreferredButton.setOnClickListener(v -> {
            preferredStudentContainer.removeAllViews();
            addTableHeader(preferredStudentContainer, "Username", "Email", "Action");

            preferredListRef.child(usernameTut).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot studentSnapshot : snapshot.getChildren()) {
                        String studentUsername = studentSnapshot.getValue(String.class);
                        addPreferredStudentToList(studentUsername, studentSnapshot.getKey());
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(TutorDashboardActivity.this, "Failed to load preferred students.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void addPreferredStudentToList(String studentUsername, String studentKey) {
        usersRef.orderByChild("username").equalTo(studentUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String studentEmail = userSnapshot.child("email").getValue(String.class);

                                TableRow row = new TableRow(TutorDashboardActivity.this);
                                row.setBackgroundColor(0xFFFFFFFF);
                                row.setPadding(4, 4, 4, 4);

                                TextView usernameView = new TextView(TutorDashboardActivity.this);
                                usernameView.setText(studentUsername);
                                usernameView.setTextColor(0xFF212121);
                                usernameView.setPadding(8, 8, 8, 8);

                                TextView emailView = new TextView(TutorDashboardActivity.this);
                                emailView.setText(studentEmail);
                                emailView.setTextColor(0xFF212121);
                                emailView.setPadding(8, 8, 8, 8);

                                Button removeButton = new Button(TutorDashboardActivity.this);
                                removeButton.setText("Remove");
                                removeButton.setBackgroundTintList(ContextCompat.getColorStateList(
                                        TutorDashboardActivity.this, android.R.color.holo_red_dark));
                                removeButton.setTextColor(0xFFFFFFFF);
                                removeButton.setTextSize(12);
                                removeButton.setPadding(4, 2, 4, 2);
                                removeButton.setOnClickListener(v -> removeStudentFromPreferred(studentKey, studentUsername, row));

                                row.addView(usernameView);
                                row.addView(emailView);
                                row.addView(removeButton);
                                preferredStudentContainer.addView(row);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(TutorDashboardActivity.this, "Failed to load student email.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void removeStudentFromPreferred(String studentKey, String studentUsername, TableRow row) {
        preferredListRef.child(usernameTut).child(studentKey).removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(TutorDashboardActivity.this, "Removed " + studentUsername, Toast.LENGTH_SHORT).show();
                    preferredStudentContainer.removeView(row);
                    loadStudentList();
                })
                .addOnFailureListener(e -> Toast.makeText(TutorDashboardActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
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
     * Adds a student to a tutor's preferred list.
     * @param tutorUsername The tutor's username.
     * @param studentUsername The student's username.
     * @return A Task object to handle success/failure callbacks.
     */
    public Task<Void> addPreferredStudent(String tutorUsername, String studentUsername) {
        return preferredListRef.child(tutorUsername).push().setValue(studentUsername);
    }

    /**
     * Removes a student from a tutor's preferred list.
     * @param tutorUsername The tutor's username.
     * @param studentKey The Firebase-generated key for the preferred student entry.
     * @return A Task object to handle success/failure callbacks.
     */
    public Task<Void> removePreferredStudent(String tutorUsername, String studentKey) {
        return preferredListRef.child(tutorUsername).child(studentKey).removeValue();
    }
}