package com.example.dial2;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.example.dial2.menuPage.CategoryFragment;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    CardView card1, card2, card3, card4, card5, card6;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    Toolbar toolbar;
    Button btnChangeLang;
    ProgressBar progressBar;



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, login.class); // or LoginActivity.class if renamed
            startActivity(intent);
            finish();
            return;
        }
        SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        String lang = prefs.getString("My_Lang", "en");
        setLocale(lang);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        card1 = findViewById(R.id.card1);
        card2 = findViewById(R.id.card2);
        card3 = findViewById(R.id.card3);
        card4 = findViewById(R.id.card4);
        card5 = findViewById(R.id.card5);
        card6 = findViewById(R.id.card6);
        drawerLayout = findViewById(R.id.main);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        btnChangeLang = findViewById(R.id.btn_change_language);

        Intent thisIntent =getIntent();
        String userEmail=thisIntent.getStringExtra("userEmail");
        String userEmailOriginal=thisIntent.getStringExtra("userEmailOriginal");

//        progressBar = findViewById(R.id.progressBarLogin);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar,R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.bringToFront();

        btnChangeLang.setOnClickListener(v -> {
            final String[] languages = {"English", "हिंदी", "मराठी", "ગુજરાતી", "മലയാളം"};
            final String[] languageCodes = {"en", "hi", "mr", "gu", "ml"};

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Choose Language")
                    .setItems(languages, (dialog, which) -> {
                        setLocale(languageCodes[which]);
                    })
                    .show();
        });

        card1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                intent.putExtra("navigateTo", R.id.nav_riceItems_menupage); // Change to desired menu item ID
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);

            }
        });

        card2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                // Pass the ID of the fragment you want to navigate to
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("navigateTo", R.id.nav_lunchItems_menupage); // Change to desired menu item ID
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);
            }
        });

        card3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("navigateTo", R.id.nav_dosaItems_menupage); // Change to desired menu item ID
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);
            }
        });

        card4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("navigateTo", R.id.nav_sandwich_menupage); // Change to desired menu item ID
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);
            }
        });

        card5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("navigateTo", R.id.nav_chatItem_menupage); // Change to desired menu item ID
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);
            }
        });

        card6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity3.class);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("navigateTo", R.id.nav_hotItems_menupage); // Change to desired menu item ID
                intent.putExtra("userEmailOriginal",userEmailOriginal);
                startActivity(intent);
            }
        });

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_home);
    }



    @Override
    public void onBackPressed(){

        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else {
            super.onBackPressed();
        }
    }

    public void setLocale(String langCode) {
        // Check if the current locale is different from the new one
        String currentLang = getSharedPreferences("Settings", MODE_PRIVATE).getString("My_Lang", "en");
        if (!currentLang.equals(langCode)) {
            // Set new locale only if different
            Locale locale = new Locale(langCode);
            Locale.setDefault(locale);

            Configuration config = new Configuration();
            config.setLocale(locale);

            // Update configuration to apply the locale change
            Context context = getApplicationContext().createConfigurationContext(config);
            getResources().updateConfiguration(config, getResources().getDisplayMetrics());

            // Save preference for future reference
            SharedPreferences.Editor editor = getSharedPreferences("Settings", MODE_PRIVATE).edit();
            editor.putString("My_Lang", langCode);
            editor.apply();

            // Call `recreate()` safely after changing the language
            recreate(); // This will only restart the activity when the language actually changes
        }
    }




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuitem) {
        if (menuitem.getItemId() == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, About.class);
            startActivity(intent);
            DrawerLayout drawer = findViewById(R.id.main);
            drawer.closeDrawer(GravityCompat.START);
            return true;
        } else if (menuitem.getItemId() == R.id.nav_contactus) {
            Intent intent = new Intent(MainActivity.this, Contact.class);
            startActivity(intent);
        } else if (menuitem.getItemId() == R.id.profile) {
            Intent intent = new Intent(MainActivity.this, Profile.class);
            startActivity(intent);
        } else if (menuitem.getItemId() == R.id.nav_home){
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            startActivity(intent);
        } else if (menuitem.getItemId() == R.id.logout) {
            // Clear session (example using SharedPreferences)
            // 1. Sign out from Firebase
            FirebaseAuth.getInstance().signOut();

            // 2. Clear session or login-related SharedPreferences
            SharedPreferences preferences = getSharedPreferences("user_session", MODE_PRIVATE);
            preferences.edit().clear().apply();

            // If you use any other SharedPreferences for login (like "loginPrefs"), clear them too:
            // getSharedPreferences("loginPrefs", MODE_PRIVATE).edit().clear().apply();

            // 3. Redirect to LoginActivity (or your login screen)
            Intent intent = new Intent(getApplicationContext(), login.class);
            // Clear the back stack so user can't return by pressing back
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 4. Finish current activity
            finish();
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}