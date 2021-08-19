package com.mobdeve.tighee.fotostreamapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class ImageStreamActivity extends AppCompatActivity {
    // Views needed
    private ImageButton addBtn;
    private TextView welcomeTv;

    // Views related to the RecyclerView
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private MyAdapter myAdapter;

    // Internal references of the current logged in user
    private String username, userIdString;

    /* For Reference:
     *      Firebase Firestore
     *          User
     *              -> id
     *                  -> username (String)
     *          Post
     *              -> id
     *                  -> userRef (DocumentReference) [from User collection]
     *                  -> caption (String)
     *                  -> location (String)
     *                  -> imageUri (String)
     *      Firebase Storage
     *          -> images/
     *              -> <name of image>
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_stream);

        // Information from the LoginActivity
        this.username = getIntent().getStringExtra(IntentKeys.USERNAME_KEY.name());
        this.userIdString = getIntent().getStringExtra(IntentKeys.USER_ID_KEY.name());

        // View initializaiton
        this.welcomeTv = findViewById(R.id.welcomeTv);
        this.addBtn = findViewById(R.id.addBtn);
        this.recyclerView = findViewById(R.id.recyclerView);
        this.swipeRefreshLayout = findViewById(R.id.swipeLayout);

        // Set the welcoming message
        this.welcomeTv.setText("Welcome, " + username + "!");

        // Sends the user to the AddPostActivity
        this.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ImageStreamActivity.this, AddPostActivity.class);
                i.putExtra(IntentKeys.USER_ID_KEY.name(), userIdString);
                startActivity(i);
            }
        });

        // Sets a grid view for the recyclerview with a 2-item row
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        this.recyclerView.setLayoutManager(gridLayoutManager);

        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);

        /* The swipeRefreshLayout is triggered when the user decided to swipe down. This simulates
         * a "refresh" -- hence the onRefresh() method. We override the onRefresh to add our own
         * logic (updateDataAndAdapter()), as well as animate the swipeRefreshLayout loading cirle.
         * */
        this.swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                updateDataAndAdapter();
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        // When the user comes back, we perform an update.
        updateDataAndAdapter();
    }

    /* This method is responsible for querying all Posts from the Post collection and updating the
     * adapter. This method only contains one statement but helps in improving readability in the
     * onCreate and onStart methods.
     * */
    private void updateDataAndAdapter() {
        MyFirestoreReferences.getPostCollectionReference()
            .orderBy(MyFirestoreReferences.TIMESTAMP_FIELD)
            .get()
            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        ArrayList<Post> p = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult())
                            p.add(document.toObject(Post.class));

                        myAdapter.setData(p);
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }
        );
    }
}