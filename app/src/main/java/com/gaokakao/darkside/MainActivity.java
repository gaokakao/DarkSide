// MainActivity.java
package com.gaokakao.darkside;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;  // Import View class
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView usernameTextView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "user_prefs";
    private static final String USERNAME_KEY = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameTextView = findViewById(R.id.usernameTextView);
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString(USERNAME_KEY, null);
        if (savedUsername == null) {
            promptForUsername();
        } else {
            displayUsername(savedUsername);
        }
    }

    private void promptForUsername() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter Username");
        final EditText input = new EditText(this);
        input.setHint("Username");
        input.setTextColor(getResources().getColor(android.R.color.holo_blue_light));
        input.setHintTextColor(getResources().getColor(android.R.color.holo_orange_light));
        builder.setView(input);
        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String username = input.getText().toString();
                if (!username.isEmpty()) {
                    sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
                    displayUsername(username);
                }
            }
        });
        builder.setCancelable(false);
        builder.show();
    }

    private void displayUsername(String username) {
        usernameTextView.setText(username);
        usernameTextView.setVisibility(View.VISIBLE);
    }
}
