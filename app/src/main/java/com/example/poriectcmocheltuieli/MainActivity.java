package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private EditText usernameField, passwordField;
    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth; // Adăugare Firebase Auth

    private static final int REQ_ONE_TAP = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        Button loginButton = findViewById(R.id.loginButton);
        Button signUpButton = findViewById(R.id.signUpButton);

        // Inițializare Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configurarea GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("802966616072-27b4corqpdc30j5amqv4og32mmdoe5ht.apps.googleusercontent.com") // ID-ul Clientului OAuth 2.0
                .requestEmail()
                .build();

        // Inițializare variabila globala googleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        // Permite toate conturile
        BeginSignInRequest signInRequest = BeginSignInRequest.builder()
                .setPasswordRequestOptions(BeginSignInRequest.PasswordRequestOptions.builder()
                        .setSupported(true)
                        .build())
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId("802966616072-8t320q29gsi7t69ct3cm60d4lvijos74.apps.googleusercontent.com")
                        .setFilterByAuthorizedAccounts(false) // Permite toate conturile
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        // Navigare către SignUpActivity
        signUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignUpActivity.class);
            startActivity(intent);
        });

        // Buton de login cu Firebase Authentication
        loginButton.setOnClickListener(v -> {
            String email = usernameField.getText().toString();
            String password = passwordField.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            // Test Mode pentru autentificare rapida
            if (email.equals("test") && password.equals("test")) {
                Toast.makeText(this, "Login Successful (Test Mode)", Toast.LENGTH_SHORT).show();

                // Salvare starea autentificării
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sharedPreferences.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("username", "test@test.com")
                        .apply();

                navigateToWelcomePage("test@test.com");
                return;
            }

            // Firebase Auth sign-in
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();

                                // Salvăm starea autentificării
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                sharedPreferences.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("username", user.getEmail())
                                        .apply();

                                navigateToWelcomePage(user.getEmail());
                            }
                        } else {
                            Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Verificare daca utilizatorul este deja autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToWelcomePage(currentUser.getEmail());
        }
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            navigateToWelcomePage(account.getEmail());
        }
    }

    private void navigateToWelcomePage(String username) {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish(); // Opțional: pentru a preveni revenirea la ecranul de login
    }

    public void buttonGoogleSignIn(View view) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, REQ_ONE_TAP);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_ONE_TAP) {
            try {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                GoogleSignInAccount account = task.getResult(ApiException.class);

                if (account != null) {
                    String email = account.getEmail();
                    Toast.makeText(this, "Google Sign-In successful: " + email, Toast.LENGTH_SHORT).show();

                    // Salvare starea autentificarii
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    sharedPreferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("username", email)
                            .apply();

                    navigateToWelcomePage(email);
                }
            } catch (ApiException e) {
                Log.e("TAG", "Google Sign-In failed.", e);
                Toast.makeText(this, "Google Sign-In failed.", Toast.LENGTH_SHORT).show();
            }
        }
}
}
