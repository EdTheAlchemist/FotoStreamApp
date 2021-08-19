package com.mobdeve.tighee.fotostreamapp;

import com.google.firebase.firestore.DocumentId;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.ServerTimestamp;

import java.util.Date;

public class Post {
    @DocumentId
    private DocumentReference postId;

    // Attributes
    private DocumentReference userRef;
    private String caption;
    private String location;
    private String imageUri;
    private @ServerTimestamp Date timestamp;

    // Default blank constructor for Firebase
    public Post() {

    }

    public Post(DocumentReference userRef, String caption, String location, String imageUri) {
        this.userRef = userRef;
        this.imageUri = imageUri;
        this.setCaption(caption);
        this.setLocation(location);
    }

    public DocumentReference getPostId() {
        return postId;
    }

    public void setPostId(DocumentReference postId) {
        this.postId = postId;
    }

    public DocumentReference getUserRef() {
        return userRef;
    }

    public void setUserRef(DocumentReference userId) {
        this.userRef = userId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        if(caption == null || caption.equals(""))
            this.caption = null;
        else
            this.caption = location;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        if(location == null || location.equals(""))
            this.location = null;
        else
            this.location = location;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
