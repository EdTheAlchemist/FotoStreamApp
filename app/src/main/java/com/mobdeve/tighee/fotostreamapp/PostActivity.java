package com.mobdeve.tighee.fotostreamapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.List;


public class PostActivity extends AppCompatActivity {

    // Usual variables for the views
    private TextView headerUsernameTv, captionUsernameTv, captionTv, datePostedTv, locationTv;
    private LinearLayout captionHll;
    private ImageView postImageIv;

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
        setContentView(R.layout.activity_post);

        // View initialization
        this.headerUsernameTv = findViewById(R.id.headerUsernameTv);
        this.captionUsernameTv = findViewById(R.id.captionUsernameTv);
        this.captionTv = findViewById(R.id.captionTv);
        this.datePostedTv = findViewById(R.id.dateTv);
        this.locationTv = findViewById(R.id.locationTv);
        this.captionHll = findViewById(R.id.captionHll);
        this.postImageIv = findViewById(R.id.imageIv);

        // Retrieve the post ID and the user ID of the ViewHolder that was clicked
        Intent i = getIntent();
        String postRefString = i.getStringExtra(IntentKeys.POST_ID_KEY.name());
        String userRefString = i.getStringExtra(IntentKeys.USER_ID_KEY.name());

        // Create document references of the Post and User
        DocumentReference postRef = MyFirestoreReferences.getPostDocumentReference(postRefString);
        DocumentReference userRef = MyFirestoreReferences.getUserDocumentReference(userRefString);

        /* This is a little different that the usual way of running tasks.
         *
         * First, Tasks.whenAllSuccess() is a method that accepts multiple Tasks. Example of tasks
         * include performing .get() and .add(), among others. Notice the data type of
         * postRef.get() -- it is actually a Task<DocumentSnapshot>. The convention used below is
         * so I can combine both the get() calls of the postRef and the userRef and avoid more lines
         * of code.
         *
         * Next, notice that onSuccess, you're given a list of objects -- these are the results
         * from each task in order of the first parameter to the last. Each element in the list is
         * an Object, but we actually need it to be a DocumentSnapshot so we can utilize the
         * .toObject() method -- hence the typecasting.
         *
         * Please also note that setViews is a method created to reduce the lines of code in the
         * onCreate and make things a little more readable.
         * */
        Tasks.whenAllSuccess(postRef.get(), userRef.get())
            .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                @Override
                public void onSuccess(List<Object> list) {
                    setViews(
                        ((DocumentSnapshot) list.get(0)).toObject(Post.class),
                        ((DocumentSnapshot) list.get(1)).toObject(User.class)
                    );
                }
            });
    }

    /* setViews is a method that accepts both a Post and a User object. It handles inserting the
     * data from these objects into the views of the activity.
     * */
    private void setViews(Post p, User u) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                MyFirestoreReferences.downloadImageIntoImageView(p, postImageIv);

                headerUsernameTv.setText(u.getUsername());
                datePostedTv.setText(p.getTimestamp().toString());

                if(p.getLocation() == null) {
                    locationTv.setVisibility(View.GONE);
                } else {
                    locationTv.setText(p.getLocation());
                }

                if(p.getCaption() == null) {
                    captionHll.setVisibility(View.GONE);
                } else {
                    captionUsernameTv.setText(u.getUsername());
                    captionTv.setText(p.getCaption());
                }
            }
        });
    }
}