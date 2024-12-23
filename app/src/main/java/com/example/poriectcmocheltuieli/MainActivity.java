package com.example.poriectcmocheltuieli;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

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
import com.google.android.gms.tasks.Task;

public class MainActivity extends AppCompatActivity {
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;
    private EditText usernameField, passwordField;
    private Button loginButton;
    private TextView statusMessage;
    private Button signUpButton;
    private GoogleSignInClient googleSignInClient;

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


        //PENTRU GOOGLE AUTENTIFICARE
        oneTapClient = Identity.getSignInClient(this);
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

                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                String savedUsername = sharedPreferences.getString("username", null);
                String savedPassword = sharedPreferences.getString("password", null);

                if (username.equals(savedUsername) && password.equals(savedPassword)) {
                    statusMessage.setText("Login Successful");
                    statusMessage.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

                    // Salvăm starea autentificării
                    sharedPreferences.edit().putBoolean("isLoggedIn", true).apply();

                    navigateToWelcomePage(username);
                } else {
                    statusMessage.setText("Invalid Credentials");
                    statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                }
            }
        });

        // Verificăm dacă utilizatorul este deja autentificat
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
        String username = sharedPreferences.getString("username", "");

        if (isLoggedIn) {
            navigateToWelcomePage(username);
            return;
        }
    }

    private void navigateToWelcomePage(String username) {
        Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
        finish(); // Opțional: pentru a preveni revenirea la ecranul de login
    }

    public void buttonGoogleSignIn(View view) {
        // Șterge datele din preferințe
        SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();

        googleSignInClient.signOut().addOnCompleteListener(this, task -> {
            googleSignInClient.revokeAccess().addOnCompleteListener(this, revokeTask -> {
                Intent signInIntent = googleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, REQ_ONE_TAP);
            });
        });
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
                Log.e(TAG, "Google Sign-In failed.", e);
                statusMessage.setText("Google Sign-In failed.");
                statusMessage.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            }
        }
    }
}
