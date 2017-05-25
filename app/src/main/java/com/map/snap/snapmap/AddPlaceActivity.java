package com.map.snap.snapmap;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class AddPlaceActivity extends AppCompatActivity implements OnMapReadyCallback {

    Button btnAddPlace;
    EditText etPlaceName;
    ToggleButton toggleDND;
    private GoogleMap mMap;
    FirebaseAuth mAuth;
    DatabaseReference mDatabase;
    HashMap<String, String> data;
    Boolean isEditMode;
    private boolean status = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_place);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(R.layout.action_bar);

        mAuth = FirebaseAuth.getInstance();
        btnAddPlace = (Button) findViewById(R.id.btnAddPlace);
        toggleDND = (ToggleButton) findViewById(R.id.toggleDND);
        etPlaceName = (EditText) findViewById(R.id.etPlaceName);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("myPlaces");
        data = new HashMap<String, String>();
        isEditMode = false;

        // Save Place details
        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String placeName = etPlaceName.getText().toString().trim();
                if(!TextUtils.isEmpty(placeName) && !data.isEmpty()) {
                    data.put("name", placeName);
                    data.put("dnd", String.valueOf(status));
                    if(isEditMode) { // If editing an existing place
                        mDatabase.child(getIntent().getStringExtra("placeKey")).setValue(data);
                        Toast.makeText(AddPlaceActivity.this, "Place Updated!", Toast.LENGTH_SHORT).show();
                    }
                    else { // Saving a new place
                        mDatabase.push().setValue(data);
                        Toast.makeText(AddPlaceActivity.this, "New Place Added!", Toast.LENGTH_SHORT).show();
                    }
                    startActivity(new Intent(AddPlaceActivity.this, HomeActivity.class));
                }
            }
        });

        toggleDND.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                status = !status;
                Log.d("toggle", String.valueOf(status));
            }
        });

        ((MapFragment) getFragmentManager().findFragmentById(R.id.fragment)).getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;

        // Drop a marker on the selected place and save location in HashMap
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                mMap.clear();
                LatLng current = latLng;
                mMap.addMarker(new MarkerOptions().position(current));
                data.put("lat", String.valueOf(latLng.latitude));
                data.put("lon", String.valueOf(latLng.longitude));
            }
        });

        if(getIntent().hasExtra("placeKey")) {
            isEditMode = true;
            Log.d("placeKey", getIntent().getStringExtra("placeKey"));
            loadPlace();
        }
    }

    // Load details of place in edit mode
    private void loadPlace() {
        mDatabase.child(getIntent().getStringExtra("placeKey")).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                etPlaceName.setText(dataSnapshot.child("name").getValue(String.class));
                LatLng current = new LatLng(Double.parseDouble(dataSnapshot.child("lat").getValue(String.class)), Double.parseDouble(dataSnapshot.child("lon").getValue(String.class)));
                mMap.addMarker(new MarkerOptions().position(current));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
                data.put("lat", dataSnapshot.child("lat").getValue(String.class));
                data.put("lon", dataSnapshot.child("lon").getValue(String.class));
//                data.put("dnd", String.valueOf(status));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
