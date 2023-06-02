package com.codinggeekers.lms.Adapter;

import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.codinggeekers.lms.CommentActivity;
import com.codinggeekers.lms.Models.Notification;
import com.codinggeekers.lms.Models.Post;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.DashboardRvSampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
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

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.viewHolder> {

    ArrayList<Post> list;
    Context context;

    public PostAdapter(ArrayList<Post> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.dashboard_rv_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(10));

        Post model = list.get(position);
//        Picasso.get().load(model.getPostImg()).placeholder(R.drawable.placeholder).into(holder.binding.postImage);

        Glide.with(context).load(model.getPostImg()).apply(requestOptions).placeholder(R.drawable.placeholder).into(holder.binding.postImage);

        holder.binding.like.setText(model.getPostLike() + "");
        holder.binding.comment.setText(model.getCommentCount() + "");
        String description = model.getPostDescription();
        if (description.equals("")) {
            holder.binding.postDescription.setVisibility(View.GONE);
        } else {
            holder.binding.postDescription.setText(Html.fromHtml("<b>"+model.getPostedByName()+"</b>"+" "+ model.getPostDescription()));
            holder.binding.postDescription.setVisibility(View.VISIBLE);
        }

        FirebaseDatabase.getInstance().getReference().child("Users")
                .child(model.getPostedBy()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User users = snapshot.getValue(User.class);

                Picasso.get().
                        load(users.getProfilePhoto()).
                        placeholder(R.drawable.avatar).into(holder.binding.profileImage);
                holder.binding.userName.setText(users.getName());
                holder.binding.about.setText(users.getUniversity());

                String timeAgo = TimeAgo.using(model.getPostedAt());

                holder.binding.postedAt.setText(timeAgo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        FirebaseDatabase.getInstance().getReference()
                .child("Posts")
                .child(model.getPostId())
                .child("Likes")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_red, 0, 0, 0);
                } else {
                    holder.binding.like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            FirebaseDatabase.getInstance().getReference().child("Posts")
                                    .child(model.getPostId())
                                    .child("Likes")
                                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                                    .setValue(true).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    FirebaseDatabase.getInstance().getReference().child("Posts")
                                            .child(model.getPostId())
                                            .child("postLike")
                                            .setValue(model.getPostLike() + 1).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            holder.binding.like.setCompoundDrawablesWithIntrinsicBounds(R.drawable.heart_red, 0, 0, 0);

                                            Notification notification = new Notification();
                                            notification.setNotificationBy(FirebaseAuth.getInstance().getUid());
                                            notification.setNotificationAt(new Date().getTime());
                                            notification.setPostId(model.getPostId());
                                            notification.setPostedBy(model.getPostedBy());
                                            notification.setType("Like");

                                            FirebaseDatabase.getInstance().getReference()
                                                    .child("Notification")
                                                    .child(model.getPostedBy())
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

        holder.binding.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, CommentActivity.class);
                intent.putExtra("postId", model.getPostId());
                intent.putExtra("postedBy", model.getPostedBy());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        DashboardRvSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = DashboardRvSampleBinding.bind(itemView);
        }
    }
}
