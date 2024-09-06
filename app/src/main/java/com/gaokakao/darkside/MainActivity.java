package com.gaokakao.darkside;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private TextView latitudeText;
    private TextView longitudeText;
    private TextView resultTextView;
    private TextView ipTextView;
    private final int LOCATION_REQUEST_CODE = 10001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        latitudeText = findViewById(R.id.latitude_text);
        longitudeText = findViewById(R.id.longitude_text);
        resultTextView = findViewById(R.id.result_text_view);
        ipTextView = findViewById(R.id.ip_text_view);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    private void startLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(500); // Update interval set to 500 milliseconds
        locationRequest.setFastestInterval(500); // Fastest interval set to 500 milliseconds
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    String lat = String.valueOf(location.getLatitude());
                    String lon = String.valueOf(location.getLongitude());
                    latitudeText.setText("Lat: " + lat);
                    longitudeText.setText("Lon: " + lon);
                    String ip = getIpAddress();
                    ipTextView.setText("IP: " + ip);
                    sendLocationToServer(lat, lon, ip);
                }
            }
        };
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null);
    }

    private void sendLocationToServer(String latitude, String longitude, String ip) {
        new Thread(() -> {
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL("http://gao.lt/index.php?latitude=" + latitude + "&longitude=" + longitude + "&ip=" + ip);
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                int responseCode = urlConnection.getResponseCode();
                String responseMessage = urlConnection.getResponseMessage();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                runOnUiThread(() -> {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        resultTextView.setText("Success: " + response.toString());
                        resultTextView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_green_light));
                    } else {
                        resultTextView.setText("Failed: " + responseMessage);
                        resultTextView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    resultTextView.setText("Exception: " + e.getMessage());
                    resultTextView.setBackgroundColor(ContextCompat.getColor(MainActivity.this, android.R.color.holo_red_light));
                });
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }
        }).start();
    }

    private String getIpAddress() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            Network activeNetwork = cm.getActiveNetwork();
            if (activeNetwork != null) {
                NetworkCapabilities capabilities = cm.getNetworkCapabilities(activeNetwork);
                if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    int ipAddress = wifiInfo.getIpAddress();
                    return String.format(Locale.getDefault(), "%d.%d.%d.%d",
                            (ipAddress & 0xFF),
                            (ipAddress >> 8 & 0xFF),
                            (ipAddress >> 16 & 0xFF),
                            (ipAddress >> 24 & 0xFF));
                } else if (capabilities != null && capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                        for (InetAddress inetAddress : Collections.list(networkInterface.getInetAddresses())) {
                            if (!inetAddress.isLoopbackAddress() && inetAddress.isSiteLocalAddress()) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "unknown";
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
