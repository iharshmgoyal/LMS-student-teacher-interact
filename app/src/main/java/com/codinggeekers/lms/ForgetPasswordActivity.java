package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.codinggeekers.lms.databinding.ActivityForgetPasswordBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class ForgetPasswordActivity extends AppCompatActivity {

    ActivityForgetPasswordBinding binding;

    FirebaseAuth auth = FirebaseAuth.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityForgetPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.forgetPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (binding.etEmailForget.getText().toString().isEmpty()){
                    Toast.makeText(ForgetPasswordActivity.this, "Enter Registered Email", Toast.LENGTH_SHORT).show();
                } else {
                    auth.sendPasswordResetEmail(binding.etEmailForget.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()){
                                        Log.d("TAG", "onComplete: Email sent");
                                        Toast.makeText(ForgetPasswordActivity.this, "Check your email\nAnd Follow instructions Provided.",
                                                Toast.LENGTH_LONG).show();
                                        startActivity(new Intent(ForgetPasswordActivity.this, LoginActivity.class));
                                        finish();
                                    } else {
                                        Toast.makeText(ForgetPasswordActivity.this,
                                                "Error: "+ Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            }
        });
    }
}