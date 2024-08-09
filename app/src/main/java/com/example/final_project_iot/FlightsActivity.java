package com.example.final_project_iot;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.VideoView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.Calendar;

public class FlightsActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private VideoView video;
    private FrameLayout video_layout;
    private EditText start_location;
    private EditText end_location;
    private EditText start_date;
    private EditText end_date;
    private Button search_button;
    private String start_date_url_arg;
    private String end_date_url_arg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_flights);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Set 'flights' to be checked on the menu when activity starts
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.getMenu().findItem(R.id.menu_flights).setChecked(true);

        // For some reason the MediaPlayer listener is not executing if videoView is set to invisible initially.
        // So I just put it in a frame layout and made that invisible initially instead.
        video_layout = findViewById(R.id.video_layout);
        String video_uri = "android.resource://" + getPackageName() + "/" + R.raw.plane_takeoff;
        video = findViewById(R.id.videoView);
        video.setVideoURI(Uri.parse(video_uri));
        video.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) { // after the video is loaded start playing and make it visible and looping
                mp.setLooping(true);
                video.start();
                video_layout.setVisibility(View.VISIBLE);
            }
        });

        start_date = findViewById(R.id.editTextDateStart);
        start_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when clicked, get current date and open a new date picker dialog with current date marked initially
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(FlightsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // when date is selected, it is displayed in the text edit in normal format
                                // also save in correct url argument format
                                start_date.setText(getString(R.string.selected_date, dayOfMonth, monthOfYear+1, year));
                                start_date_url_arg = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                            }
                        },
                        // passing current year, month and day for to date picker (initialization).
                        year, month, day);
                // at last displaying the date picker
                datePickerDialog.show();
            }
        });

        end_date = findViewById(R.id.editTextDateEnd);
        end_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // when clicked, get current date and open a new date picker dialog with current date marked initially
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(FlightsActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                // when date is selected, it is displayed in the text edit in normal format
                                // also save in correct url argument format
                                end_date.setText(getString(R.string.selected_date, dayOfMonth, monthOfYear+1, year));
                                end_date_url_arg = year + "-" + (monthOfYear + 1) + "-" + dayOfMonth;
                            }
                        },
                        // passing current year, month and day for to date picker (initialization).
                        year, month, day);
                // at last displaying the date picker
                datePickerDialog.show();
            }
        });

        // Find the edit texts to fill in the url use when button is pressed
        start_location = findViewById(R.id.editTextCityFrom);
        end_location = findViewById(R.id.editTextCityTo);

        search_button = findViewById(R.id.book_flight_button);
        search_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { // find a trip on google flights with the given parameters
                String url = "https://www.google.com/travel/flights?q=Flights%20from%20"+ start_location.getText().toString()
                        +"%20to%20"+ end_location.getText().toString()
                        +"%20on%20"+ start_date_url_arg +"%20through%20" + end_date_url_arg;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.addCategory(Intent.CATEGORY_BROWSABLE);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        });

        // add event listener to navigation bar
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_profile) {
                    Intent intent = new Intent(FlightsActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_home) {
                    Intent intent = new Intent(FlightsActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_checklist) {
                    Intent intent = new Intent(FlightsActivity.this, ChecklistActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_journal) {
                    Intent intent = new Intent(FlightsActivity.this, JournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }
}