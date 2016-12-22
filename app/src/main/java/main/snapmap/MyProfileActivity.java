package main.snapmap;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MyProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    EditText etFirstName, etLastName, etCountry, etState, etCity, etCC, etPhone;
    String gender = "";
    Button btnUpdate;
    private ProgressDialog mProgress;
    RadioButton rFemale, rMale;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgress = new ProgressDialog(this);

        etFirstName = (EditText) findViewById(R.id.etFirstName);
        etLastName = (EditText) findViewById(R.id.etLastName);
        etCountry = (EditText) findViewById(R.id.etCountry);
        etState = (EditText) findViewById(R.id.etState);
        etCity = (EditText) findViewById(R.id.etCity);
        rFemale = (RadioButton) findViewById(R.id.radio_female);
        rMale = (RadioButton) findViewById(R.id.radio_male);
        etCC = (EditText) findViewById(R.id.etCC);
        etPhone = (EditText) findViewById(R.id.etPhone);

        btnUpdate = (Button) findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateProfile();
            }
        });

        getMyProfileDetails();
    }

    private void getMyProfileDetails() {
        String user_id = mAuth.getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rf = rootRef.child("Users").child(user_id);

        rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("firstname")) {
                    etFirstName.setText(dataSnapshot.child("firstname").getValue(String.class));
                }
                if(dataSnapshot.hasChild("lastname")) {
                    etLastName.setText(dataSnapshot.child("lastname").getValue(String.class));
                }

                if(dataSnapshot.hasChild("country")) {
                    etCountry.setText(dataSnapshot.child("country").getValue(String.class));
                }

                if(dataSnapshot.hasChild("state")) {
                    etState.setText(dataSnapshot.child("state").getValue(String.class));
                }

                if(dataSnapshot.hasChild("city")) {
                    etCity.setText(dataSnapshot.child("city").getValue(String.class));
                }

                if(dataSnapshot.hasChild("gender")) {
                    String sex = dataSnapshot.child("gender").getValue(String.class);
                    if(sex.equals("male")) {
                        rMale.setChecked(true);
                    }
                    else {
                        rFemale.setChecked(true);
                    }
                }
                if(dataSnapshot.hasChild("phone")) {
                    String cc = dataSnapshot.child("phone").getValue(String.class);
                    String phone = dataSnapshot.child("phone").getValue(String.class);
                    phone = phone.substring(Math.max(0, phone.length() - 10), phone.length());
                    cc = cc.substring(0, 2);
                    etCC.setText(cc);
                    etPhone.setText(phone);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void updateProfile() {
        String fname = etFirstName.getText().toString().trim();
        String lname = etLastName.getText().toString().trim();
        String country = etCountry.getText().toString().trim();
        String state = etState.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String phone = etCC.getText().toString().trim()+etPhone.getText().toString().trim();
        if(TextUtils.isEmpty(fname) && TextUtils.isEmpty(lname) && TextUtils.isEmpty(country) && TextUtils.isEmpty(state) && TextUtils.isEmpty(city) && TextUtils.isEmpty(gender)) {
            Toast.makeText(MyProfileActivity.this, "Enter missing fields!", Toast.LENGTH_SHORT).show();
        }
        else {
            mProgress.setMessage("Updating...");
            mProgress.show();
            String user_id = mAuth.getCurrentUser().getUid();
            DatabaseReference current_user_db = mDatabase.child(user_id);
            current_user_db.child("lastname").setValue(lname);
            current_user_db.child("country").setValue(country);
            current_user_db.child("state").setValue(state);
            current_user_db.child("city").setValue(city);
            current_user_db.child("phone").setValue(phone);
            mProgress.dismiss();
            Toast.makeText(MyProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectGender(View v) {
        switch (v.getId())
        {
            case R.id.radio_female:
                gender = "female";
                break;
            case R.id.radio_male:
                gender = "male";
                break;
        }
    }
}
