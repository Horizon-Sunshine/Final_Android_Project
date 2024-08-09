package com.example.final_project_iot;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private BottomNavigationView navigationView;
    private TextView country_description_textView;
    private ProgressBar loading_indicator;
    private ArrayAdapter<String> adapter;
    private AutoCompleteTextView textView;
    private ArrayList<String> COUNTRIES;
    private String selected_country;
    private ArrayList<String> image_urls;
    private String country_description;
    private ArrayList<Bitmap> bitmaps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });
        // Set home to be checked on the menu on app start
        navigationView = findViewById(R.id.bottomNavigationView);
        navigationView.getMenu().findItem(R.id.menu_home).setChecked(true);

        // Load list of countries from a .txt file in raw folder
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
        // Link the autocomplete and the list of countries with array adapter
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, COUNTRIES);
        textView = findViewById(R.id.autoCompleteTextView);
        textView.setAdapter(adapter);
        loading_indicator = findViewById(R.id.progressBar);
        textView.setOnItemClickListener(new AdapterView.OnItemClickListener() { // Decide what happens when a country is clicked
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                loading_indicator.setVisibility(View.VISIBLE);
                selected_country = textView.getText().toString();
                Toast.makeText(MainActivity.this, selected_country, Toast.LENGTH_SHORT).show();

                ExecutorService executor = Executors.newSingleThreadExecutor(); // make sure all separate asynchronous tasks run sequentially on one thread
                Handler handler = new Handler(Looper.getMainLooper()); // get handler for the main thread running the UI

                //Jsoup (web scraping tool) has to be run on a separate thread or it does not work properly
                executor.execute(() -> { // Run a separate thread
                    bitmaps = new ArrayList<>();
                    image_urls = new ArrayList<>();
                    // Get 4 images and a short description of selected country
                    String url1 = "https://www.pexels.com/search/" + selected_country.replace(" ", "%20");
                    String url2 = "https://www.google.com/search?q=" + selected_country.replace(" ", "+");
                    try {
                        // get the relevant html documents from the urls
                        Document doc1 = Jsoup.connect(url1).userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.135 Safari/537.36 Edge/12.246").get();
                        Document doc2 = Jsoup.connect(url2).get();

                        Elements images = doc1.select("div[data-testid='item'] img"); // extract images from page
                        for (int i = 0; i < 4; i++){ // add only first 4 images src
                            image_urls.add(images.get(i).attr("src"));
                        }
                        country_description = doc2.selectFirst("div[data-attrid='description'] span span").text(); // Get the description string from a google search

                        // Get the images from the srcs and convert them to bitmaps
                        for (final String img_url : image_urls){
                            URL url = new URL(img_url);
                            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                            connection.setDoInput(true);
                            connection.connect();
                            InputStream input = connection.getInputStream();
                            bitmaps.add(BitmapFactory.decodeStream(input));
                        }
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                    handler.post(() -> { // Force the UI to update from the main thread
                        // Update UI
                        loading_indicator.setVisibility(View.GONE); // hide loading indicator
                        int[] img_ids = {R.id.image1, R.id.image2, R.id.image3, R.id.image4};
                        for (int i = 0; i < 4; i++){
                            String img_url = image_urls.get(i);
                            ImageView imageView = findViewById(img_ids[i]);
                            imageView.setImageBitmap(bitmaps.get(i));
                            imageView.setOnClickListener(new View.OnClickListener(){
                                public void onClick(View v){ // make image clickable and open full size in browser
                                    Intent intent = new Intent();
                                    intent.setAction(Intent.ACTION_VIEW);
                                    intent.addCategory(Intent.CATEGORY_BROWSABLE);
                                    intent.setData(Uri.parse(img_url.substring(0, img_url.indexOf('?'))));
                                    startActivity(intent);
                                }
                            });
                            // Images appear with an animation
                            ObjectAnimator mover = ObjectAnimator.ofFloat(imageView, "translationY", 400f, 0f).setDuration(1000);
                            ObjectAnimator fadeIn = ObjectAnimator.ofFloat(imageView, "alpha", 0f, 1f).setDuration(1000);
                            AnimatorSet animatorSet = new AnimatorSet();
                            animatorSet.play(fadeIn).with(mover);
                            animatorSet.start();
                        }
                        country_description_textView = findViewById(R.id.description_textView);
                        if (!country_description.isEmpty()) {
                            country_description_textView.setBackgroundColor(Color.parseColor("#99141414"));
                            country_description_textView.setText(country_description);
                        }
                        else { // If for some reason description fetch failed, make the textView disappear
                            country_description_textView.setBackgroundColor(0);
                            country_description_textView.setText("");
                        }
                    });
                });
                executor.shutdown(); //shut down executor after the separate thread finished working
            }
        });
        // add event listener to navigation bar
        navigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.menu_profile){
                    Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_flights) {
                    Intent intent = new Intent(MainActivity.this, FlightsActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_checklist) {
                    Intent intent = new Intent(MainActivity.this, ChecklistActivity.class);
                    startActivity(intent);
                    finish();
                } else if (id == R.id.menu_journal) {
                    Intent intent = new Intent(MainActivity.this, JournalActivity.class);
                    startActivity(intent);
                    finish();
                }
                return true;
            }
        });
    }
}