package com.codinggeekers.lms.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.codinggeekers.lms.Models.Follow;
import com.codinggeekers.lms.Models.Notification;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.UsersSampleBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.viewHolder>{

    Context context;
    ArrayList<User> list;

    public UsersAdapter(Context context, ArrayList<User> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.users_sample,parent,false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        User users = list.get(position);
        Picasso.get().load(users.getProfilePhoto()).placeholder(R.drawable.avatar).into(holder.binding.profileImage);

        holder.binding.name.setText(users.getName());
        holder.binding.university.setText(users.getUniversity());

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(users.getUserID())
                .child("Followers")
                .child(FirebaseAuth.getInstance().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                    holder.binding.followBtn.setText("Following");
                    holder.binding.followBtn.setTextColor(context.getResources().getColor(R.color.blue));
                    holder.binding.followBtn.setEnabled(false);
                }
                else {
                    holder.binding.followBtn.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            Follow follow = new Follow();
                            follow.setFollowedBy(FirebaseAuth.getInstance().getUid());
                            follow.setFollowedAt(new Date().getTime());

                            FirebaseDatabase.getInstance().getReference()
                                    .child("Users")
                                    .child(users.getUserID())
                                    .child("Followers")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .setValue(follow).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child("Users")
                                            .child(users.getUserID())
                                            .child("followerCount")
                                            .setValue(users.getFollowerCount()+1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            holder.binding.followBtn.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.follow_active_btn));
                                            holder.binding.followBtn.setText("Following");
                                            holder.binding.followBtn.setTextColor(context.getResources().getColor(R.color.blue));
                                            holder.binding.followBtn.setEnabled(false);

                                            Toast.makeText(context, "You followed "+users.getName(), Toast.LENGTH_SHORT).show();

                                            Notification notification = new Notification();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setType("Follow");

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Notification")
                                                    .child(users.getUserID())
                                                    .push()
                                                    .setValue(notification).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {

                                                }
                                            });
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
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        UsersSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = UsersSampleBinding.bind(itemView);
        }
    }
}
