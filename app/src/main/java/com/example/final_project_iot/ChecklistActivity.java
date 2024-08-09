package com.example.final_project_iot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ChecklistActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private SharedPreferences sp;
    private ListView checklistView;
    private ArrayAdapter<String> adapter;
    private final String[] checklist_Labels = {"Valid passports and other documents", "Travel insurance", "Phone plan/e-Sim",
                                         "Check in to the flight", "Medicine", "Chargers", "Power adapters", "Earphones",
                                         "Shirts", "Pants", "Underwear", "Socks", "Bathing suit", "Shoes + flip flops",
                                         "Toothbrush + Toothpaste", "Shampoo + conditioner", "Body soap", "Deodorant", "Hygiene products"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_checklist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Set 'checklist' to be checked on the menu when activity starts
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.getMenu().findItem(R.id.menu_checklist).setChecked(true);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, checklist_Labels);
        checklistView = findViewById(R.id.checklistView);
        checklistView.setAdapter(adapter);

        sp = getSharedPreferences("Checklist_sp", 0);
        SharedPreferences.Editor editor = sp.edit();
        // If shared preference 'first' is non existent or true, populate sp with all false and put 'first' = false.
        // If shared preferences 'first' is false, then this is not the first time visiting this activity.
        // If that's the case, then we want the checklist to return to the last state instead of being blank (nothing checked).
        if (sp.getBoolean("first", true)){
            editor.putBoolean("first", false);
            for (int i = 0; i < checklist_Labels.length; i++){ // Populate sp with all false
                editor.putBoolean("checklist_item_"+i, false);
            }
            editor.apply();
        }
        else {
            for (int i = 0; i < checklist_Labels.length; i++){ // Populate list from shared preferences
                checklistView.setItemChecked(i, sp.getBoolean("checklist_item_"+i, false));
            }
        }

        checklistView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // On click save change to shared preferences
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                editor.putBoolean("checklist_item_"+id, checklistView.isItemChecked((int)id));
                editor.apply();
            }
        });

        // add event listener to navigation bar
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    Intent intent = new Intent(ChecklistActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_flights) {
                    Intent intent = new Intent(ChecklistActivity.this, FlightsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_home) {
                    Intent intent = new Intent(ChecklistActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_journal) {
                    Intent intent = new Intent(ChecklistActivity.this, JournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }
}