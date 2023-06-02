package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codinggeekers.lms.Adapter.PostAdapter;
import com.codinggeekers.lms.Models.Follow;
import com.codinggeekers.lms.Models.Notification;
import com.codinggeekers.lms.Models.Post;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.databinding.ActivityFriendsProfileBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class FriendsProfileActivity extends AppCompatActivity {

    ActivityFriendsProfileBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;

    ArrayList<Post> postList;
    RecyclerView friendsProfileRV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFriendsProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String userId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String profilePic = getIntent().getStringExtra("profilePic");

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        friendsProfileRV = findViewById(R.id.friendsProfileRV);
//        friendsProfileRV.showShimmerAdapter();

        database.getReference()
                .child("Users")
                .child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User users = snapshot.getValue(User.class);
                        binding.userName.setText(userName);
                        binding.university.setText(users.getUniversity());
                        Picasso.get().load(users.getProfilePhoto()).placeholder(R.drawable.avatar).into(binding.profileImage);
                        Glide.with(FriendsProfileActivity.this).load(users.getCoverPhoto()).placeholder(R.drawable.placeholder).into(binding.coverPhoto);
                        if (users.getFollowerCount()!=0){
                            binding.followersCount.setText(Integer.toString(users.getFollowerCount()));
                        } else {
                            binding.followersCount.setText("0");
                        }

                        FirebaseDatabase.getInstance().getReference()
                                .child("Users")
                                .child(userId)
                                .child("Followers")
                                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()) {
                                    binding.flwBtnAtUserProfile.setBackgroundDrawable(ContextCompat
                                            .getDrawable(FriendsProfileActivity.this, R.drawable.follow_active_btn));
                                    binding.flwBtnAtUserProfile.setText("Following");
                                    binding.flwBtnAtUserProfile.setTextColor(FriendsProfileActivity.this.getResources().getColor(R.color.blue));
                                    binding.flwBtnAtUserProfile.setEnabled(false);
                                }
                                else {
                                    binding.flwBtnAtUserProfile.setOnClickListener(new View.OnClickListener() {

                                        @Override
                                        public void onClick(View v) {
                                            Follow follow = new Follow();
                                            follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                                            follow.setFollowedAt(new Date().getTime());

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Users")
                                                    .child(userId)
                                                    .child("Followers")
                                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                                    .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("Users")
                                                            .child(userId)
                                                            .child("followerCount")
                                                            .setValue(users.getFollowerCount()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void unused) {
                                                            binding.flwBtnAtUserProfile.setBackgroundDrawable(ContextCompat.getDrawable(FriendsProfileActivity.this, R.drawable.follow_active_btn));
                                                            binding.flwBtnAtUserProfile.setText("Following");
                                                            binding.flwBtnAtUserProfile.setTextColor(FriendsProfileActivity.this.getResources().getColor(R.color.blue));
                                                            binding.flwBtnAtUserProfile.setEnabled(false);

                                                            Toast.makeText(FriendsProfileActivity.this, "You followed "+users.getName(), Toast.LENGTH_SHORT).show();

                                                            Notification notification = new Notification();
                                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                            notification.setNotificationAt(new Date().getTime());
                                                            notification.setType("Follow");

                                                            FirebaseDatabase.getInstance().getReference()
                                                                    .child("Notification")
                                                                    .child(users.getUserID())
                                                                    .push()
                                                                    .setValue(notification);

                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        //        Dashboard
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, FriendsProfileActivity.this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        friendsProfileRV.setLayoutManager(layoutManager);
        friendsProfileRV.setNestedScrollingEnabled(false);


        database.getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    if (post.getPostedBy().equals(userId)) {
                        post.setPostId(dataSnapshot.getKey());
                        postList.add(post);
                    }
                }
                friendsProfileRV.setAdapter(postAdapter);
//                friendsProfileRV.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}