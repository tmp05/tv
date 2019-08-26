package ru.ks.tv.updater;

import android.app.DownloadManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;

import androidx.core.content.FileProvider;

import java.io.File;

public class DownloadUpdateService extends Service {

    public static final String DOWNLOAD_UPDATE_TITLE = "Updating KSTV";
    public static final String KEY_DOWNLOAD_URL = "downloadURL";
    private static final String FILE_NAME = "app_new.apk";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {

            String downloadUrl = intent.getStringExtra(KEY_DOWNLOAD_URL);
            String newApkFilePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/" + FILE_NAME;
            final File newApkFile = new File(newApkFilePath);
            final Uri downloadUri = Uri.parse("file://" + newApkFile);
            if (newApkFile.exists()) {
                newApkFile.delete();
            }
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            request.setTitle(DOWNLOAD_UPDATE_TITLE);

            //set destination
            request.setDestinationUri(downloadUri);

            // get download service and enqueue file
            final DownloadManager manager = (DownloadManager) this.getBaseContext().getSystemService(
                    Context.DOWNLOAD_SERVICE);
            final long startedDownloadId = manager.enqueue(request);

            //set BroadcastReceiver to install app when .apk is downloaded
            BroadcastReceiver onComplete = new BroadcastReceiver() {
                public void onReceive(Context ctxt, Intent intent) {
                    long finishedDownloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (startedDownloadId == finishedDownloadId) {

                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(finishedDownloadId);
                        Cursor cursor = manager.query(query);
                        if (cursor.moveToFirst()) {
                            int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                            int status = cursor.getInt(columnIndex);

                            if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                //open the downloaded file
                                Intent install = new Intent(Intent.ACTION_VIEW);
                                install.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                Uri downloadUri2 = downloadUri;
                                if (Build.VERSION.SDK_INT >= 24) {
                                    downloadUri2 = FileProvider.getUriForFile(ctxt,
                                            getApplicationContext().getPackageName() + ".provider",
                                            newApkFile);
                                }
                                install.setDataAndType(downloadUri2,
                                        manager.getMimeTypeForDownloadedFile(startedDownloadId));
                                ctxt.startActivity(install);
                            } else if (status == DownloadManager.STATUS_FAILED) {
                                if (newApkFile.exists()) {
                                    newApkFile.delete();
                                }
                            }
                        } else {
                            //Delete the partially downloaded file
                            if (newApkFile.exists()) {
                                newApkFile.delete();
                            }
                        }

                        ctxt.unregisterReceiver(this);
                        stopSelf();
                    }
                }
            };

            this.registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

