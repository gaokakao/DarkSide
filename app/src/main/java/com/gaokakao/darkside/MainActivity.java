// MainActivity.java
package com.gaokakao.darkside;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private Button submitButton;
    private TextView usernameTextView;
    private SharedPreferences sharedPreferences;
    private static final String PREFS_NAME = "user_prefs";
    private static final String USERNAME_KEY = "username";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameEditText = findViewById(R.id.usernameEditText);
        submitButton = findViewById(R.id.submitButton);
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
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString();
                if (!username.isEmpty()) {
                    sharedPreferences.edit().putString(USERNAME_KEY, username).apply();
                    displayUsername(username);
                }
            }
        });
    }

    private void displayUsername(String username) {
        usernameEditText.setVisibility(View.GONE);
        submitButton.setVisibility(View.GONE);
        usernameTextView.setText(username);
        usernameTextView.setVisibility(View.VISIBLE);
    }
}
