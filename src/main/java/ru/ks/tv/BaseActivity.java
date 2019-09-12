package ru.ks.tv;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {

    protected Context mContext = this;
    protected View mDecorView;
    protected DevicePolicyManager mDpm;

    protected void setUpAdmin(boolean autoplay) {
        if (!KioskModeApp.isInLockMode()) {
            ComponentName deviceAdmin = new ComponentName(this, AdminReceiver.class);
            mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (!mDpm.isAdminActive(deviceAdmin)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,deviceAdmin);
                ComponentName mDPM = new ComponentName(this, AdminReceiver.class);
                String[] packages = {this.getPackageName()};
                startActivityForResult(intent,1);
            }

            if (mDpm.isDeviceOwnerApp(getPackageName())) {
                mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});
            } else {
                Log.e("Kiosk Mode Error", getString(R.string.not_device_owner));
                try {
                    Runtime.getRuntime().exec("dpm set-device-owner ru.ks.tv/.AdminReceiver");
                    if (mDpm.isDeviceOwnerApp(getPackageName())) {
                        mDpm.setLockTaskPackages(deviceAdmin, new String[]{getPackageName()});}
                } catch (Exception e) {
                    Log.e("owner", "device owner not set");
                    e.printStackTrace();
                }
            }


            enableKioskMode(autoplay);
            //TODO : for clear device Owner
//        } else {
//            mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
//            mDpm.clearDeviceOwnerApp(getPackageName());
            //
            //in shell adb shell dpm remove-active-admin ru.ks.tv/.AdminReceiver
            // or Runtime.getRuntime().exec("dpm remove-active-admin ru.ks.tv/.AdminReceiver");
        }

        mDecorView = getWindow().getDecorView();
        hideSystemUI();
    }

    protected void enableKioskMode(boolean enabled) {
        try {
            if (enabled) {
                if (mDpm.isLockTaskPermitted(this.getPackageName())) {
                    KioskModeApp.setIsInLockMode(true);
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Приложение установлено как основное", Toast.LENGTH_SHORT);
                    toast.show();
                    startLockTask();

                } else {
                    KioskModeApp.setIsInLockMode(false);
                    Log.e("Kiosk Mode Error", getString(R.string.kiosk_not_permitted));
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "На устройстве уже настроена учетная запись, невозможно установить приложение как основное", Toast.LENGTH_SHORT);
                    toast.show();
                }
            } else {
                KioskModeApp.setIsInLockMode(false);
                stopLockTask();
                mDpm.clearDeviceOwnerApp(getPackageName());
                Toast toast = Toast.makeText(getApplicationContext(),
                        "Приложение переведено в обычный режим работы", Toast.LENGTH_SHORT);
                toast.show();
            }
        } catch (Exception e) {
            KioskModeApp.setIsInLockMode(false);
            // TODO: Log and handle appropriately
            Log.e("Kiosk Mode Error", e.getMessage());
        }
    }

    protected void hideSystemUI() {
        mDecorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}