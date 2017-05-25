package com.map.snap.snapmap;

import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private Button btnAddPlace, btnShareCurrent;
    FirebaseAuth mAuth;
    private ListView mPlacesList;
    DatabaseReference mDatabase;
    List<DataItem> lstData;
    TextView tv4;

    private LocationListener listener;
    private LocationManager locationManager;
    private Location current;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        mPlacesList = (ListView) findViewById(R.id.mPlacesList);
        lstData = new ArrayList<>();
        tv4 = (TextView) findViewById(R.id.textView4);

        mAuth = FirebaseAuth.getInstance();
        btnAddPlace = (Button) findViewById(R.id.btnAddPlace);
        btnShareCurrent = (Button) findViewById(R.id.btnShareCurrent);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("myPlaces");

        // Go to AddPlaceActivity
        btnAddPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            startActivity(new Intent(HomeActivity.this, AddPlaceActivity.class));
            }
        });

        // Shares the current location with external application
        btnShareCurrent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(current!=null) {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    String shareBody = "Your body here";
                    String shareSub = "http://maps.google.com/?ll="+current.getLatitude()+"," + current.getLongitude();
                    intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                    intent.putExtra(Intent.EXTRA_TEXT, shareSub);
                    startActivity(Intent.createChooser(intent, "Share using"));
                }
                else
                    Toast.makeText(HomeActivity.this, "Not found current location!", Toast.LENGTH_SHORT).show();
            }
        });

        // Get place names stored in db
        loadMyPlaces();

        if(!RequestPermission()) {
            LocationData();
        }

        Typeface Roboto = Typeface.createFromAsset(getAssets(), "fonts/Roboto-Medium.ttf");
        btnAddPlace.setTypeface(Roboto);
        btnShareCurrent.setTypeface(Roboto);
    }

    private void CheckDNDStatus() {

    }

    private boolean RequestPermission() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
            return true;
        }
        return false;
    }

    private void LocationData() {
        // Listen to changes in current location
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                current = location;
                Log.d("Current Location", location.getLatitude() + " " + location.getLongitude());
                //tv4.setText("Current: " + location.getLatitude() + " " + location.getLongitude());
                if(location!=null) {
                    for(int i=0; i<lstData.size();i++) {
                        if(Boolean.parseBoolean(lstData.get(i).dnd)) {
                            Location loc1 = new Location("");
                            loc1.setLatitude(location.getLatitude());
                            loc1.setLongitude(location.getLongitude());

                            Location loc2 = new Location("");
                            loc2.setLatitude(Double.parseDouble(lstData.get(i).lat));
                            loc2.setLongitude(Double.parseDouble(lstData.get(i).lon));

                            float distanceInMeters = loc1.distanceTo(loc2)/1000;
                            tv4.append("\n" + "1Change: "+ location.getLatitude() + "/" + location.getLongitude() + " Distance: " + distanceInMeters);
                            if(distanceInMeters<1) {
                                tv4.append("\nIn Range");
                                /*NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE);
                                }*/
                            }
                        }
                    }
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

            }
        };
        locationManager = (LocationManager) getApplication().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, listener);
    }

    private void loadMyPlaces() {
        final ProgressDialog progressDialog = new ProgressDialog(HomeActivity.this);
        progressDialog.setTitle("Loading...");
        progressDialog.show();

        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                lstData.clear();
                for(DataSnapshot snap: dataSnapshot.getChildren()) {
                    lstData.add(new DataItem(snap.getKey(), snap.child("name").getValue(String.class), snap.child("lat").getValue(String.class), snap.child("lon").getValue(String.class), snap.child("dnd").getValue(String.class)));
                }
                // Display places in ListView
                loadListView();
                progressDialog.dismiss();
                CheckDNDStatus();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadListView() {
        CustomAdapter adapter = new CustomAdapter(this, R.layout.itemrow, lstData);
        mPlacesList.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        }
        else if (id == R.id.nav_share) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String shareBody = "Install Snap Map";
            String shareSub = "http://www.kedarnadkarny.com/";
            intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
            intent.putExtra(Intent.EXTRA_TEXT, shareSub);
            startActivity(Intent.createChooser(intent, "Spread the word!"));

        } else if (id == R.id.nav_logout) {
            mAuth.signOut();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
