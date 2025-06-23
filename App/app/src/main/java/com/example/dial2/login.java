package com.example.dial2;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class login extends AppCompatActivity {

    Button submitButton;
    TextView signUp;
    TextView emailError;
    EditText etEmail, etPassword;
    FirebaseAuth auth;
    ProgressBar progressBarLogin;

    // Define the allowed email domain (change as needed)
    private static final String ALLOWED_EMAIL_DOMAIN = "@somaiya.edu";

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null){

            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            String email = currentUser.getEmail();
            String Uemail = email.replace(".", "_").replace("@", "_");
            intent.putExtra("userEmailOriginal",email);
            intent.putExtra("userEmail",Uemail);

            startActivity(intent);


        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        signUp = findViewById(R.id.SignUp);
        submitButton = findViewById(R.id.SubmitButton);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        emailError = findViewById(R.id.emailError);

        auth = FirebaseAuth.getInstance();
//        progressBarLogin = findViewById(R.id.progressBarLogin);

        // Sign Up Click Event
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(login.this, MainActivity2.class);
            startActivity(intent);
        });

        // Submit Button Click Event
        submitButton.setOnClickListener(v -> validateInputs());

    }
    private void validateInputs() {
//        progressBarLogin.setVisibility(View.VISIBLE);
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Reset errors
        emailError.setVisibility(View.GONE);
        etPassword.setError(null);

        // Email Validation
        if (email.isEmpty()) {
            emailError.setText("Email is required!");
            emailError.setVisibility(View.VISIBLE);
//            progressBarLogin.setVisibility(View.GONE);
            return;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailError.setText("Invalid email format!");
            emailError.setVisibility(View.VISIBLE);
//            progressBarLogin.setVisibility(View.GONE);
            return;
        } else if (!isValidSomaiyaEmail(email)) {
            emailError.setText("Please use a valid Somaiya email");
            emailError.setVisibility(View.VISIBLE);
//            progressBarLogin.setVisibility(View.GONE);
            return;
        }

        // Password Validation
        if (password.isEmpty()) {
            etPassword.setError("Password is required!");
            etPassword.requestFocus();
//            progressBarLogin.setVisibility(View.GONE);
            return;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters long!");
            etPassword.requestFocus();
//            progressBarLogin.setVisibility(View.GONE);
            return;
        }

        // Proceed to Firebase Authentication
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
//                    progressBarLogin.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        Toast.makeText(login.this, "Login Successful", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        String Uemail=email;
                        Uemail = Uemail.replace(".", "_").replace("@", "_");
                        intent.putExtra("userEmail",Uemail);
                        intent.putExtra("userEmailOriginal",email);
                        startActivity(intent);
                        finish(); // This will prevent going back to Login page after login
                    } else {
                        Toast.makeText(login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Function to validate Somaiya email
    private boolean isValidSomaiyaEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@somaiya\\.edu$",email);
   }
}
