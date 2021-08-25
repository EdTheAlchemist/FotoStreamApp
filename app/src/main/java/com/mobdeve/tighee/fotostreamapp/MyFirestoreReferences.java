package com.mobdeve.tighee.fotostreamapp;

import android.net.Uri;
import android.widget.ImageView;

import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.Date;

/* At the last minute, I decided to utilize a singleton pattern for all Firebase references. I
 * didn't have to because Firestore.getInstance() already utilizes a singleton pattern, but for some
 * reason I felt everything would look cleaner when a lot of the calls come from this helper class.
 *
 * This call contains both DB references (field / collection names) and useful methods used
 * throughout the entire application.
 * */

public class MyFirestoreReferences {
    // All our instances of Firestore and Storage
    private static FirebaseFirestore firebaseFirestoreInstance = null;
    private static StorageReference storageReferenceInstance = null;
    private static CollectionReference usersRef = null;
    private static CollectionReference postsRef = null;

    // Collection and document names
    public final static String
        USERS_COLLECTION = "User",
        POST_COLLECTION = "Post",

        USERNAME_FIELD = "username",
        USER_REF_FIELD = "userRef",
        CAPTION_FIELD = "caption",
        LOCATION_FIELD = "location",
        IMAGE_URI_FIELD = "imageUri",
        TIMESTAMP_FIELD = "timestamp";

    public static FirebaseFirestore getFirestoreInstance() {
        if(firebaseFirestoreInstance == null) {
            firebaseFirestoreInstance = FirebaseFirestore.getInstance();
        }
        return firebaseFirestoreInstance;
    }

    public static StorageReference getStorageReferenceInstance() {
        if (storageReferenceInstance == null) {
            storageReferenceInstance = FirebaseStorage.getInstance().getReference();
        }
        return storageReferenceInstance;
    }

    public static CollectionReference getUserCollectionReference() {
        if(usersRef == null) {
            usersRef = getFirestoreInstance().collection(USERS_COLLECTION);
        }
        return usersRef;
    }

    public static CollectionReference getPostCollectionReference() {
        if(postsRef == null) {
            postsRef = getFirestoreInstance().collection(POST_COLLECTION);
        }
        return postsRef;
    }

    public static DocumentReference getUserDocumentReference(String stringRef) {
        return getUserCollectionReference().document(stringRef);
    }

    public static DocumentReference getPostDocumentReference(String stringRef) {
        return getPostCollectionReference().document(stringRef);
    }

    /* As an image download + inserting into an ImageView is done in both the ViewHolder and the
     * PostActivity, I decided to centralize the logic to this class.
     *
     * The method takes in a Post object and an ImageView. It handles (1) identifying the path of
     * the image, (2) downloading the image, and (3) inserting it into the supplied ImageView.
     * */
    public static void downloadImageIntoImageView(Post p, ImageView iv) {
        String path = "images/" + p.getUserRef().getId() + "-" + Uri.parse(p.getImageUri()).getLastPathSegment();

        getStorageReferenceInstance().child(path).getDownloadUrl()
            .addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(Task<Uri> task) {
                    CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable(iv.getContext());
                    circularProgressDrawable.setCenterRadius(30);
                    Picasso.get()
                        .load(task.getResult())
                        .error(R.drawable.ic_error_foreground)
                        .placeholder(circularProgressDrawable)
                        .into(iv);
                }
            });
    }

    /* This method is actually used only in one place -- the AddPostActivity. However, I thought
     * that abstracting the "nitty-gritty" look of the string helps in readability.
     * */
    public static String generateNewImagePath(DocumentReference userRef, Uri imageUri) {
        return "images/" + userRef.getId() + "-" + imageUri.getLastPathSegment();
    }
}

