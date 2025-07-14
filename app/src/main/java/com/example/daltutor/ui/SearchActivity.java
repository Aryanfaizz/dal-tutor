package com.example.daltutor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.daltutor.R;
import com.example.daltutor.core.Posting;
import com.example.daltutor.firebase.DatabaseHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class SearchActivity extends AppCompatActivity implements OnMapReadyCallback {

    private int minFee;
    private int maxFee;
    private int minDuration;
    private int maxDuration;
    private TableLayout table;
    private GoogleMap mMap;
    private int numPostingsFound;
    private ArrayList<String> postingIDs;

    private final LatLng defaultLocation = new LatLng(44.6375, -63.5750);

    private int dpToPx (int dp) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    };

    private void addPostingToMap(Posting posting, String id) {
        String address = posting.getAddress();
        Geocoder geo = new Geocoder(SearchActivity.this);
        Address geoAddress = null;
        try {
            geoAddress = geo.getFromLocationName(address, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (geoAddress != null) {
            LatLng latLng = new LatLng(geoAddress.getLatitude(), geoAddress.getLongitude());
            Marker mapMarker = mMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(posting.getDescription()));
            assert mapMarker != null;
            mapMarker.setTag(id);
            mapMarker.setSnippet(posting.getTutor());
        }
    }

    private int parseMinText(EditText et) throws NumberFormatException {
        String text = et.getText().toString();
        if (text.equals("")) {
            return 0;
        } else {
            return Integer.parseInt(text);
        }
    }

    private int parseMaxText(EditText et) throws NumberFormatException {
        String text = et.getText().toString();
        if (text.equals("")) {
            return Integer.MAX_VALUE;
        } else {
            return Integer.parseInt(text);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.search_map);
        mapFragment.getMapAsync(SearchActivity.this);

        Spinner topicSpinner = findViewById(R.id.search_topic_spinner);
        LinkedList<String> topics = new LinkedList<String>(Arrays.asList(getResources().getStringArray(R.array.topics)));
        topics.add(0, "Any");
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, topics);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        topicSpinner.setAdapter(adapter);

        Button backButton = findViewById(R.id.search_back_button);
        backButton.setOnClickListener(v -> {
            finish();
        });

        ArrayList<String> postingIDs = DatabaseHelper.getPostingIDs();

        EditText feeMinText = findViewById(R.id.search_fee_min);
        EditText feeMaxText = findViewById(R.id.search_fee_max);
        EditText durMinText = findViewById(R.id.search_duration_min);
        EditText durMaxText = findViewById(R.id.search_duration_max);

        Button searchButton = findViewById(R.id.search_now_button);
        searchButton.setOnClickListener(v -> {
            mMap.clear();
            try {
                minFee = parseMinText(feeMinText);
                maxFee = parseMaxText(feeMaxText);
                minDuration = parseMinText(durMinText);
                maxDuration = parseMaxText(durMaxText);
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please only enter numbers in the filter fields", Toast.LENGTH_SHORT).show();
                return;
            }

            numPostingsFound = 0;
            for (String id : postingIDs) {
                DatabaseHelper.getPostingFromID(id, new DatabaseHelper.PostingCallback() {
                    @Override
                    public void onPostingFound(Posting p) {
                        if (p.matchesCriteria(minFee, maxFee, minDuration, maxDuration, topicSpinner.getSelectedItem().toString())) {
                            addPostingToMap(p, id);
                            numPostingsFound++;
                        }
                        TextView resultText = findViewById(R.id.search_results_text);
                        if (numPostingsFound == 0) {
                            resultText.setText("No postings found!");
                            resultText.setTextColor(0xFFFF0000);
                            resultText.setVisibility(View.VISIBLE);
                        } else {
                            resultText.setText(numPostingsFound + " posting(s) found.\nClick a map pin for more details.");
                            resultText.setTextColor(0xFF000000);
                            resultText.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {;
        mMap = googleMap;

        try {
            FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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

        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                Intent intent = new Intent(SearchActivity.this, SessionDetailsActivity.class);
                intent.putExtra("POSTING_ID", marker.getTag().toString());

                // TUTORIAL REGISTRATION: Get username and role from the intent that started SearchActivity
                Intent receivedIntent = getIntent();
                if (receivedIntent != null) {
                    String username = receivedIntent.getStringExtra("USERNAME");
                    String role = receivedIntent.getStringExtra("ROLE");

                    if (username != null) {
                        intent.putExtra("USERNAME", username);
                    }
                    if (role != null) {
                        intent.putExtra("ROLE", role);
                    }
                }

                startActivity(intent);
            }
        });
    }
}
