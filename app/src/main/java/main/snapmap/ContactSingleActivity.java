package main.snapmap;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class ContactSingleActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    Button btnShareLocation, btnRequestLocation;
    SnapMap s;
    String user_id;
    LatLng location;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Firebase.setAndroidContext(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_single);
        btnShareLocation = (Button) findViewById(R.id.btnShareLocation);
        btnRequestLocation = (Button) findViewById(R.id.btnRequestLocation);
        final String[] receiverID = new String[1];

        Intent intent = getIntent();
        final String phone = intent.getStringExtra("phone");
        s = new SnapMap();

        mAuth = FirebaseAuth.getInstance();
        user_id = mAuth.getCurrentUser().getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").orderByChild("phone").equalTo(phone).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                receiverID[0] = (String) ((Map.Entry)((java.util.HashMap)dataSnapshot.getValue()).entrySet().toArray()[0]).getKey();
                s.setReceiverID((String) ((Map.Entry)((java.util.HashMap)dataSnapshot.getValue()).entrySet().toArray()[0]).getKey());
                Toast.makeText(ContactSingleActivity.this, "ReceiverID: "+ s.getReceiverID() + " Phone: "+phone, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnShareLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
                DatabaseReference rf = rootRef.child("Users").child(user_id).child("friends");
                com.google.firebase.database.Query query = rf.orderByChild("phone").equalTo(phone);
                query.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot snap: dataSnapshot.getChildren()) {
                            DatabaseReference friend = mDatabase.child("Users").child(user_id).child("friends").child(snap.getKey());
                            friend.child("locationaccess").setValue("yes");
                            Toast.makeText(ContactSingleActivity.this, "Permission granted!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        btnRequestLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }
}
