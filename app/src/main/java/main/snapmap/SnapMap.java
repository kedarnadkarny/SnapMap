package main.snapmap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class SnapMap {
    private static String Uid;
    private static String receiverID;

    public static String getUid() {
        return Uid;
    }

    public static void setUid(String uid) {
        Uid = uid;
    }

    public static String getReceiverID() {
        return receiverID;
    }

    public static void setReceiverID(String receiverID) {
        SnapMap.receiverID = receiverID;
    }

    public void savePreference(String user_id, Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = sp.edit();
        edit.putString("Uid", user_id);
        edit.commit();
    }

    public String checkPreferenceSet(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String Uid = sp.getString("Uid", "error");
        return Uid;
    }

    public void emptyPreference(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove("Uid");
        editor.apply();
    }
}
