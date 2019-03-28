package com.securivo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.Console;
import java.io.File;

public class UploadActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FloatingActionButton fab = findViewById(R.id.fabSelectFile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("*/*");
                startActivityForResult(intent, 7);
            }
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Log.d("APP", mStorageRef.toString());

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 7:
                if (resultCode == RESULT_OK) {
                    String[] filePathTemp = data.getData().getPath().split("/");
                    String firebaseDbPath = firebaseUser.getUid()+"/"+filePathTemp[filePathTemp.length-1];
                    mStorageRef.child(firebaseDbPath).putFile(data.getData())
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content
                                    Log.d("APP/UP/SUCCESS", mStorageRef.getDownloadUrl().toString());
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    Snackbar.make(findViewById(android.R.id.content), exception.toString(), Snackbar.LENGTH_LONG)
                                            .setAction("Action", null).show();
                                }
                            });
                }
                break;
        }
    }
}
