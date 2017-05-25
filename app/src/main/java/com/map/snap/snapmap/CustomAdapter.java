package com.map.snap.snapmap;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CustomAdapter extends ArrayAdapter<DataItem> {

    Context context;
    int layoutResourceId;
    List<DataItem> data = null;
    DatabaseReference mDatabase;
    FirebaseAuth mAuth;

    public CustomAdapter(Context context, int resource, List<DataItem> objects) {
        super(context, resource, objects);

        this.layoutResourceId = resource;
        this.context = context;
        this.data = objects;
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getCurrentUser().getUid()).child("myPlaces");
    }

    static class DataHolder {
        TextView tvPlace;
        ImageButton btnSharePlace;
        ImageButton btnEditPlace;
        ImageButton btnDeletePlace;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        DataHolder holder = null;

        if(convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();

            convertView = inflater.inflate(layoutResourceId, parent, false);

            holder = new DataHolder();
            holder.tvPlace = (TextView) convertView.findViewById(R.id.tvPlace);
            holder.btnSharePlace = (ImageButton) convertView.findViewById(R.id.btnShare);
            holder.btnEditPlace = (ImageButton) convertView.findViewById(R.id.btnEditPlace);
            holder.btnDeletePlace = (ImageButton) convertView.findViewById(R.id.btnDeletePlace);

            holder.btnSharePlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("select", ""+data.get(position).placeID);
                    mDatabase.child(data.get(position).placeID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            dataSnapshot.getValue();
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("text/plain");
                            String shareBody = "Your body here";
                            String shareSub = "http://maps.google.com/?ll="+dataSnapshot.child("lat").getValue()+"," + dataSnapshot.child("lon").getValue();
                            intent.putExtra(Intent.EXTRA_SUBJECT, shareBody);
                            intent.putExtra(Intent.EXTRA_TEXT, shareSub);
                            context.startActivity(Intent.createChooser(intent, "Share using"));
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            });

            holder.btnEditPlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("edit", ""+data.get(position).placeID);
                    Intent intent = new Intent(getContext(), AddPlaceActivity.class);
                    intent.putExtra("placeKey", data.get(position).placeID);
                    getContext().startActivity(intent);
                }
            });

            holder.btnDeletePlace.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("delete", ""+data.get(position).placeID);
                    mDatabase.child(data.get(position).placeID).removeValue();
                }
            });

            convertView.setTag(holder);
        }
        else {
            holder = (DataHolder) convertView.getTag();
        }

        DataItem dataItem = data.get(position);
        holder.tvPlace.setText(dataItem.placeName);

        return convertView;
    }
}
