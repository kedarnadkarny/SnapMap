package main.snapmap;

import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContactsActivity extends AppCompatActivity {

    private ListView listView;
    private String namecsv = "";
    private String phonecsv = "";
    private String namearray[];
    private String phonearray[];
    Map<String, String> contacts;
    private Button btnRefresh;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);
        btnRefresh = (Button) findViewById(R.id.btnRefresh);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        listView = (ListView) findViewById(R.id.listView);

        String user_id = mAuth.getCurrentUser().getUid();

        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rf = rootRef.child("Users").child(user_id).child("friends");
        final DatabaseReference status = rootRef.child("Users").child(user_id);

        //initializeContacts();
        status.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild("friends")) {
                    initializeContacts();
                    status.child("contactsinitialized").setValue("yes");
                }
                else {
                    //loadContact();
                    getFirebaseContacts();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshContacts();
            }
        });
    }

    private void loadListView() {
        //Create Array Adapter and pass ArrayOfVales to it
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, namearray);

        //BindAdapter with our Actual ListView
        listView.setAdapter(adapter);

        //click on ListView
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String phone = phonearray[i];
                Intent intent = new Intent(ContactsActivity.this, ContactSingleActivity.class);
                intent.putExtra("phone", phone);
                startActivity(intent);
            }
        });
    }

    // Get phone contacts from Firebase
    private void getFirebaseContacts() {
        contacts = new HashMap<String, String>();
        String user_id = mAuth.getCurrentUser().getUid();

        final DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rf = rootRef.child("Users").child(user_id).child("friends");
        rf.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot snap: dataSnapshot.getChildren()) {
                    String user_id = snap.getKey();
                    String name = ((Map.Entry)((java.util.HashMap)snap.getValue()).entrySet().toArray()[1]).getValue().toString();
                    final String phone =((Map.Entry)((java.util.HashMap)snap.getValue()).entrySet().toArray()[0]).getValue().toString();
                    contacts.put(name, phone);
                    loopy(phone);
                }
                //Sort ordering of Map
                Map<String, String> oContacts = new TreeMap<String, String>(contacts);

                for(Map.Entry<String, String> entry:oContacts.entrySet()) {
                    if(entry.getKey()!=null) {
                        namecsv += entry.getKey() + ",";
                        phonecsv += entry.getValue() + ",";
                    }
                }
                //Convert csvString into array
                namearray = namecsv.split(",");
                phonearray = phonecsv.split(",");
                loadListView();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loopy(final String phone) {
        DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference rm = rootRef.child("Users");
        com.google.firebase.database.Query query = rm.orderByChild("phone").equalTo(phone);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.v("username", phone + " " + String.valueOf(dataSnapshot.getChildren()));
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    //Get phone contacts from phone
    private void loadContact() {
        contacts = new HashMap<String, String>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            //Read Contact Name
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //Read Contact Number
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contacts.put(name,phoneNumber);
        }
        phones.close();

        //Sort ordering of Map
        Map<String, String> oContacts = new TreeMap<String, String>(contacts);

        for(Map.Entry<String, String> entry:oContacts.entrySet()) {
            if(entry.getKey()!=null) {
                namecsv += entry.getKey() + ",";
                phonecsv += entry.getValue() + ",";
            }
        }
        //Convert csvString into array
        namearray = namecsv.split(",");
        phonearray = phonecsv.split(",");
        loadListView();
    }

    // To update phone contacts on Firebase
    private void refreshContacts() {
        namecsv = "";
        phonecsv = "";
        Arrays.fill(namearray, null);
        Arrays.fill(phonearray, null);
        contacts = new HashMap<String, String>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            //Read Contact Name
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //Read Contact Number
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            contacts.put(name,phoneNumber);
        }
        phones.close();

        //Sort ordering of Map
        Map<String, String> oContacts = new TreeMap<String, String>(contacts);

        String user_id = mAuth.getCurrentUser().getUid();

        DatabaseReference tempRef = mDatabase.child(user_id).child("friends");
        tempRef.setValue(null);

        for(Map.Entry<String, String> entry:oContacts.entrySet()) {
            Contacts c = new Contacts();
            c.setFullname(entry.getKey());
            c.setPhone(entry.getValue());
            DatabaseReference current_user_db = mDatabase.child(user_id).child("friends").push();
            current_user_db.setValue(c);

            if(entry.getKey()!=null) {
                namecsv += entry.getKey() + ",";
                phonecsv += entry.getValue() + ",";
            }
        }
        //Convert csvString into array
        namearray = namecsv.split(",");
        phonearray = phonecsv.split(",");
        loadContact();
    }

    // To push Phone contacts to Firebase
    private void initializeContacts() {
        contacts = new HashMap<String, String>();
        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        while(phones.moveToNext()) {
            //Read Contact Name
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //Read Contact Number
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            String regex = "[^0-9^+]";
            String output = phoneNumber.replaceAll(regex, "");
            contacts.put(name, output);

        }
        phones.close();

        //Sort ordering of Map
        Map<String, String> oContacts = new TreeMap<String, String>(contacts);

        String user_id = mAuth.getCurrentUser().getUid();

        //DatabaseReference current_user_db = mDatabase.child(user_id).child("friends");

        for(Map.Entry<String, String> entry:oContacts.entrySet()) {
            Contacts c = new Contacts();
            c.setFullname(entry.getKey());
            c.setPhone(entry.getValue());
            DatabaseReference current_user_db = mDatabase.child(user_id).child("friends").push();
            current_user_db.setValue(c);

            //current_user_db.push().setValue(entry);
            if(entry.getKey()!=null) {
                namecsv += entry.getKey() + ",";
                phonecsv += entry.getValue() + ",";
            }
        }
        //Convert csvString into array
        namearray = namecsv.split(",");
        phonearray = phonecsv.split(",");
        loadListView();
    }
}