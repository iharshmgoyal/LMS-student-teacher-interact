package com.codinggeekers.lms;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codinggeekers.lms.Adapter.CommentAdapter;
import com.codinggeekers.lms.Models.Comment;
import com.codinggeekers.lms.Models.Notification;
import com.codinggeekers.lms.Models.Post;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.databinding.ActivityCommentBinding;
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

public class CommentActivity extends AppCompatActivity {

    ActivityCommentBinding binding;
    Intent intent;

    String postId, postedBy;

    FirebaseDatabase database;
    FirebaseAuth auth;

    ArrayList<Comment> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar2);
        CommentActivity.this.setTitle("Comments");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        intent = getIntent();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = intent.getStringExtra("postId");
        postedBy = intent.getStringExtra("postedBy");

        database.getReference().child("Posts")
                .child(postId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Post post = snapshot.getValue(Post.class);
//                        Picasso.get().load(post.getPostImg())
//                                .placeholder(R.drawable.placeholder)
//                                .into(binding.postImage);
                        Glide.with(CommentActivity.this)
                                .load(post.getPostImg())
                                .placeholder(R.drawable.placeholder)
                                .into(binding.postImage);
                        String description = post.getPostDescription();
                        if(description.equals("")) {
                            binding.postDescription.setVisibility(View.GONE);
                        } else {
                            binding.postDescription.setText(post.getPostDescription());
                            binding.postDescription.setVisibility(View.VISIBLE);
                        }
                        binding.like.setText(String.valueOf(post.getPostLike()));
                        binding.comment.setText(String.valueOf(post.getCommentCount()));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        database.getReference()
                .child("Users")
                .child(postedBy)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Picasso.get().load(user.getProfilePhoto()).placeholder(R.drawable.avatar)
                                .into(binding.profileImage);
                        binding.userNameInCommentActivity.setText(user.getName());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        binding.commentPostBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String commentET = binding.commentET.getText().toString();
                if (commentET.matches("")) {

                } else {
                    Comment comment = new Comment();
                    comment.setCommentBody(binding.commentET.getText().toString());
                    comment.setCommentedAt(new Date().getTime());
                    comment.setCommentedBy(FirebaseAuth.getInstance().getUid());
                    database.getReference()
                            .child("Posts")
                            .child(postId)
                            .child("Comments")
                            .push()
                            .setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            database.getReference()
                                    .child("Posts")
                                    .child(postId)

                                    .child("commentCount")
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            int commentCount = 0;
                                            if (snapshot.exists()) {
                                                commentCount = snapshot.getValue(int.class);
                                            }
                                            database.getReference()
                                                    .child("Posts")
                                                    .child(postId)
                                                    .child("commentCount")
                                                    .setValue(commentCount + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    binding.commentET.setText("");
                                                    Toast.makeText(CommentActivity.this, "Comment added!!!", Toast.LENGTH_SHORT).show();

                                                    Notification notification = new Notification();
                                                    notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                                    notification.setNotificationAt(new Date().getTime());
                                                    notification.setPostId(postId);
                                                    notification.setPostedBy(postedBy);
                                                    notification.setType("Comment");

                                                    FirebaseDatabase.getInstance().getReference()
                                                            .child("Notification")
                                                            .child(postedBy)
                                                            .push()
                                                            .setValue(notification);
                                                }
                                            });
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                    });
                }
            }
        });

        CommentAdapter adapter = new CommentAdapter(this, list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        binding.commentRV.setLayoutManager(layoutManager);
        binding.commentRV.setAdapter(adapter);

        database.getReference()
                .child("Posts")
                .child(postId)
                .child("Comments")
                .addValueEventListener(new ValueEventListener() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        list.clear();
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()){
                            Comment comment = dataSnapshot.getValue(Comment.class);
                            list.add(comment);
                        }
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }
}