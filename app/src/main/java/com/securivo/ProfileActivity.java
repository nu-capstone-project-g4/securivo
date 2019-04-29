package com.securivo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = firebaseAuth.getCurrentUser();
        EditText editText = findViewById(R.id.editTextUID);
        try {
            editText.setText(user.getUid());
        } catch (Exception e) {
            Log.d("PROFILE", e.getMessage());
        }
        editText.setInputType(InputType.TYPE_NULL);
        editText.setTextIsSelectable(true);
        editText.setKeyListener(null);
    }
}
