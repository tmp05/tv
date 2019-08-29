package ru.ks.tv.updater;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import androidx.appcompat.app.AlertDialog;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.ks.tv.MainAuthActivity;

import static ru.ks.tv.MainAuthActivity.ACTION_SHOW_UPDATE_DIALOG;
import static ru.ks.tv.MainAuthActivity.isAppBeingUpdated;

//import androidx.appcompat.app.AlertDialog;


public class UpdateBroadcastReceiver extends BroadcastReceiver {
    public boolean isRegistered;
    private MainAuthActivity activity;
    public void register(MainAuthActivity context, IntentFilter filter) {
        isRegistered = true;
        activity=context;
        LocalBroadcastManager.getInstance(context).registerReceiver(this, new IntentFilter(ACTION_SHOW_UPDATE_DIALOG));//context.registerReceiver(this, filter);
    }

    public boolean unregister(Context context) {
        if (isRegistered) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
            //context.unregisterReceiver(this);  // edited
            isRegistered = false;
            return true;
        }
        return false;
    }

    public void getAppDialog(final MainAuthActivity context)

    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Важное сообщение!")
                .setMessage("Покормите кота!")
                .setCancelable(false)
                .setNegativeButton("ОК, иду на кухню",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        AppUpdate update = intent.getParcelableExtra("update");
        if (update.getStatus() == AppUpdate.UPDATE_AVAILABLE && !isAppBeingUpdated(
                context)) {
                AlertDialog updateDialog = AppUpdateUtil.getAppUpdateDialog(activity, update);
                updateDialog.show();

           }

    }


}
