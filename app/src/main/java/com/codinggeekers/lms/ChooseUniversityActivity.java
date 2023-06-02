package com.codinggeekers.lms;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.codinggeekers.lms.databinding.ActivityChooseUniversityBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

public class ChooseUniversityActivity extends AppCompatActivity {

    ActivityChooseUniversityBinding binding;

    FirebaseDatabase database;

    String university;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChooseUniversityBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ArrayAdapter<CharSequence> universityAdapter = ArrayAdapter.createFromResource(this, R.array.university_select, android.R.layout.simple_spinner_item);
        universityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.universitySpinner.setAdapter(universityAdapter);

        ArrayAdapter<CharSequence> roleAdapter = ArrayAdapter.createFromResource(this, R.array.university_select, android.R.layout.simple_spinner_item);
        roleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.universitySpinner.setAdapter(roleAdapter);

        database = FirebaseDatabase.getInstance();

        binding.universitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if (selectedItem.matches("Select University")) {

                } else {
                    Log.d("TAG", "onItemSelected: Selected University : "+selectedItem);
                    university = selectedItem;
                    Toast.makeText(adapterView.getContext(), selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.roleSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getItemAtPosition(i).toString();
                if (selectedItem.matches("Select Role")) {

                } else {
                    Log.d("TAG", "onItemSelected: Selected Role : "+selectedItem);
                    role = selectedItem;
                    Toast.makeText(adapterView.getContext(), selectedItem, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        binding.btnUniversitySubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (university == null || university.equalsIgnoreCase("select university")
                        || role == null || role.equalsIgnoreCase("Select Role")){
                    Toast.makeText(ChooseUniversityActivity.this, "Choose all fields to proceed.", Toast.LENGTH_SHORT).show();
                } else {
                    Intent intent = new Intent(ChooseUniversityActivity.this, DetailsRegisterActivity.class);
                    intent.putExtra("university", university);
                    intent.putExtra("role", role);
                    startActivity(intent);
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);

                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(ChooseUniversityActivity.this, LoginActivity.class));
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }
}