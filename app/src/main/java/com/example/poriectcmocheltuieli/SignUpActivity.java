package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupUsername, signupPassword, confirmPassword;
    private Button signUpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuppage);

        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signupButton);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = signupUsername.getText().toString();
                String password = signupPassword.getText().toString();
                String confirmPasswordText = confirmPassword.getText().toString();

                if (password.equals(confirmPasswordText)) {
                    // Salvăm username-ul și parola în SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("username", username);
                    editor.putString("password", password);
                    editor.apply();  // Folosim apply() pentru a salva rapid datele

                    // Afișăm un mesaj de succes
                    Toast.makeText(SignUpActivity.this, "User created: " + username, Toast.LENGTH_SHORT).show();

                    // Navigăm către pagina de login (MainActivity)
                    Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();  // Închidem activitatea curentă pentru a nu lăsa SignUpActivity în backstack
                } else {
                    // Dacă parolele nu se potrivesc, afișăm un mesaj de eroare
                    Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
