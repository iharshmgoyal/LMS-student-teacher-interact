package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.codinggeekers.lms.Adapter.UsersListAdapter;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.databinding.ActivityChatViewBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ChatViewActivity extends AppCompatActivity {

    ActivityChatViewBinding binding;

    ArrayList<User> list = new ArrayList<>();
    FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar2);
        getSupportActionBar().setTitle("Chats");

        database = FirebaseDatabase.getInstance();

        UsersListAdapter adapter = new UsersListAdapter(list, this);
        binding.chatRecyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.chatRecyclerView.setLayoutManager(layoutManager);

        database.getReference()
                .child("Users")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot :  snapshot.getChildren()){
                            User users = dataSnapshot.getValue(User.class);
                            users.setUserID(dataSnapshot.getKey());

                            if(!users.getUserID().equals(FirebaseAuth.getInstance().getUid()) && !users.getUserID().equals("HcdPXSZCaAdYr6guNDka5KEmFsE2")){
                                list.add(users);
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }
}