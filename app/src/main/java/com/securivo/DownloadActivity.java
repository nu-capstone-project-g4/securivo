package com.securivo;

import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.securivo.tools.AES;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

public class DownloadActivity extends AppCompatActivity {
    private FloatingActionButton fabDownload;
    private EditText editTextUIDin;
    private EditText editTextMD5in;
    private String fileName;
    private StorageReference pathReference;
    private File localFile;
    private String fileExt;
    private byte[] iv;
    private String downUID;
    private String downMD5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);
        editTextUIDin = findViewById(R.id.editTextUIDin);
        editTextMD5in = findViewById(R.id.editTextMD5in);
        fabDownload = findViewById(R.id.fabDownloadFile);

        fabDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                localFile = null;
                downUID = editTextUIDin.getText().toString();
                downMD5 = editTextMD5in.getText().toString();
                StorageReference storageReference = FirebaseStorage.getInstance().getReference();
                pathReference = storageReference.child(downUID+"/"+downMD5);

                pathReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
                        @Override
                        public void onSuccess(StorageMetadata storageMetadata) {
                            fileName = storageMetadata.getCustomMetadata("fileName");
                            iv = android.util.Base64.decode(storageMetadata.getCustomMetadata("iv"), android.util.Base64.DEFAULT);
                            fileExt = fileName.substring(fileName.lastIndexOf(".") + 1);
                            fileName = fileName.substring(0, fileName.lastIndexOf(".")-1);
                            File downLocation = new File(getExternalFilesDir(null), "Securivo");
                            Log.e("MKDIR", downLocation.mkdirs() ? "Created: "+downLocation.toString() : "Not: "+downLocation.toString());
                            try {
                                localFile = File.createTempFile(fileName, "." + fileExt, downLocation);
                            } catch (Exception e) {
                                Log.e("APP/DOWN/1", e.getMessage());
                            }
                            pathReference.getFile(localFile).addOnProgressListener(new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
                                @Override
                                public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                    float totalMB = Float.valueOf(taskSnapshot.getTotalByteCount())/1000000;
                                    float doneMB = Float.valueOf(taskSnapshot.getBytesTransferred())/1000000;
                                    DecimalFormat df = new DecimalFormat("#.##");
                                    double progress = doneMB*100/totalMB;
                                    if(progress > 10 && progress < 12 || progress > 25 && progress < 28 || progress > 50 && progress < 53 || progress > 80 && progress < 82) {
                                        Snackbar.make(findViewById(R.id.downloadCoordinator), df.format(progress) + "%, Downloaded " + df.format(doneMB) +
                                                "MB/" + df.format(totalMB) + "MB", Snackbar.LENGTH_LONG).show();
                                    }
                                }
                                }).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                        Snackbar.make(findViewById(R.id.downloadCoordinator), "Download complete, decrypting...", Snackbar.LENGTH_LONG)
                                                .setAction("Action", null).show();
                                        byte[] key = downMD5.getBytes();
                                        try {
                                            FileInputStream fis = new FileInputStream(localFile);
                                            byte fileContent[] = new byte[(int) localFile.length()];
                                            fis.read(fileContent);
                                            byte decrypted[] = AES.decodeFile(key, fileContent, iv);
                                            OutputStream os = new FileOutputStream(localFile);
                                            os.write(decrypted);
                                            os.close();
                                            Snackbar.make(findViewById(R.id.downloadCoordinator), "Download and decryption complete", Snackbar.LENGTH_LONG)
                                                    .setAction("Action", null).show();
                                        } catch (Exception e) {
                                            Log.e("APP/DOWN/BOS", e.getMessage());

                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        Log.e("APP/DOWN/2", exception.getMessage());
                                    }
                            });
                        }
                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Snackbar.make(findViewById(R.id.downloadCoordinator), "Failed retrieving metadata!", Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                                Log.e("APP/DOWN/3", exception.getMessage());
                            }
                });




            }
        });
    }
}
