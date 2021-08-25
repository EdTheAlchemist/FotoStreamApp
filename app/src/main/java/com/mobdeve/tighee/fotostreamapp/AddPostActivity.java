package com.mobdeve.tighee.fotostreamapp;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;

public class AddPostActivity extends AppCompatActivity {

    // Views needed for the activity
    private EditText locationEtv, captionEtv;
    private ImageView tempImageIv;
    private Button selectBtn, addBtn;

    // A variable that holds the temporary image path selected by the user.
    private Uri imageUri = null;

    /* Handles the return intent from the image selector. Stores the imageUri and extracts the image
     * and inserts it into the ImageView.
     * */
    private ActivityResultLauncher<Intent> myActivityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == Activity.RESULT_OK){
                    try {
                        if(result.getData() != null) {
                            imageUri = result.getData().getData();
                            Picasso.get().load(imageUri).into(tempImageIv);
                        }
                    } catch(Exception exception){
                        Log.d("TAG",""+exception.getLocalizedMessage());
                    }
                }
            }
        });

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
        setContentView(R.layout.activity_add_post);

        // Extracts the user ID sent from the adapter / ViewHolder
        String userIdString = getIntent().getStringExtra(IntentKeys.USER_ID_KEY.name());

        // View initialization
        this.locationEtv = findViewById(R.id.locationEtv);
        this.captionEtv = findViewById(R.id.captionEtv);
        this.tempImageIv = findViewById(R.id.tempImageIv);
        this.selectBtn = findViewById(R.id.selectBtn);
        this.addBtn = findViewById(R.id.addBtn);

        // Reference to the User document of the person who uploaded this Post.
        DocumentReference userRef = MyFirestoreReferences.getUserDocumentReference(userIdString);

        // Logic for selecting an image from the image picker
        this.selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_OPEN_DOCUMENT);
                myActivityResultLauncher.launch(Intent.createChooser(i, "Select Picture"));
            }
        });

        /* There's a lot here, but basically it handles the (1) uploading of the image and (2) the
         * uploading of the Post information.
         * */
        this.addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageUri != null) {
                    // This is a prompt for the user to know the status of the image upload
                    final ProgressDialog progressDialog = new ProgressDialog(AddPostActivity.this);
                    progressDialog.setTitle("Uploading");
                    progressDialog.show();

                    // Readying the Post object for adding to the Post collection.
                    Post p = new Post(
                            userRef,
                            captionEtv.getText().toString(),
                            locationEtv.getText().toString(),
                            imageUri.toString()
                    );

                    // Reference of the image in Firebase Storage
                    StorageReference imageRef = MyFirestoreReferences.getStorageReferenceInstance()
                            .child(MyFirestoreReferences.generateNewImagePath(userRef, imageUri));
                    // Post collection reference
                    CollectionReference postsRef = MyFirestoreReferences.getPostCollectionReference();

                    /* t1 involves uploading the image to the Firebase Storage. We add an
                     * OnProgressListener so we can send updates to the user regarding how much
                     * the image upload has progressed. t1 is an UploadTask and OnProgressListener
                     * is unique to UploadTasks.
                     * */
                    Task t1 = imageRef.putFile(imageUri)
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                progressDialog.setCanceledOnTouchOutside(false);
                                progressDialog.setMessage("Uploaded  " + (int) progress + "%");
                            }
                        });
                    // t2 handles adding the Post to the Post collection
                    Task t2 = postsRef.add(p);

                    /* I originally wanted to merge t1 and t2 completely in a whenAllSuccess;
                     * however, as t1 is an UploadTask and only UploadTasks have an
                     * OnProgressListener, we can't add an onProgressListener to the grouped tasks.
                     * Hence why we added in the listener earlier and then added the OnSuccess and
                     * OnFailure only here. This way, we reduce the number of Listeners we need to
                     * declare.
                     * */
                    Tasks.whenAllSuccess(t1, t2)
                        .addOnSuccessListener(new OnSuccessListener<List<Object>>() {
                            @Override
                            public void onSuccess(List<Object> objects) {
                                progressDialog.setCanceledOnTouchOutside(true);
                                progressDialog.setMessage("Success!");

                                // If both tasks are successful, we finish this activity and return
                                // to the ImageStreamActivity
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                progressDialog.setCanceledOnTouchOutside(true);
                                progressDialog.setMessage("Error occurred. Please try again.");
                            }
                        });
                } else {
                    // Error message when no image was selected. We need at least an image to post.
                    Toast.makeText(
                            AddPostActivity.this,
                            "Please supply an image to post.",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }
        });
    }
}