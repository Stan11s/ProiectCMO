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

        // Inițializăm FirebaseAuth
        mAuth = FirebaseAuth.getInstance();

        signUpButton.setOnClickListener(v -> {
            String username = signupUsername.getText().toString();
            String password = signupPassword.getText().toString();
            String confirmPasswordText = confirmPassword.getText().toString();

            if (password.equals(confirmPasswordText)) {
                // Înregistrare utilizator în Firebase Authentication
                mAuth.createUserWithEmailAndPassword(username, password)
                        .addOnCompleteListener(SignUpActivity.this, task -> {
                            if (task.isSuccessful()) {
                                // Înregistrare reușită, navigăm la LoginActivity
                                Toast.makeText(SignUpActivity.this, "User created successfully", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();  // Închide activitatea de înregistrare
                            } else {
                                // Eroare la înregistrare
                                Toast.makeText(SignUpActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                            }
                        });
            } else {
                Toast.makeText(SignUpActivity.this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
