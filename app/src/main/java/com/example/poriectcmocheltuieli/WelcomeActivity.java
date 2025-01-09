package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {
    private TextView welcomeMessage;
    private Button logoutButton;
    private Button addDataButton;
    private Button fetchDataButton;
    private TextView displayDataText;


    // Firebase instance
    private FirebaseFirestore db;

    // GoogleSignInClient
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        // Inițializare Firestore
        db = FirebaseFirestore.getInstance();

        welcomeMessage = findViewById(R.id.welcomeMessage);
        logoutButton = findViewById(R.id.logoutButton);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurarea Google SignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        String userId = null;
        String username = null;

        // Verificare daca utilizatorul este conectat cu Firebase Authentication
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // UID Firebase
            username = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Email-ul utilizatorului
        } else {
            // Verificare daca utilizatorul este conectat cu Google Sign-In
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                userId = account.getId(); // ID Google
                username = account.getDisplayName(); // Nume utilizator
            }
        }

        // Daca un utilizator este autentificat
        if (userId != null) {
            welcomeMessage.setText("Salut, " + username);
        }

        // Logout
        logoutButton.setOnClickListener(v -> {
            // stergem starea autentificarii din SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
            sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();

            // Deconectare utilizator Firebase Authentication
            FirebaseAuth.getInstance().signOut();

            // Deconectare utilizator Google Sign-In
            mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_expenses) {
            // Verificam dacă utilizatorul este conectat
            String userId = null;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    userId = account.getId();
                }
            }

            if (userId != null) {
                // Transmitem userId catre CalendarActivity
                Intent intent = new Intent(this, CalendarActivity.class);
                intent.putExtra("userId", userId); // Transmitem userId ca extra
                startActivity(intent);
            } else {
                Toast.makeText(this, "Eroare: Nu s-a găsit utilizatorul autentificat!", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        if(id == R.id.action_add_expense)
        {
            // Verificam dacă utilizatorul este conectat
            String userId = null;
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
            } else {
                GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
                if (account != null) {
                    userId = account.getId();
                }
            }

            if (userId != null) {
                // Transmitem userId catre CalendarActivity
                Intent intent = new Intent(this, AdaugareCheltuieliActivity.class);
                intent.putExtra("userId", userId); // Transmitem userId ca extra
                startActivity(intent);
            } else {
                Toast.makeText(this, "Eroare: Nu s-a găsit utilizatorul autentificat!", Toast.LENGTH_SHORT).show();
            }
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
