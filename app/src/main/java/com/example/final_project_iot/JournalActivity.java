package com.example.final_project_iot;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.text.InputType;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class JournalActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private SharedPreferences sp;
    private AutoCompleteTextView countries_text_autocomplete;
    private ListView list;
    private TextView list_header;
    private Button add_review_button;
    private ProgressBar loading_indicator;
    private ArrayList<String> COUNTRIES;
    private ArrayAdapter<String> countries_adapter;
    private ArrayAdapter<String> reviews_adapter;
    private String selected_country = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_journal);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Set Journal to be checked on the menu on app start
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.getMenu().findItem(R.id.menu_journal).setChecked(true);

        // Load list of countries from a .txt file in raw folder to arrayList
        try (InputStream inputStream = getResources().openRawResource(R.raw.counties_list)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            COUNTRIES = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                COUNTRIES.add(line);
            }
            reader.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }

        // Link the autocomplete and the list of countries using array adapter
        countries_adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        countries_text_autocomplete = findViewById(R.id.autoCompleteTextView1);
        countries_text_autocomplete.setAdapter(countries_adapter);

        // init reviews array adapter, set header and set adapter to the listView
        reviews_adapter =  new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        list_header = new TextView(this);
        list_header.setText(R.string.journal_list_header);
        list = findViewById(R.id.reviews_list);
        list.addHeaderView(list_header);
        list.setAdapter(reviews_adapter);

        loading_indicator = findViewById(R.id.progressBar);
        // Pull from firebase all the relevant reviews for this country and display in listView
        countries_text_autocomplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loading_indicator.setVisibility(View.VISIBLE); // indicate loading to user
                selected_country = countries_text_autocomplete.getText().toString(); // save last selected country
                Toast.makeText(JournalActivity.this, selected_country, Toast.LENGTH_SHORT).show();
                // Connect to firebase and pull all relevant reviews for the selected country
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                DatabaseReference myRef = database.getReference("reviews").child(selected_country);
                myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        reviews_adapter.clear();
                        if (snapshot.exists()){
                            String line;
                            for (DataSnapshot data : snapshot.getChildren()){
                                line = data.getKey() + ": " + data.getValue(String.class);
                                reviews_adapter.add(line);
                            }
                        }
                        else {
                            reviews_adapter.add(getString(R.string.empty_list));
                        }
                        reviews_adapter.notifyDataSetChanged(); // notify adapter that the contents changed and list needs to be refreshed
                        runOnUiThread(() -> { // update UI elements from main thread
                            // hide loading indicator and set list to be visible
                            loading_indicator.setVisibility(View.GONE);
                            list.setVisibility(View.VISIBLE);
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(JournalActivity.this, "Error getting reviews", Toast.LENGTH_LONG).show();
                    }
                });
            }
        });

        sp = getSharedPreferences("Logged_in_sp", 0);
        add_review_button = findViewById(R.id.add_review_button);
        add_review_button.setOnClickListener(new View.OnClickListener() { // add listener to add review button
            @Override
            public void onClick(View v) { // When clicked, ask the user for a review message and update the list
                if (sp.getBoolean("logged_in", false)){
                    if (!selected_country.isEmpty()){
                        AlertDialog.Builder alert_builder = new AlertDialog.Builder(JournalActivity.this);
                        alert_builder.setTitle("What would you like to say about this country?");
                        final EditText input = new EditText(JournalActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_TEXT);
                        alert_builder.setView(input);
                        alert_builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // update database and add to list
                                String username = sp.getString("username", "");
                                if (!username.isEmpty()){
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference myRef = database.getReference("reviews").child(selected_country).child(username);
                                    myRef.setValue(input.getText().toString());
                                    // if list is empty (only default message) clear it out first before adding review
                                    if(reviews_adapter.getItem(0).equals(getString(R.string.empty_list))){
                                        reviews_adapter.clear();
                                    }
                                    reviews_adapter.add(username + ": " + input.getText().toString());
                                    reviews_adapter.notifyDataSetChanged();
                                }
                                else
                                    Toast.makeText(JournalActivity.this, "This shouldn't happen but username is empty", Toast.LENGTH_LONG).show();
                            }
                        });
                        alert_builder.setNegativeButton("Cancel", null);
                        AlertDialog dialogBox = alert_builder.create();
                        dialogBox.show();
                    }
                    else {
                        AlertDialog.Builder alert_builder = new AlertDialog.Builder(JournalActivity.this);
                        alert_builder.setMessage("You must first select a country").setCancelable(false).setPositiveButton("OK", null);
                        AlertDialog alert = alert_builder.create();
                        alert.show();
                    }
                }
                else { // If not logged in show a pop up message
                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(JournalActivity.this);
                    alert_builder.setMessage("You must be logged in to add a new review").setCancelable(false).setPositiveButton("OK", null);
                    AlertDialog alert = alert_builder.create();
                    alert.show();
                }
            }
        });

        // Set listener for when you click an item from the list, it will send an sms message with this review
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (id != -1){ // -1 is header
                    // make sure list isn't empty (only default message)
                    if (!list.getItemAtPosition(position).toString().equals(getString(R.string.empty_list))){
                        // Prepare sms message
                        String full_review = list.getItemAtPosition(position).toString();
                        String username = full_review.split(":")[0];
                        String message_body = full_review.substring(username.length()+2);
                        String sms_message = "Hey! a friend wanted you to know that:\n" + username +
                                " have said: \"" + message_body + "\"" + "about " + selected_country;

                        // ask for phone number and send sms message
                        AlertDialog.Builder alert_builder = new AlertDialog.Builder(JournalActivity.this);
                        alert_builder.setTitle("Enter a friend's phone number\n(normal israeli number)");
                        final EditText input = new EditText(JournalActivity.this);
                        input.setInputType(InputType.TYPE_CLASS_PHONE);
                        alert_builder.setView(input);
                        alert_builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { // update database and add to list
                                String phone_num = input.getText().toString();
                                try {
                                    SmsManager smsManager = SmsManager.getDefault();
                                    smsManager.sendTextMessage(phone_num, null, sms_message, null, null);
                                }
                                catch (Exception e){
                                    AlertDialog.Builder alert_builder = new AlertDialog.Builder(JournalActivity.this);
                                    alert_builder.setMessage("You must give the app SMS permissions to perform this action").setCancelable(false).setPositiveButton("OK", null);
                                    AlertDialog alert = alert_builder.create();
                                    alert.show();
                                }
                            }
                        });
                        alert_builder.setNegativeButton("Cancel", null);
                        AlertDialog dialogBox = alert_builder.create();
                        dialogBox.show();
                    }
                }
            }
        });

        // add event listener to navigation bar
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_profile){
                    Intent intent = new Intent(JournalActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_flights) {
                    Intent intent = new Intent(JournalActivity.this, FlightsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_home) {
                    Intent intent = new Intent(JournalActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_checklist) {
                    Intent intent = new Intent(JournalActivity.this, ChecklistActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }
}