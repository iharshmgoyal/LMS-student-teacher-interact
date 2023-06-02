package com.codinggeekers.lms.Fragments;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.codinggeekers.lms.Models.Post;
import com.codinggeekers.lms.Models.User;
import com.codinggeekers.lms.R;
import com.codinggeekers.lms.databinding.FragmentAddPostBinding;
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

import java.util.Date;
import java.util.Objects;

public class AddPostFragment extends Fragment {

    FragmentAddPostBinding binding;

    FirebaseAuth auth;
    FirebaseDatabase database;
    FirebaseStorage storage;

    ProgressDialog dialog;


    Uri uri;

    public AddPostFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        dialog = new ProgressDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding =  FragmentAddPostBinding.inflate(inflater, container, false);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Post Uploading...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);

        database.getReference().child("Users").child(auth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()) {
                            User users = snapshot.getValue(User.class);
                            Picasso.get().load(users.getProfilePhoto()).placeholder(R.drawable.avatar).into(binding.profileImage);

                            binding.name.setText(users.getName());
                            binding.university.setText(users.getUniversity());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


        binding.postDescription.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String description = binding.postDescription.getText().toString();
                if(!description.isEmpty()) {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn_bg));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white));
                    binding.postBtn.setEnabled(true);
                }
                else {
                    binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_active_btn));
                    binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.gray));
                    binding.postBtn.setEnabled(false);
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        binding.addImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, 10);
            }
        });

        binding.postBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
                final StorageReference reference = storage.getReference().child("Posts").child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid())).child(new Date().getTime()+"");
                reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Post post = new Post();
                                post.setPostImg(uri.toString());
                                post.setPostedBy(FirebaseAuth.getInstance().getUid());
                                post.setPostedByName(binding.name.getText().toString());
                                post.setPostDescription(binding.postDescription.getText().toString());
                                post.setPostedAt(new Date().getTime());

                                database.getReference().child("Posts")
                                        .push()
                                        .setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        dialog.dismiss();
//                                        Toast.makeText(getContext(), "Posted Successfully!!", Toast.LENGTH_SHORT).show();

                                        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                                                getContext()
                                        )
                                                .setSmallIcon(R.drawable.ic_notification_logo)
                                                .setContentTitle("Post Uploaded successfully")
                                                .setContentText("Your new post is uploaded successfully, check here.")
                                                .setAutoCancel(true);

                                        Intent intent = new Intent(getContext(), HomeFragment.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

                                        PendingIntent pendingIntent = PendingIntent.getActivity(getContext(),
                                                0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                                        builder.setContentIntent(pendingIntent);

                                        binding.postImage.setVisibility(View.GONE);
                                        binding.postDescription.setText("");
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(data.getData()!=null) {
            uri = data.getData();
//            binding.postImage.setImageURI(uri);
            Glide.with(getContext()).load(uri).into(binding.postImage);
            binding.postImage.setVisibility(View.VISIBLE);

            binding.postBtn.setBackgroundDrawable(ContextCompat.getDrawable(getContext(), R.drawable.follow_btn_bg));
            binding.postBtn.setTextColor(getContext().getResources().getColor(R.color.white));
            binding.postBtn.setEnabled(true);
        }
    }
}