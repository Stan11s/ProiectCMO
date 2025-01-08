package com.example.poriectcmocheltuieli;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.firebase.auth.FirebaseAuth;

public abstract class BaseActivity extends AppCompatActivity {
    protected String userId;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        } else {
            GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
            if (account != null) {
                userId = account.getId();
            } else {
                userId = null;
            }

        }
    }

    // Configurarea meniului
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("BaseActivity", "onCreateOptionsMenu called in BaseActivity");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_toolbar, menu);
        return true;
    }


    // Gestionarea evenimentelor din meniu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_expenses) {
            // Setăm layout-ul la calendar.xml
            Intent intent = new Intent(this, CalendarActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;
        }
        if(id ==R.id.action_home)
        {
            Intent intent = new Intent(this, WelcomeActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;
        }
        if(id ==R.id.action_add_expense)
        {
            Intent intent = new Intent(this, AdaugareCheltuieliActivity.class);
            intent.putExtra("userId", userId);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
