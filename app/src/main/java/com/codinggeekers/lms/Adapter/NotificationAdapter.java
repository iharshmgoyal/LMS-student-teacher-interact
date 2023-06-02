package com.codinggeekers.lms.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codinggeekers.lms.CommentActivity;
import com.codinggeekers.lms.Models.Notification;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.NotificationSampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.ArrayList;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.viewHolder>{

    ArrayList<Notification> list;
    Context context;

    public NotificationAdapter(ArrayList<Notification> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_sample,parent,false);

        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {

        Notification notification = list.get(position);

        String type = notification.getType();

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(notification.getNotificationBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User user = snapshot.getValue(User.class);
                        Log.d("TAG", "onBindViewHolder: Notification By : " + notification.getNotificationBy());
                        Picasso.get().load(user.getProfilePhoto()).placeholder(R.drawable.avatar).into(holder.binding.profileImage);
                        if (type.equals("Like")) {
                            holder.binding.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>" + " liked your post."));
                        }
                        else  if (type.equals("Comment")) {
                            holder.binding.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>" + " commented on your post."));
                        }
                        else {
                            holder.binding.notification.setText(Html.fromHtml("<b>"+user.getName()+"</b>" + " started following you."));
                        }
                        String timeAgo = TimeAgo.using(notification.getNotificationAt());
                        holder.binding.time.setText(timeAgo);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        holder.binding.openNotification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!type.equals("Follow")){
                    FirebaseDatabase.getInstance().getReference()
                            .child("Notification")
                            .child(notification.getPostedBy())
                            .child(notification.getNotificationId())
                            .child("checkOpen").setValue(true);
                    holder.binding.openNotification.setBackgroundColor(Color.parseColor("#FFFFFF"));
                    Intent intent = new Intent(context, CommentActivity.class);
                    intent.putExtra("postId", notification.getPostId());
                    intent.putExtra("postedBy", notification.getPostedBy());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
                else {

                }
            }
        });
        Boolean checkOpen = notification.isCheckOpen();
        if (checkOpen.equals(true)) {
            holder.binding.openNotification.setBackgroundColor(Color.parseColor("#000000"));
        } else {}
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class viewHolder extends RecyclerView.ViewHolder {

        NotificationSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);
            binding = NotificationSampleBinding.bind(itemView);

        }
    }
}
