package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.Identity;
import com.google.android.gms.auth.api.identity.SignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private EditText usernameField, passwordField;
    private Button loginButton;
    private TextView statusMessage;
    private Button signUpButton;
    private GoogleSignInClient googleSignInClient;

    private FirebaseAuth firebaseAuth; // Adăugare Firebase Auth

    private static final int REQ_ONE_TAP = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        usernameField = findViewById(R.id.username);
        passwordField = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        statusMessage = findViewById(R.id.statusMessage);
        signUpButton = findViewById(R.id.signUpButton);

        // Inițializare Firebase Auth
        firebaseAuth = FirebaseAuth.getInstance();

        // Configurarea GoogleSignInOptions
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("802966616072-27b4corqpdc30j5amqv4og32mmdoe5ht.apps.googleusercontent.com") // ID-ul Clientului OAuth 2.0
                .requestEmail()
                .build();

        // Inițializează variabila globală googleSignInClient
        googleSignInClient = GoogleSignIn.getClient(this, gso);

        signInRequest = BeginSignInRequest.builder()
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
            if (email.equals("test") && password.equals("test")) {
                statusMessage.setText("Login Successful (Test Mode)");
                statusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                // Salvăm starea autentificării
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sharedPreferences.edit()
                        .putBoolean("isLoggedIn", true)
                        .putString("username", "test@test.com") // Simulăm un email pentru testare
                        .apply();

                navigateToWelcomePage("test@test.com"); // Navigare către WelcomeActivity
                return; // Ne asigurăm că Firebase Auth nu este apelat
            }
            if (email.isEmpty() || password.isEmpty()) {
                statusMessage.setText("Please fill in all fields");
                statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                return;
            }

            // Firebase Auth sign-in
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null) {
                                statusMessage.setText("Login Successful");
                                statusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                                // Salvăm starea autentificării
                                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                sharedPreferences.edit()
                                        .putBoolean("isLoggedIn", true)
                                        .putString("username", user.getEmail())
                                        .apply();

                                navigateToWelcomePage(user.getEmail());
                            }
                        } else {
                            statusMessage.setText("Invalid Credentials");
                            statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                        }
                    });
        });

        // Verificăm dacă utilizatorul este deja autentificat
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser != null) {
            navigateToWelcomePage(currentUser.getEmail());
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

                    // Salvăm starea autentificării
                    SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    sharedPreferences.edit()
                            .putBoolean("isLoggedIn", true)
                            .putString("username", email)
                            .apply();

                    navigateToWelcomePage(email);
                }
            } catch (ApiException e) {
                Log.e("TAG", "Google Sign-In failed.", e);
                statusMessage.setText("Google Sign-In failed.");
                statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}
