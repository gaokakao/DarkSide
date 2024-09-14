package com.gaokakao.darkside;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView latitudeText;
    private TextView longitudeText;
    private TextView usernameTextView;
    private TextView resultTextView;
    private final int LOCATION_REQUEST_CODE = 10001;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latitudeText = findViewById(R.id.latitude_text);
        longitudeText = findViewById(R.id.longitude_text);
        usernameTextView = findViewById(R.id.username_text_view);
        resultTextView = findViewById(R.id.result_text_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        sharedPreferences = getSharedPreferences("DarksidePrefs", MODE_PRIVATE);

        // Set username text to bold
        usernameTextView.setTypeface(null, Typeface.BOLD);

        checkForUsername();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        } else {
            startLocationUpdates();
        }

        usernameTextView.setOnClickListener(v -> showUsernameDialog());
    }

    private void checkForUsername() {
        String username = sharedPreferences.getString("username", null);
        if (username == null) {
            showUsernameDialog();
        } else {
            usernameTextView.setText(username);
        }
    }

    private void showUsernameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Username");
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_username, null);
        builder.setView(customLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            TextView usernameInput = customLayout.findViewById(R.id.username_input);
            String newUsername = usernameInput.getText().toString().trim();
            if (!newUsername.isEmpty()) {
                usernameTextView.setText(newUsername);
                sharedPreferences.edit().putString("username", newUsername).apply();
            } else {
                Toast.makeText(this, "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(300);  // Set update interval to 300 milliseconds
        locationRequest.setFastestInterval(300);  // Fastest update interval to 300 milliseconds

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    double lat = location.getLatitude();
                    double lon = location.getLongitude();
                    latitudeText.setText("Latitude: " + lat);
                    longitudeText.setText("Longitude: " + lon);
                    sendLocationToServer(lat, lon);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocationToServer(double latitude, double longitude) {
        String username = sharedPreferences.getString("username", "unknown");
        new Thread(() -> {
            try {
                String urlString = "http://gao.lt/index.php?latitude=" + URLEncoder.encode(String.valueOf(latitude), "UTF-8")
                        + "&longitude=" + URLEncoder.encode(String.valueOf(longitude), "UTF-8")
                        + "&user=" + URLEncoder.encode(username, "UTF-8");

                longitudeText.setText(urlString);

                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");

                int responseCode = conn.getResponseCode();
                InputStream inputStream = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                conn.disconnect();

                String responseBody = response.toString();
                runOnUiThread(() -> {
                    if (responseCode == 200 && responseBody.contains("ok")) {
                        usernameTextView.setTextColor(Color.GREEN);
                        resultTextView.setText("Request succeeded: " + responseBody);
                    } else {
                        usernameTextView.setTextColor(Color.RED);
                        resultTextView.setText("Request failed: " + responseBody);
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    usernameTextView.setTextColor(Color.RED);
                    resultTextView.setText("Request failed: " + e.getMessage());
                });
            }
        }).start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission denied. Please grant location access in settings.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            }
        }
    }
}
