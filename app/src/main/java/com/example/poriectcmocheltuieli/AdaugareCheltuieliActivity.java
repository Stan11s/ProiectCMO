package com.example.poriectcmocheltuieli;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AdaugareCheltuieliActivity extends BaseActivity {
    private EditText editTextDescriere;
    private EditText editTextSuma;
    private EditText editTextData;
    private Button btnSalvare;

    private FirebaseFirestore db;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.adaugare_cheltuieli);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Inițializare Firestore
        db = FirebaseFirestore.getInstance();

        // Obținem userId transmis prin Intent
        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            Log.e("AdaugareCheltuieli", "Eroare: userId este null!");
            Toast.makeText(this, "Eroare: Nu există userId!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Log.d("AdaugareCheltuieli", "userId: " + userId);

        // Inițializare elemente UI
        editTextDescriere = findViewById(R.id.descriptionEditText);
        editTextSuma = findViewById(R.id.amountEditText);
        btnSalvare = findViewById(R.id.addButton);

        // Listener pentru butonul de salvare
        btnSalvare.setOnClickListener(v -> salvareCheltuiala());
    }


    private void salvareCheltuiala() {
        String descriere = editTextDescriere.getText().toString().trim();
        String sumaString = editTextSuma.getText().toString().trim();
        Date data = new Date();

        // Validare date
        if (TextUtils.isEmpty(descriere)) {
            editTextDescriere.setError("Introdu descrierea!");
            return;
        }

        if (TextUtils.isEmpty(sumaString)) {
            editTextSuma.setError("Introdu suma!");
            return;
        }

        double suma;
        try {
            suma = Double.parseDouble(sumaString);
        } catch (NumberFormatException e) {
            editTextSuma.setError("Suma trebuie să fie un număr valid!");
            return;
        }

        // Creare un obiect pentru salvare în Firestore
        Map<String, Object> cheltuiala = new HashMap<>();
        cheltuiala.put("description", descriere);
        cheltuiala.put("amount", suma);
        cheltuiala.put("date", data);

        // Salvare cheltuiala în Firestore
        db.collection("Users")
                .document(userId)
                .collection("Cumparaturi")
                .add(cheltuiala)
                .addOnSuccessListener(documentReference -> {
                    Log.d("AdaugareCheltuieli", "Cheltuială salvată cu succes: " + documentReference.getId());

                    Toast.makeText(this, "Cheltuială adăugată cu succes!", Toast.LENGTH_SHORT).show();

                    editTextDescriere.setText("");
                    editTextSuma.setText("");
                })
                .addOnFailureListener(e -> {
                    Log.e("AdaugareCheltuieli", "Eroare la salvare: " + e.getMessage(), e);
                    Toast.makeText(this, "Eroare la salvare: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}
