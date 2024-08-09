package com.example.final_project_iot;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ProfileActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private SharedPreferences sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Set 'profile' to be checked on the menu when activity starts
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.getMenu().findItem(R.id.menu_profile).setChecked(true);

        sp = getSharedPreferences("Logged_in_sp", 0);
        if (sp.getBoolean("logged_in", false)) // if logged in, show profile fragment
            switchFragment(new ProfileFragment());
        else
            switchFragment(new LoginFragment()); // else show login fragment

        // add event listener to navigation bar
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_flights) {
                    Intent intent = new Intent(ProfileActivity.this, FlightsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_home) {
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_checklist) {
                    Intent intent = new Intent(ProfileActivity.this, ChecklistActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_journal) {
                    Intent intent = new Intent(ProfileActivity.this, JournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }

    // Helper functions used in fragments
    public void switchFragment(Fragment fragment) { // function to switch to the relevant fragment (log in/sign up/profile)
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.profile_fragment_view, fragment.getClass(), null);
        fragmentTransaction.commit();
    }
    public String Password_to_SHA256_Hex_String(String password) throws NoSuchAlgorithmException { // password protection with hashing
        // Get SHA-256 hashing algorithm
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // Perform hashing of password string and convert to byte array
        byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash){ // break down every byte into 2 hex digits and append
            hexString.append(String.format("%02X", b));
        }
        return hexString.toString();
    }
    public void login_attempt(String username, String password){
        if (username.isEmpty() || password.isEmpty()){ // make sure fields are not empty
            Toast.makeText(getApplicationContext(), "Username and password cannot remain empty", Toast.LENGTH_SHORT).show();
            return;
        }
        for (char c : username.toCharArray()){ // prevent illegal characters in firebase keys
            switch (c){
                case '.':
                case '$':
                case '#':
                case '[':
                case ']':
                case '/':
                case ' ':
                    Toast.makeText(getApplicationContext(), "Username cannot contain . $ [ ] / or whitespace", Toast.LENGTH_SHORT).show();
                    return;
            }
        }
        try {
            String hex_pass = Password_to_SHA256_Hex_String(password);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(username).child(hex_pass);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("logged_in", true);
                        editor.putString("username", username);
                        editor.putString("phone_num", dataSnapshot.child("phone_num").getValue(String.class));
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Logged in successfully!", Toast.LENGTH_LONG).show();
                        runOnUiThread(() -> switchFragment(new ProfileFragment())); // switch to profile fragment
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Incorrect username or password", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {// Failed to read value
                    Toast.makeText(getApplicationContext(), "Error accessing database" + error.getDetails(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    public void logout(){
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("logged_in", false);
        editor.remove("username");
        editor.remove("phone_num");
        editor.apply();
        switchFragment(new LoginFragment());
    }
    public void sign_up(String username, String password, String phone_num){ // Add new user to the database
        if (username.isEmpty() || password.isEmpty() || phone_num.isEmpty()){ // make sure fields are not empty
            Toast.makeText(getApplicationContext(), "Fields cannot remain empty", Toast.LENGTH_SHORT).show();
            return;
        }
        for (char c : username.toCharArray()){ // prevent illegal characters in firebase keys
            switch (c){
                case '.':
                case '$':
                case '#':
                case '[':
                case ']':
                case '/':
                case ' ':
                    Toast.makeText(getApplicationContext(), "Username cannot contain . $ [ ] / or whitespace", Toast.LENGTH_SHORT).show();
                    return;
            }
        }
        try {
            String hex_pass = Password_to_SHA256_Hex_String(password);
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            DatabaseReference myRef = database.getReference("users").child(username);
            myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.exists()) { // make sure user doesn't already exists
                        myRef.child(hex_pass).child("phone_num").setValue(phone_num);

                        SharedPreferences.Editor editor = sp.edit();
                        editor.putBoolean("logged_in", true);
                        editor.putString("username", username);
                        editor.putString("phone_num", phone_num);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Signed up successfully!", Toast.LENGTH_LONG).show();
                        runOnUiThread(() -> switchFragment(new ProfileFragment())); // switch to profile fragment
                    }
                    else
                        Toast.makeText(getApplicationContext(), "Username already taken", Toast.LENGTH_SHORT).show();
                }
                @Override
                public void onCancelled(@NonNull DatabaseError error) {// Failed to read value
                    Toast.makeText(getApplicationContext(), "Error accessing database" + error.getDetails(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}