package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private Button loginButton;
    private TextView statusMessage;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        statusMessage = findViewById(R.id.statusMessage);
        signUpButton = findViewById(R.id.signUpButton);

        // Navigare către SignUpActivity
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });

        // Buton de login
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameField.getText().toString();
                String password = passwordField.getText().toString();

                // Obținem datele salvate în SharedPreferences
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("username", null);
                String savedPassword = sharedPreferences.getString("password", null);

                // Verificăm dacă datele introduse corespund celor salvate
                if (username.equals(savedUsername) && password.equals(savedPassword)) {
                    statusMessage.setText("Login Successful");
                    statusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                } else {
                    statusMessage.setText("Invalid Credentials");
                    statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });
    }
}
