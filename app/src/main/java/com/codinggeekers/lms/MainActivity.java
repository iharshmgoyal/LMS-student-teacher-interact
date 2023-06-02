package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codinggeekers.lms.Fragments.AddFriendFragment;
import com.codinggeekers.lms.Fragments.AddPostFragment;
import com.codinggeekers.lms.Fragments.HomeFragment;
import com.codinggeekers.lms.Fragments.NotificationFragment;
import com.codinggeekers.lms.Fragments.ProfileFragment;
import com.codinggeekers.lms.databinding.ActivityMainBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import me.ibrahimsn.lib.OnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

    ActivityMainBinding binding;

    FirebaseAuth auth;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();

        setSupportActionBar(binding.menuToolBar);
        getSupportActionBar();

        if (!user.isEmailVerified()){
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, new HomeFragment());
        transaction.commit();

        binding.bottomBar.setOnItemSelectedListener(    new OnItemSelectedListener() {
            @Override
            public boolean onItemSelect(int i) {

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

                switch (i) {
                    case 0:
                        transaction.replace(R.id.container, new HomeFragment());
                        break;
                    case 1:
                        transaction.replace(R.id.container, new AddFriendFragment());
                        break;
                    case 2:
                        transaction.replace(R.id.container, new AddPostFragment());
                        break;
                    case 3:
                        transaction.replace(R.id.container, new NotificationFragment());
                        break;
                    case 4:
                        transaction.replace(R.id.container, new ProfileFragment());
                        break;
                }
                transaction.commit();
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(@NonNull Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.updateProfile:
//                auth.signOut();
//                startActivity(new Intent(getContext(), LoginActivity.class));
//                break;
            case R.id.logout:
                auth.signOut();
                finish();
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
            case R.id.direct:
                finish();
                startActivity(new Intent(MainActivity.this, ChatViewActivity.class));
                overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}