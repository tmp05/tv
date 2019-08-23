package ru.ks.tv;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class BootReceiver extends BroadcastReceiver {
    boolean pref_autoplay = true;

    @Override
    public void onReceive(Context arg0, Intent arg1) {
        SharedPreferences prefs;
        prefs = PreferenceManager.getDefaultSharedPreferences(arg0);
        pref_autoplay = prefs.getBoolean("autoplay", true);
        if (pref_autoplay) {
            Intent ativivtyIntent = new Intent(arg0, MainAuthActivity.class);
            ativivtyIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            arg0.startActivity(ativivtyIntent);
        }

    }
}
