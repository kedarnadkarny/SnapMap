package com.map.snap.snapmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String TAG = "status";
    private EditText etsEmail, etsPass;
    private Button btnLogin, btnGoRegister;
    ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }

            }
        };

        etsEmail = (EditText) findViewById(R.id.etsEmail);
        etsPass = (EditText) findViewById(R.id.etsPass);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnGoRegister = (Button) findViewById(R.id.btnGoRegister);
        mProgress = new ProgressDialog(this);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setMessage("Signing in...");
                mProgress.show();
                String email = etsEmail.getText().toString().trim();
                String password  = etsPass.getText().toString().trim();
                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if(task.isSuccessful()) {
                                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                                }
                                else {
                                    Log.w(TAG, "signInWithEmail:failed", task.getException());
                                    Toast.makeText(LoginActivity.this, "Failed!",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                mProgress.dismiss();
            }

        });

        btnGoRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

        Typeface Roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        btnLogin.setTypeface(Roboto);
        btnGoRegister.setTypeface(Roboto);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
