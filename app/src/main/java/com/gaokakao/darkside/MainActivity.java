package com.gaokakao.darkside;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends AppCompatActivity {
    private static final String SERVER_URL = "https://gao.lt/index.php";
    private LinearLayout usernameBar;
    private TextView usernameTextView;
    private TextView usersListTextView;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private String username = "";

    private final Runnable updateLocationRunnable = new Runnable() {
        @Override
        public void run() {
            double latitude = 54.7619029; // Example values
            double longitude = 25.2868659; // Example values
            sendLocationToServer(latitude, longitude);
            handler.postDelayed(this, 300);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameBar = findViewById(R.id.user);
        usernameTextView = findViewById(R.id.username_text);
        usersListTextView = findViewById(R.id.users_list_text);

        promptForUsername();
    }

    private void promptForUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Username");

        // Create EditText for user input
        final EditText input = new EditText(this);
        input.setPadding(20, 20, 20, 20);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                username = input.getText().toString();
                if (!username.isEmpty()) {
                    usernameTextView.setText(username.toUpperCase());
                    usernameTextView.setTextColor(Color.WHITE);
                    usernameBar.setBackgroundColor(Color.GREEN); // Initial color
                    handler.post(updateLocationRunnable);
                } else {
                    finish(); // Close the app if username is empty
                }
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish(); // Close app if user cancels
            }
        });

        final AlertDialog dialog = builder.create();
        dialog.setOnShowListener(d -> {
            // Use ViewTreeObserver to show keyboard after layout is drawn
            input.post(() -> {
                input.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
            });
        });
        dialog.show();
    }

    private void sendLocationToServer(double latitude, double longitude) {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL + "?latitude=" + latitude + "&longitude=" + longitude + "&user=" + username);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    handler.post(() -> {
                        usernameBar.setBackgroundColor(Color.GREEN);
                        fetchUsers();
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

    private void fetchUsers() {
        new Thread(() -> {
            try {
                URL url = new URL(SERVER_URL + "?latitude=0&longitude=0&user=" + username); // Adjust as needed
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    JSONArray users = new JSONArray(readStream(conn.getInputStream()));
                    handler.post(() -> displayUsers(users));
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
                double distance = user.getDouble("distance"); // Distance in meters
                double distanceInKm = distance / 1000; // Convert to kilometers
                usersList.append(String.format("%s: %.2f km\n", userName, distanceInKm));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        usersListTextView.setText(usersList.toString());
        usersListTextView.setTextColor(Color.GREEN); // Set text color
    }
}
