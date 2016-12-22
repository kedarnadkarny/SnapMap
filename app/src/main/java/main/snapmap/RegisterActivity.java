package main.snapmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    EditText etName, etEmail, etPassword;
    Button btnRegister, btnLogin;
    private ProgressDialog mProgress;
    private EditText etCC, etPhone;
    SnapMap s;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        etName = (EditText) findViewById(R.id.etName);
        etEmail = (EditText) findViewById(R.id.etEmail);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etCC = (EditText) findViewById(R.id.etCC);
        etPhone = (EditText) findViewById(R.id.etPhone);

        btnRegister = (Button) findViewById(R.id.btnRegister);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        s = new SnapMap();

        String response = s.checkPreferenceSet(RegisterActivity.this);
        if(!response.equals("error")) {
            startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
        }

        mProgress = new ProgressDialog(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            }
        });
    }

    private void startRegister() {
        final String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        mProgress.setMessage("Signing Up...");
        mProgress.show();
        if(!TextUtils.isEmpty(name) && !TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()) {
                        String user_id = mAuth.getCurrentUser().getUid();
                        DatabaseReference current_user_db = mDatabase.child(user_id);
                        current_user_db.child("firstname").setValue(name);
                        // Check if this works
                        current_user_db.child("phone").setValue(etCC.getText().toString().trim()+etPhone.getText().toString().trim());
                        current_user_db.child("email").setValue(mAuth.getCurrentUser().getEmail());
                        current_user_db.child("image").setValue("default");
                        mProgress.dismiss();
                        SnapMap s = new SnapMap();
                        s.savePreference(user_id, RegisterActivity.this);
                        startActivity(new Intent(RegisterActivity.this, HomeActivity.class));
                    }
                }
            });
        }
    }
}
