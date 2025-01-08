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

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
            startActivity(intent);
            return true;
        }
        if(id ==R.id.action_home)
        {
            Intent intent = new Intent(this, WelcomeActivity.class);
            startActivity(intent);
            return true;
        }
        return false;
    }
}
