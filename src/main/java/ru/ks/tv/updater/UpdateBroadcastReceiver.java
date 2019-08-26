package ru.ks.tv.updater;


import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;


import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import ru.ks.tv.MainAuthActivity;
import static ru.ks.tv.MainAuthActivity.ACTION_SHOW_UPDATE_DIALOG;
import static ru.ks.tv.MainAuthActivity.isAppBeingUpdated;


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
