package com.codinggeekers.lms.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codinggeekers.lms.Models.Comment;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.CommentSampleBinding;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.viewHolder> {

    Context context;
    ArrayList<Comment> list;

    public CommentAdapter(Context context, ArrayList<Comment> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public viewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_sample, parent, false);
        return new viewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull viewHolder holder, int position) {
        Comment comment = list.get(position);
        holder.binding.comment.setText(comment.getCommentBody());

        String timeAgo = TimeAgo.using(comment.getCommentedAt());
        holder.binding.time.setText(timeAgo);

        FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(comment.getCommentedBy())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        User users = snapshot.getValue(User.class);
                        Picasso.get().load(users.getProfilePhoto()).placeholder(R.drawable.avatar).into(holder.binding.profileImage);
                        holder.binding.comment.setText(Html.fromHtml("<b>" + users.getName() + "</b>" + "  " + comment.getCommentBody()));
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

    public class viewHolder extends RecyclerView.ViewHolder{

        CommentSampleBinding binding;

        public viewHolder(@NonNull View itemView) {
            super(itemView);

            binding = CommentSampleBinding.bind(itemView);
        }
    }
}
