package com.example.dial2;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.dial2.databinding.ActivityMainMenupageBinding;
import com.google.android.material.navigation.NavigationView;

public class MainActivity3 extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private ActivityMainMenupageBinding binding;
    public String userEmail;
    public String userEmailOriginal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainMenupageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.menupageAppBarMain.toolbar);

        // Get user email from intent
        Intent thisIntent = getIntent();
        userEmailOriginal = thisIntent.getStringExtra("userEmailOriginal");
        userEmail = thisIntent.getStringExtra("userEmail");

        // Set up FAB click listener
        binding.menupageAppBarMain.fab.setOnClickListener(view -> {
            Intent toCartPage = new Intent(MainActivity3.this, MainActivity4.class);
            toCartPage.putExtra("userEmail", userEmail);
            toCartPage.putExtra("userEmailOriginal", userEmailOriginal);
            startActivity(toCartPage);
        });

        // Handle initial navigation if specified
        int navigateTo = getIntent().getIntExtra("navigateTo", -1);
        if (navigateTo != -1) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_menupage);
            Bundle bundle = new Bundle();
            bundle.putString("userEmail", userEmail);
            bundle.putString("userEmailOriginal", userEmailOriginal);
            navController.navigate(navigateTo, bundle);
        }

        // Set up navigation drawer
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.menupageNavView;

        // Configure top level destinations
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_hotItems_menupage, R.id.nav_frankie_menupage, R.id.nav_lunchItems_menupage,
                R.id.nav_noodles_menupage, R.id.nav_soup_menupage, R.id.nav_chatItem_menupage,
                R.id.nav_choupsey_menupage, R.id.nav_dosaItems_menupage, R.id.nav_fastItems_menupage,
                R.id.nav_gravyItems_menupage, R.id.nav_grillSandwich_menupage,
                R.id.nav_masalaSandwich_menupage, R.id.nav_riceItems_menupage,
                R.id.nav_sandwich_menupage, R.id.nav_snacks_menupage, R.id.nav_spclRiceItems_menupage)
                .setOpenableLayout(drawer)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_menupage);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Custom navigation handling to pass email to fragments
        navigationView.setNavigationItemSelectedListener(item -> {
            // Create bundle with user email
            Bundle bundle = new Bundle();
            bundle.putString("userEmail", userEmail);
            bundle.putString("userEmailOriginal", userEmailOriginal);

            try {
                // Navigate to selected item with email arguments
                navController.navigate(item.getItemId(), bundle);
                drawer.closeDrawers();
                return true;
            } catch (IllegalArgumentException e) {
                // Handle case where navigation fails
                Toast.makeText(this, "Navigation error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menupage_main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_menupage);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main_menupage);
        if (navController.getCurrentDestination() != null &&
                navController.getCurrentDestination().getId() == R.id.nav_chatItem_menupage) {
            finish();
        } else {
            super.onBackPressed();
        }
    }
}