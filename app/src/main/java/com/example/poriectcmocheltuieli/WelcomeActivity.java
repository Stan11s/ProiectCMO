package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class WelcomeActivity extends AppCompatActivity {
    private TextView welcomeMessage;
    private Button logoutButton;
    private Button addDataButton;
    private Button fetchDataButton;
    private TextView displayDataText; // TextView pentru a afisa datele

    // Firestore instance
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        welcomeMessage = findViewById(R.id.welcomeMessage);
        logoutButton = findViewById(R.id.logoutButton);
        addDataButton = findViewById(R.id.addDataButton);
        fetchDataButton = findViewById(R.id.fetchDataButton);
        displayDataText = findViewById(R.id.displayDataText);

        // Inițializează Firestore
        db = FirebaseFirestore.getInstance();

        // Obține numele utilizatorului transmis prin Intent
        Intent intent = getIntent();
        String username = intent.getStringExtra("username");

        welcomeMessage.setText("Salut, " + username + "!");

        // Logout
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Ștergem starea autentificării
                SharedPreferences sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                sharedPreferences.edit().putBoolean("isLoggedIn", false).apply();

                // Redirecționăm utilizatorul la ecranul de login
                Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Adaugă date în Firestore la apăsarea butonului
        addDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addDataToFirestore();
            }
        });

        // Preia date din Firestore la apăsarea butonului
        fetchDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchDataFromFirestore();
            }
        });
    }

    // Metoda pentru a adăuga date în Firestore
    private void addDataToFirestore() {
        // Creează un HashMap cu datele pe care vrei să le adaugi
        Map<String, Object> cumparaturi = new HashMap<>();
        cumparaturi.put("produs", "Banane");
        cumparaturi.put("cantitate", 5);
        cumparaturi.put("pret", 3.50);

        // Adaugă datele în colecția "Cumparaturi"
        db.collection("Cumparaturi")
                .add(cumparaturi)
                .addOnSuccessListener(documentReference -> {
                    Log.d("Firestore", "Document adăugat cu ID-ul: " + documentReference.getId());
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Eroare la adăugare: " + e.getMessage());
                });
    }

    // Metoda pentru a prelua datele din Firestore și a le afisa
    private void fetchDataFromFirestore() {
        db.collection("Cumparaturi")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        StringBuilder displayData = new StringBuilder();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String produs = document.getString("produs");
                            Long cantitate = document.getLong("cantitate");
                            Double pret = document.getDouble("pret");

                            // Adaugă datele într-un StringBuilder pentru a le afisa
                            displayData.append("Produs: ").append(produs)
                                    .append(", Cantitate: ").append(cantitate)
                                    .append(", Pret: ").append(pret)
                                    .append("\n");
                        }
                        // Afișează datele în TextView
                        displayDataText.setText(displayData.toString());
                    } else {
                        Log.e("Firestore", "Eroare la preluare: " + task.getException().getMessage());
                    }
                });
    }
}
