package com.codinggeekers.lms.Fragments;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.codinggeekers.lms.Adapter.FollowersAdapter;
import com.codinggeekers.lms.Models.Follow;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.FragmentProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileFragment extends Fragment {

    FragmentProfileBinding binding;

    //    Firebase
    FirebaseAuth auth;
    FirebaseStorage storage;
    FirebaseDatabase database;

    ProgressDialog dialog;

    RecyclerView recyclerView;
    ArrayList<Follow> list;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();
        database = FirebaseDatabase.getInstance();
        dialog = new ProgressDialog(getContext());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        binding = FragmentProfileBinding.inflate(inflater, container, false);


        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Updating...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        database.getReference().child("Users").child(Objects.requireNonNull(auth.getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    Picasso.get().load(user.getCoverPhoto())
                            .placeholder(R.drawable.placeholder).into(binding.coverPhoto);

//                    Glide.with(getContext()).load(user.getCoverPhoto())
//                            .placeholder(R.drawable.placeholder).into(binding.coverPhoto);

                    binding.userName.setText(user.getName());
                    binding.university.setText(String.format("%s at %s", user.getRole(), user.getUniversity()));
                    binding.followersCount.setText(String.valueOf(user.getFollowerCount()));

                    Picasso.get().load(user.getProfilePhoto())
                            .placeholder(R.drawable.avatar).into(binding.profileImage);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        list = new ArrayList<>();

        FollowersAdapter adapter = new FollowersAdapter(list,getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.HORIZONTAL,false);
        binding.friendRV.setLayoutManager(linearLayoutManager);
        binding.friendRV.setAdapter(adapter);

        database.getReference().child("Users")
                .child(auth.getUid())
                .child("Followers").addValueEventListener(new ValueEventListener() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for(DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Follow follow = dataSnapshot.getValue(Follow.class);
                    list.add(follow);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.changeCoverPhoto.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("image/*");
            startActivityForResult(intent, 11);
        });

        binding.addProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 21);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 11) {
            if (data.getData()!=null) {
                dialog.show();
                Uri uri = data.getData();
                binding.coverPhoto.setImageURI(uri);
                final StorageReference reference = storage.getReference().child("cover_photo")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Cover Photo Updated!!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(auth.getUid()).child("coverPhoto")
                                        .setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }
        else if (requestCode == 21) {
            if (data.getData()!=null) {
                dialog.show();
                Uri uri = data.getData();
                binding.profileImage.setImageURI(uri);

                final StorageReference reference = storage.getReference().child("profile_photo")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()));

                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(getContext(), "Profile Photo Updated!!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                database.getReference().child("Users").child(auth.getUid()).child("profilePhoto")
                                        .setValue(uri.toString());
                            }
                        });
                    }
                });
            }
        }
        else {
            Toast.makeText(getContext(), "Something went wrong!!", Toast.LENGTH_SHORT).show();
        }
    }
}