package com.codinggeekers.lms.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.codinggeekers.lms.ChatActivity;
import com.codinggeekers.lms.LoginActivity;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class UsersListAdapter extends RecyclerView.Adapter<UsersListAdapter.ViewHolder>{

    ArrayList<User> list;
    Context context;

    public UsersListAdapter(ArrayList<User> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.sample_show_user, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        User users = list.get(position);
        Picasso.get().load(users.getProfilePhoto()).placeholder(R.drawable.avatar).into(holder.image);
        holder.userName.setText(users.getName());

        FirebaseDatabase.getInstance().getReference().child("Chats")
                .child(FirebaseAuth.getInstance().getUid()+users.getUserID())
                .orderByChild("timestamp")
                .limitToLast(1)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.hasChildren()) {
                            for(DataSnapshot snapshot1 : snapshot.getChildren()){
                                holder.lastMessage.setText(snapshot1.child("message").getValue(String.class));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

        if (FirebaseAuth.getInstance().getUid()!=null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra("userId", users.getUserID());
                    Log.d("TAG", "onClick: ReceiverID : " + users.getUserID());
                    intent.putExtra("profilePic", users.getProfilePhoto());
                    intent.putExtra("userName", users.getName());
                    context.startActivity(intent);
                }
            });
        }
        else {
            context.startActivity(new Intent(context, LoginActivity.class));
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        ImageView image;
        TextView userName, lastMessage, userId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            image = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.userName);
            lastMessage = itemView.findViewById(R.id.lastMessage);
        }
    }

}
