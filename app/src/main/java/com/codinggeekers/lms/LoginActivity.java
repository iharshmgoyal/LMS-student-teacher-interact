package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codinggeekers.lms.databinding.ActivityLoginBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.ActionCodeSettings;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    ActivityLoginBinding binding;

    FirebaseAuth auth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    @Override
    protected void onStart() {
        super.onStart();
        if (user!=null && user.isEmailVerified()) {
            Log.d("TAG", "onStart: OnStartCheck If user is online: "+auth.getUid());
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
        } else {
            Log.d("TAG", "onStart: user is null");
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        binding.goToSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, ChooseUniversityActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            }
        });

        binding.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password;
                email = binding.etEmail.getText().toString();
                password = binding.etPassword.getText().toString();

                if (!email.equals("") && !password.equals("")) {

                        auth.signInWithEmailAndPassword(binding.etEmail.getText().toString(), binding.etPassword.getText().toString())
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if(task.isSuccessful()) {
                                            if (auth.getCurrentUser().isEmailVerified()) {
                                                Toast.makeText(LoginActivity.this, "User Login Successfully!!!", Toast.LENGTH_SHORT).show();
                                                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                            } else {
                                                Toast.makeText(LoginActivity.this, "Please verify your Email.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                        else {
                                            Toast.makeText(LoginActivity.this, Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                } else {
                    Toast.makeText(LoginActivity.this, "Required fields are missing...", Toast.LENGTH_SHORT).show();
                }

            }
        });

        binding.forgetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, ForgetPasswordActivity.class));
            }
        });

    }
}