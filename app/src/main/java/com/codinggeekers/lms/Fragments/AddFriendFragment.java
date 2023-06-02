package com.codinggeekers.lms.Fragments;


import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.codinggeekers.lms.Adapter.UsersAdapter;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.databinding.FragmentAddFriendBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class AddFriendFragment extends Fragment {

    ArrayList<User> list = new ArrayList<>();

    FragmentAddFriendBinding binding;

    //    Firebase
    FirebaseAuth auth;
    FirebaseDatabase database;

    public AddFriendFragment() {
        // Required empty public constructor

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(false);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentAddFriendBinding.inflate(inflater, container, false);

        UsersAdapter adapter = new UsersAdapter(getContext(), list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.usersRV.setLayoutManager(layoutManager);
        binding.usersRV.setAdapter(adapter);

        database.getReference().child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User users = dataSnapshot.getValue(User.class);
                    users.setUserID(dataSnapshot.getKey());
                    if(!dataSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid()) && !users.getUserID().equals("HcdPXSZCaAdYr6guNDka5KEmFsE2")) {
                        list.add(users);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        return binding.getRoot();
    }
}