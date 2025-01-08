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

        // Inițializează Firestore
        db = FirebaseFirestore.getInstance();

        welcomeMessage = findViewById(R.id.welcomeMessage);
        logoutButton = findViewById(R.id.logoutButton);
        addDataButton = findViewById(R.id.addDataButton);
        fetchDataButton = findViewById(R.id.fetchDataButton);
        displayDataText = findViewById(R.id.displayDataText);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Configurarea Google SignIn
        mGoogleSignInClient = GoogleSignIn.getClient(this, GoogleSignInOptions.DEFAULT_SIGN_IN);

        String userId = null;
        String username = null;

        // Verificăm dacă utilizatorul este conectat cu Firebase Authentication
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // UID Firebase
            username = FirebaseAuth.getInstance().getCurrentUser().getEmail(); // Email-ul utilizatorului
        } else {
            // Verificăm dacă utilizatorul este conectat cu Google Sign-In
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                userId = account.getId(); // ID Google
                username = account.getDisplayName(); // Nume utilizator
            }
        }

        // Dacă un utilizator este autentificat
        if (userId != null) {
            welcomeMessage.setText("Salut, " + username);

            // Adaugă date în Firestore
            String finalUserId = userId; // Trebuie să fie `final` pentru a putea fi folosit în lambda
            addDataButton.setOnClickListener(v -> addDataToFirestore(finalUserId));

            // Preia date din Firestore
            fetchDataButton.setOnClickListener(v -> fetchDataFromFirestore(finalUserId));
        }

        // Logout
        logoutButton.setOnClickListener(v -> {
            // Ștergem starea autentificării din SharedPreferences
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


    // Metoda pentru a adăuga date în Firestore pentru utilizatorul curent
    private void addDataToFirestore(String userId) {
        // Creează un HashMap cu datele pe care vrei să le adaugi
        Map<String, Object> cumparaturi = new HashMap<>();
        cumparaturi.put("produs", "Banane");
        cumparaturi.put("cantitate", 5);
        cumparaturi.put("pret", 3.50);
        cumparaturi.put("data", com.google.firebase.Timestamp.now());

        // Adaugă datele în colecția "Users" -> ID-ul utilizatorului -> "Cumparaturi"
        db.collection("Users")
                .document(userId)  // Creăm un document pentru fiecare utilizator cu ID-ul lor
                .collection("Cumparaturi")
                .add(cumparaturi)
                .addOnSuccessListener(documentReference -> Log.d("Firestore", "Document adăugat cu ID-ul: " + documentReference.getId()))
                .addOnFailureListener(e -> Log.e("Firestore", "Eroare la adăugare: " + e.getMessage()));
    }

    // Metoda pentru a prelua datele din Firestore pentru utilizatorul curent
    private void fetchDataFromFirestore(String userId) {
        db.collection("Users")
                .document(userId)  // Accesăm documentul corespunzător utilizatorului
                .collection("Cumparaturi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder displayData = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String produs = document.getString("produs");
                            Long cantitate = document.getLong("cantitate");
                            Double pret = document.getDouble("pret");
                            Date data =document.getDate(("data"));

                            String dataFormatata = "";
                            if (data != null) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                                dataFormatata = sdf.format(data);
                            }
                            // Adaugă datele într-un StringBuilder pentru a le afisa
                            displayData.append("Produs: ").append(produs)
                                    .append(", Cantitate: ").append(cantitate)
                                    .append(", Pret: ").append(pret)
                                    .append(", Data: ").append(dataFormatata)
                                    .append("\n");
                        }
                        // Afișează datele în TextView
                        displayDataText.setText(displayData.toString());
                    } else {
                        Log.e("Firestore", "Eroare la preluare: " + task.getException().getMessage());
                    }
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
            // Setăm layout-ul la calendar.xml
            Intent intent = new Intent(this, CalendarActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
