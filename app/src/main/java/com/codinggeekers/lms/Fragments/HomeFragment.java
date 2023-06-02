package com.codinggeekers.lms.Fragments;

import android.app.ProgressDialog;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.codinggeekers.lms.Adapter.PostAdapter;
import com.codinggeekers.lms.Adapter.StoryAdapter;
import com.codinggeekers.lms.Models.Post;
import com.codinggeekers.lms.Models.Story;
import com.codinggeekers.lms.Models.UserStories;
import com.codinggeekers.lms.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class HomeFragment extends Fragment {

    //    RecyclerView ;
    RecyclerView storyRv, dashboardRv;
    ArrayList<Story> storyList;
    ArrayList<Post> postList;
    FirebaseDatabase database;
    FirebaseAuth auth;
    FirebaseStorage storage;
    CircleImageView addStoryImage;
    ActivityResultLauncher<String> galleryLauncher;
    ProgressDialog dialog;
    ImageView directMessage;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dialog = new ProgressDialog(getContext());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_home, container, false);

        dashboardRv = view.findViewById(R.id.dashboardRV);
//        dashboardRv.showShimmerAdapter();

        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        storage = FirebaseStorage.getInstance();

        storyRv = view.findViewById(R.id.storyRV);

        storyList = new ArrayList<>();

        StoryAdapter adapter = new StoryAdapter(storyList, getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        storyRv.setLayoutManager(linearLayoutManager);
        storyRv.setNestedScrollingEnabled(false);
        storyRv.setAdapter(adapter);

        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setTitle("Uploading...");
        dialog.setMessage("Please wait...");
        dialog.setCancelable(false);

        database.getReference()
                .child("Stories")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            storyList.clear();
                            for (DataSnapshot storySnapshot : snapshot.getChildren()) {
                                Story story = new Story();
                                story.setStoryBy(storySnapshot.getKey());
                                story.setStoryAt(storySnapshot.child("postedBy").getValue(Long.class));

                                ArrayList<UserStories> stories = new ArrayList<>();
                                for (DataSnapshot snapshot1 : storySnapshot.child("userStories").getChildren()) {
                                    UserStories userStories = snapshot1.getValue(UserStories.class);
                                    stories.add(userStories);
                                }
                                story.setStories(stories);
                                storyList.add(story);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


//        Dashboard
        postList = new ArrayList<>();
        PostAdapter postAdapter = new PostAdapter(postList, getContext());

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        dashboardRv.setLayoutManager(layoutManager);
        dashboardRv.setNestedScrollingEnabled(false);


        database.getReference().child("Posts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Post post = dataSnapshot.getValue(Post.class);
                    post.setPostId(dataSnapshot.getKey());
                    postList.add(post);
                }
                dashboardRv.setAdapter(postAdapter);
//                dashboardRv.hideShimmerAdapter();
                postAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        addStoryImage = view.findViewById(R.id.addStoryImage);
        addStoryImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                galleryLauncher.launch("image/jpg");
            }
        });

        galleryLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                addStoryImage.setImageURI(result);

                dialog.show();

                final StorageReference reference = storage.getReference().child("Stories")
                        .child(Objects.requireNonNull(FirebaseAuth.getInstance().getUid()))
                        .child(new Date().getTime()+"");
                reference.putFile(result).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        reference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override

                            public void onSuccess(Uri uri) {
                                Story story = new Story();
                                story.setStoryAt(new Date().getTime());
                                database.getReference()
                                        .child("Stories")
                                        .child(FirebaseAuth.getInstance().getUid())
                                        .child("postedBy")
                                        .setValue(story.getStoryAt()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        UserStories stories = new UserStories(uri.toString(), story.getStoryAt());
                                        database.getReference().child("Stories")
                                                .child(FirebaseAuth.getInstance().getUid())
                                                .child("userStories")
                                                .push()
                                                .setValue(stories).addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void unused) {
                                                dialog.dismiss();
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });

            }
        });

        return view;
    }
}