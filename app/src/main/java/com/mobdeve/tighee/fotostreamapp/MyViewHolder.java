package com.mobdeve.tighee.fotostreamapp;

import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.CircularProgressDrawable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;


public class MyViewHolder extends RecyclerView.ViewHolder {
    private ImageView vhImageIv;

    public MyViewHolder(View itemView) {
        super(itemView);
        this.vhImageIv = itemView.findViewById(R.id.vhImageIv);
    }

    public void bindData(Post p) {
        MyFirestoreReferences.downloadImageIntoImageView(p, this.vhImageIv);
    }
}
