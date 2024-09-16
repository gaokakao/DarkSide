package com.gaokakao.darkside;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
public class MainActivity extends AppCompatActivity {
    private TextView usernameText;
    private String username = "USERNAME"; // Default username
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usernameText = findViewById(R.id.username_text);
        LinearLayout topBar = findViewById(R.id.user);
        // Set the initial username
        usernameText.setText(username);
        // Click listener for changing the username
        topBar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showUsernameChangeDialog();
            }
        });
    }
    private void showUsernameChangeDialog() {
        final EditText input = new EditText(this);
        input.setText(username);
        new AlertDialog.Builder(this)
                .setTitle("Change Username")
                .setMessage("Enter new username:")
                .setView(input)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        username = input.getText().toString().trim();
                        usernameText.setText(username);
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}