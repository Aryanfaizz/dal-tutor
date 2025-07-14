package com.example.daltutor.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.daltutor.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private final LatLng defaultLocation = new LatLng(44.6375, -63.5750);


    private void updateLocation() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_location_picker);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        updateLocation();

        Button select_loc_button = findViewById(R.id.select_location_button);
        select_loc_button.setEnabled(false);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Button select_loc_button = findViewById(R.id.select_location_button);
        final LatLng[] selected_latlng = new LatLng[1];
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(latLng);
                try {
                    markerOptions.title(getAddressFromLatLng(latLng));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                selected_latlng[0] = latLng;

                googleMap.clear();
                googleMap.addMarker(markerOptions);
                select_loc_button.setEnabled(true);
            }
        });

        select_loc_button.setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            try {
                resultIntent.putExtra("address", getAddressFromLatLng(selected_latlng[0]));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private String getAddressFromLatLng(@NonNull LatLng latlng) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());
        addresses = geocoder.getFromLocation(latlng.latitude, latlng.longitude, 1);

        String address = addresses.get(0).getAddressLine(0);
        return address;
    }
}
