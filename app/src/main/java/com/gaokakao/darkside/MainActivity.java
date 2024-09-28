package com.gaokakao.darkside;
import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
public class MainActivity extends AppCompatActivity {
    private LinearLayout usernameBar;
    private TextView usernameTextView;
    private TextView usersListTextView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String username = "";
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitude = 0;
    private double longitude = 0;
    private final Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            handler.postDelayed(this, 250);
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameBar = findViewById(R.id.user);
        usernameTextView = findViewById(R.id.username_text);
        usersListTextView = findViewById(R.id.users_list_text);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                sendLocationToServer();
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {}
        };

        String savedUsername = getSharedPreferences("appPrefs", MODE_PRIVATE).getString("username", "");
        if (savedUsername.isEmpty()) {
            promptForUsername();
        } else {
            username = savedUsername;
            usernameTextView.setText(username.toUpperCase());
            usernameTextView.setTextColor(Color.WHITE);
            usernameBar.setBackgroundColor(Color.GREEN);
            handler.post(updateLocationRunnable);
        }
        usernameBar.setOnClickListener(v -> promptForUsername());
    }
    private void promptForUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Username");
        final EditText input = new EditText(this);
        input.setPadding(20, 20, 20, 20);
        builder.setView(input);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = input.getText().toString();
                if (!username.isEmpty()) {
                    getSharedPreferences("appPrefs", MODE_PRIVATE).edit().putString("username", username).apply();
                    usernameTextView.setText(username.toUpperCase());
                    usernameTextView.setTextColor(Color.WHITE);
                    usernameBar.setBackgroundColor(Color.GREEN);
                    handler.post(updateLocationRunnable);
                } else {
                    finish();
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            input.post(() -> {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        dialog.show();
    }
    private void sendLocationToServer() {
        new Thread(() -> {
            try {
                URL url = new URL("https://gao.lt/gps.php" + "?latitude=" + latitude + "&longitude=" + longitude + "&user=" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONArray users = new JSONArray(readStream(conn.getInputStream()));
                    handler.post(() -> {
                        usernameBar.setBackgroundColor(Color.GREEN);
                        displayUsers(users);
                    });
                } else {
                    handler.post(() -> usernameBar.setBackgroundColor(Color.RED));
                }
                conn.disconnect();
            } catch (Exception e) {
                handler.post(() -> usernameBar.setBackgroundColor(Color.RED));
            }
        }).start();
    }
    private String readStream(InputStream inputStream) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }
    private void displayUsers(JSONArray users) {
        StringBuilder usersList = new StringBuilder();
        try {
            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String userName = user.getString("user");
                if (!userName.equals(username)) {
                    double distance = user.getDouble("distance");
                    usersList.append(String.format("%s: %.2f metrai\n", userName, distance));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        usersListTextView.setText(usersList.toString());
        usersListTextView.setTextColor(Color.GREEN);
    }
}