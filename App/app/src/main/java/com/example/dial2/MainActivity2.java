package com.example.dial2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

public class MainActivity2 extends AppCompatActivity {
    private static final String TAG = "RegistrationActivity";

    EditText etFirstName, etEmail, etPassword, etConfirmPassword;
    Button registerButton;
    TextView emailError, passwordError, countdownText;
    FirebaseAuth auth;
    ProgressBar progressBarRegister;
    Handler handler = new Handler(Looper.getMainLooper());
    int time = 4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Initialize UI Components
        initializeViews();

        // Set Click Listener for Register Button
        registerButton.setOnClickListener(v -> validateAndRegisterUser());
    }

    private void initializeViews() {
        etFirstName = findViewById(R.id.etFirstName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        registerButton = findViewById(R.id.RegisterButton);
        emailError = findViewById(R.id.emailError);
        passwordError = findViewById(R.id.passwordError);
        countdownText = findViewById(R.id.countdownText);
        progressBarRegister = findViewById(R.id.progressBarRegister);
    }

    private void validateAndRegisterUser() {
        // Reset error visibility
        emailError.setVisibility(View.GONE);
        passwordError.setVisibility(View.GONE);

        // Collect input values
        String name = etFirstName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validation Checks
        if (!validateInputs(name, email, password, confirmPassword)) {
            return;
        }

        // Show Progress
        progressBarRegister.setVisibility(View.VISIBLE);

        // Perform Firebase Registration
        performFirebaseRegistration(email, password);
    }

    private boolean validateInputs(String name, String email, String password, String confirmPassword) {
        boolean isValid = true;

        if (name.isEmpty()) {
            Toast.makeText(this, "Name is required!", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        if (!isValidSomaiyaEmail(email)) {
            emailError.setText("Please use a valid Somaiya email");
            emailError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (password.isEmpty() || password.length() < 6) {
            passwordError.setText("Password must be at least 6 characters!");
            passwordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        if (!password.equals(confirmPassword)) {
            passwordError.setText("Passwords do not match!");
            passwordError.setVisibility(View.VISIBLE);
            isValid = false;
        }

        return isValid;
    }

    private void performFirebaseRegistration(String email, String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        progressBarRegister.setVisibility(View.GONE);

                        if (task.isSuccessful()) {
                            // Registration Success
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = auth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // Registration Failed
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e) {
                                passwordError.setText("Weak password. Choose a stronger password.");
                                passwordError.setVisibility(View.VISIBLE);
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                emailError.setText("Invalid email");
                                emailError.setVisibility(View.VISIBLE);
                            } catch (FirebaseAuthUserCollisionException e) {
                                emailError.setText("Email already in use");
                                emailError.setVisibility(View.VISIBLE);
                            } catch (Exception e) {
                                Toast.makeText(MainActivity2.this,
                                        "Registration failed: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
    }

    private void updateUI(FirebaseUser user) {
        if (user != null) {
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(MainActivity2.this, MainActivity.class);
            String email = user.getEmail();
            String Uemail = email.replace(".", "_").replace("@", "_");
            intent.putExtra("userEmail",Uemail);
            intent.putExtra("userEmailOriginal",email);
            startActivity(intent);
            finish();
        }
    }

    private boolean isValidSomaiyaEmail(String email) {
        return Pattern.matches("^[a-zA-Z0-9._%+-]+@somaiya\\.edu$", email);
    }
}