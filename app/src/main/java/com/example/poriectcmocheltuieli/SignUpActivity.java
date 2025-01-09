package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SignUpActivity extends AppCompatActivity {

    private EditText signupUsername, signupPassword, confirmPassword;
    private Button signUpButton;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuppage);

        signupUsername = findViewById(R.id.signup_username);
        signupPassword = findViewById(R.id.signup_password);
        confirmPassword = findViewById(R.id.confirm_password);
        signUpButton = findViewById(R.id.signupButton);

        // Initializam FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(v -> {
            String username = signupUsername.getText().toString().trim();
            String password = signupPassword.getText().toString().trim();
            String confirmPasswordText = confirmPassword.getText().toString().trim();

            // Verific daca toate campurile sunt completate
            if (username.isEmpty()) {
                signupUsername.setError("Username is required!");
                signupUsername.requestFocus();
                return;
            }

            if (password.isEmpty()) {
                signupPassword.setError("Password is required!");
                signupPassword.requestFocus();
                return;
            }

            if (confirmPasswordText.isEmpty()) {
                confirmPassword.setError("Confirm Password is required!");
                confirmPassword.requestFocus();
                return;
            }

            // Verific daca parolele se potrivesc
            if (!password.equals(confirmPasswordText)) {
                Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Înregistrare utilizator în Firebase Authentication
            mAuth.createUserWithEmailAndPassword(username, password)
                    .addOnCompleteListener(SignUpActivity.this, task -> {
                        if (task.isSuccessful()) {
                            // Înregistrare reusita, navigam la LoginActivity
                            Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();  // Închidem activitatea de înregistrare
                        } else {
                            // Eroare la înregistrare
                            Toast.makeText(SignUpActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}
