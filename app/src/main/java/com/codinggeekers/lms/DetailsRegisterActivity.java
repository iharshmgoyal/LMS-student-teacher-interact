package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.Toast;

import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.databinding.ActivityDetailsRegisterBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;

public class DetailsRegisterActivity extends AppCompatActivity {

    ActivityDetailsRegisterBinding binding;

    ProgressDialog dialog;

    FirebaseDatabase database;
    FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        dialog = new ProgressDialog(this);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Registering user...");
        dialog.setMessage("User Creation in Progress...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        String university = getIntent().getStringExtra("university");
        String role = getIntent().getStringExtra("role");
        Log.d("TAG", "onCreate: University Received from ChooseUniversityActivity is : "+university);
        Log.d("TAG", "onCreate: Role Received from ChooseUniversityActivity is : "+role);

        setSupportActionBar(binding.detailsRegisterToolbar);
        DetailsRegisterActivity.this.setTitle("Provide Complete Details");
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        binding.detailsRegisterToolbar.setTitleTextColor(getResources().getColor(R.color.white));

        binding.universityWelcomeText.setText(String.format("Welcome %ss from %s \nFill the below form to proceed", role, university));


        binding.etDOB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getRawX() >= (binding.etDOB.getRight() - binding.etDOB.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // your action hereb
                        final int[] mYear = new int[1];
                        final int[] mMonth = new int[1];
                        final int[] mDay = new int[1];
                        // To show current date in the datepicker
                        Calendar mcurrentDate = Calendar.getInstance();
                        mYear[0] = mcurrentDate.get(Calendar.YEAR);
                        mMonth[0] = mcurrentDate.get(Calendar.MONTH);
                        mDay[0] = mcurrentDate.get(Calendar.DAY_OF_MONTH);

                        DatePickerDialog mDatePicker = new DatePickerDialog(DetailsRegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                            public void onDateSet(DatePicker datepicker, int selectedyear, int selectedmonth, int selectedday) {
                                Calendar myCalendar = Calendar.getInstance();
                                myCalendar.set(Calendar.YEAR, selectedyear);
                                myCalendar.set(Calendar.MONTH, selectedmonth);
                                myCalendar.set(Calendar.DAY_OF_MONTH, selectedday);
                                String myFormat = "dd/MM/yyyy"; //Change as you need
                                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.FRANCE);
                                binding.etDOB.setText(sdf.format(myCalendar.getTime()));

                                mDay[0] = selectedday;
                                mMonth[0] = selectedmonth;
                                mYear[0] = selectedyear;
                            }
                        }, mYear[0], mMonth[0], mDay[0]);
                        //mDatePicker.setTitle("Select date");
                        mDatePicker.show();
                        return true;
                    }
                }
                return false;
            }
        });

        binding.createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email, password, dob, programme, duration, name;
                email = binding.etEmail.getText().toString();
                password = binding.etPassword.getText().toString();
                dob = binding.etDOB.getText().toString();
                programme = binding.etProgram.getText().toString();
                duration = binding.etDuration.getText().toString();
                name = binding.etName.getText().toString();

                if (!email.equals("") && !password.equals("")) {
                    dialog.show();
                    auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {

                                        String id = Objects.requireNonNull(task.getResult().getUser()).getUid();

                                        auth.getCurrentUser().sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()){
                                                            startActivity(new Intent(DetailsRegisterActivity.this, LoginActivity.class));
                                                            Toast.makeText(DetailsRegisterActivity.this,
                                                                    "Verification Email Sent\nPlease check your mail.",
                                                                    Toast.LENGTH_SHORT).show();
                                                            User user = new User(
                                                                    name,
                                                                    university,
                                                                    email,
                                                                    password,
                                                                    dob,
                                                                    programme,
                                                                    duration,
                                                                    role
                                                            );

                                                            dialog.show();


                                                            database.getReference()
                                                                    .child("Users")
                                                                    .child(id)
                                                                    .setValue(user);

                                                        }
                                                        else {
                                                            Toast.makeText(DetailsRegisterActivity.this,
                                                                    "Error: "+task.getException().getMessage(),
                                                                    Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                } else {
                    Toast.makeText(DetailsRegisterActivity.this, "Required Fields are missing..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(DetailsRegisterActivity.this, ChooseUniversityActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}