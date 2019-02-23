package com.securivo;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
public class AuthenticationActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private static final int RC_SIGN_IN = 123;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authentication);
        progressBar = findViewById(R.id.progress);
        createSignInIntent();
    }
    private void createSignInIntent() {
        List<AuthUI.IdpConfig> providers = Arrays.asList(new AuthUI.IdpConfig.EmailBuilder().build(), new AuthUI.IdpConfig.PhoneBuilder().build(), new AuthUI.IdpConfig.GoogleBuilder().build(), new AuthUI.IdpConfig.AnonymousBuilder().build());
        startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers).setLogo(R.mipmap.logo).setTosAndPrivacyPolicyUrls("https://example.com/terms.html", "https://example.com/privacy.html").build(), RC_SIGN_IN);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        progressBar.setVisibility(View.VISIBLE);
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                    DatabaseReference reference;
                    if (Objects.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "avi.gupta@st.niituniversity.in") || Objects.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "rahul.saha@st.niituniversity.in") || Objects.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "yash.rastogi@st.niituniversity.in") || Objects.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "sahil.goyal@st.niituniversity.in") || Objects.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail(), "rajat.srivastava@st.niituniversity.in")) {
                        reference = FirebaseDatabase.getInstance().getReference().child("admins").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                        if (String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getProviders()).equals("[google.com]")) {
//                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
//                            if (acct != null) {
//                                if (acct.getPhotoUrl() != null) {
//                                    reference.child("user").setValue(acct.getPhotoUrl().toString());
//                                }
//                            }
//                        }
                        reference.child("name").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        reference.child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                                    finish();
                                    Toast.makeText(AuthenticationActivity.this, "Logged In Successfully As Admin", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        reference = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
//                        if (String.valueOf(FirebaseAuth.getInstance().getCurrentUser().getProviders()).equals("[google.com]")) {
//                            GoogleSignInAccount acct = GoogleSignIn.getLastSignedInAccount(getBaseContext());
//                            if (acct != null) {
//                                if (acct.getPhotoUrl() != null) {
//                                    reference.child("user").setValue(acct.getPhotoUrl().toString());
//                                }
//                            }
//                        }
                        reference.child("name").setValue(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());
                        reference.child("email").setValue(FirebaseAuth.getInstance().getCurrentUser().getEmail()).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    startActivity(new Intent(getBaseContext(), ProfileActivity.class));
                                    finish();
                                    Toast.makeText(AuthenticationActivity.this, "Logged In Successfully As User", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }
            }
        }
    }
    private boolean doubleBackToExitPressedOnce = false;
    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finish();
        }
        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Please Click BACK Again To Exit", Toast.LENGTH_SHORT).show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 2000);
    }
}