package com.mobdeve.tighee.fotostreamapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginActivity extends AppCompatActivity {

    private final String TAG = "LoginActivity";

    // Views of the activity
    private EditText usernameEtv;
    private Button loginBtn;

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
        setContentView(R.layout.activity_login);

        // Initialization
        this.usernameEtv = findViewById(R.id.usernameEtv);
        this.loginBtn = findViewById(R.id.enterBtn);

        this.loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get the username from the EditText
                String username = usernameEtv.getText().toString();

                /* This statement handles checking if a username is part of the User collection yet.
                 * If no user is present, then a write operation is performed to the DB. Otherwise,
                 * the user is brought to the ChatRoomActivity.
                 * */
                MyFirestoreReferences.getUserCollectionReference()
                    .whereEqualTo(MyFirestoreReferences.USERNAME_FIELD, username)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                QuerySnapshot querySnapshot = task.getResult();
                                if(querySnapshot.isEmpty())
                                    showNewUserDialog(username);
                                else
                                    moveToChatRoomActivity(username, querySnapshot.getDocuments().get(0).getId());
                            }
                        }
                    });
            }
        });
    }

    /* Handles building a dialog prompt that asks the user if they want to proceed with creating an
     * account. This is also where the write operation is performed.
     * */
    private void showNewUserDialog(String username) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This seems to be a new account. Would you like for us to create an account for you?");
        builder.setCancelable(true);

        builder.setPositiveButton(
            "Yes",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    Map<String, Object> data = new HashMap<>();
                    data.put(MyFirestoreReferences.USERNAME_FIELD, username);

                    MyFirestoreReferences.getUserCollectionReference()
                        .add(data)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d(TAG, "DocumentSnapshot written with ID: " + documentReference.getId());

                                moveToChatRoomActivity(username, documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(Exception e) {
                                Log.w(TAG, "Error adding document", e);
                            }
                        });
                }
            });

        builder.setNegativeButton(
            "No",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });

        AlertDialog alert = builder.create();
        alert.show();
    }

    // This method sends both the username and the userId in hopes to reduce the number of DB calls.
    private void moveToChatRoomActivity(String username, String userId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(LoginActivity.this, ImageStreamActivity.class);
                i.putExtra(IntentKeys.USERNAME_KEY.name(), username);
                i.putExtra(IntentKeys.USER_ID_KEY.name(), userId);
                startActivity(i);
            }
        });
    }
}