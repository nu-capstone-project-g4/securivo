package com.securivo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.securivo.tools.MD5;
import com.securivo.tools.AES;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.List;

public class UploadActivity extends AppCompatActivity {
    private StorageReference mStorageRef;
    private FirebaseUser firebaseUser;
    private EditText editTextMD5;
    String[] filePathTemp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        mStorageRef = FirebaseStorage.getInstance().getReference();
        FloatingActionButton fab = findViewById(R.id.fabSelectFile);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                intent.setType("video/*");
                startActivityForResult(intent, 7);
            }
        });
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        editTextMD5 = findViewById(R.id.editTextMD5);
        editTextMD5.setInputType(InputType.TYPE_NULL);
        editTextMD5.setTextIsSelectable(true);
        editTextMD5.setKeyListener(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 7:
                if (resultCode == RESULT_OK) {
                    filePathTemp = getPath(data.getData()).split("/");
                    Snackbar.make(findViewById(R.id.uploadCoordinator), "Calculating MD5...", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    String fileExt = filePathTemp[filePathTemp.length - 1];
                    fileExt = fileExt.substring(fileExt.lastIndexOf(".") + 1);
                    final String fileName = filePathTemp[filePathTemp.length - 1];
                    final String calculatedMD5 = MD5.calculateMD5(new File(getPath(data.getData())));
                    String firebaseDbPath = firebaseUser.getUid() + "/" + calculatedMD5;
                    byte[] inputData = null;
                    try {
                        InputStream is = getContentResolver().openInputStream(data.getData());
                        inputData = getBytes(is);
                    } catch (Exception e) {
                        Log.e("APP/UP", e.getMessage());
                    }
                    byte[] key = calculatedMD5.getBytes();
                    List<byte[]> encrypted = null;
                    try {
                        encrypted = AES.encodeFile(key, inputData);
                    } catch (Exception e) {
                        Log.e("APP/DOWN/E", e.getMessage());
                    }

                    StorageMetadata storageMetadata = new StorageMetadata.Builder().setCustomMetadata("fileName", fileName).setCustomMetadata("iv", android.util.Base64.encodeToString(encrypted.get(1), android.util.Base64.DEFAULT)).build();

                    if (encrypted != null) {
                        mStorageRef.child(firebaseDbPath).putBytes(encrypted.get(0), storageMetadata)
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        Snackbar.make(findViewById(R.id.uploadCoordinator), "File upload successful!\nMD5: " + calculatedMD5, Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        Log.d("APP/UP/SUCCESS", taskSnapshot.toString());
                                        editTextMD5.setText(calculatedMD5);
                                    }
                                })
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                        float totalMB = Float.valueOf(taskSnapshot.getTotalByteCount()) / 1000000;
                                        float doneMB = Float.valueOf(taskSnapshot.getBytesTransferred()) / 1000000;
                                        DecimalFormat df = new DecimalFormat("#.##");
                                        double progress = doneMB * 100 / totalMB;
                                        Snackbar.make(findViewById(R.id.uploadCoordinator), "Progress: "
                                                + df.format(progress) + "%\t\tUploaded " + df.format(doneMB) +
                                                "MB out of " + df.format(totalMB) + "MB", Snackbar.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        // Handle unsuccessful uploads
                                        Snackbar.make(findViewById(R.id.uploadCoordinator), "Error occurred."
                                                , Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        Log.d("APP/UP/FAIL", exception.toString());
                                    }
                                });
                    } else {
                        Log.e("APP/DOWN", "error error error");
                    }
                }
                break;
        }
    }

    public String getPath(Uri uri) {
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        startManagingCursor(cursor);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }
}
