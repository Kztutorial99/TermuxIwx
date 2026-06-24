package com.kztutorial.termuxiwx.utils;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

public class APKDownloader {

    public interface DownloadCallback {
        void onComplete(long downloadId);
        void onFailed();
    }

    public static long download(Context context, String url, String version,
                                DownloadCallback callback) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("TermuxIwx " + version);
        request.setDescription("Mengunduh update...");
        request.setNotificationVisibility(
                DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(
                Environment.DIRECTORY_DOWNLOADS,
                "TermuxIwx-" + version + ".apk");
        request.setMimeType("application/vnd.android.package-archive");
        request.allowScanningByMediaScanner();

        DownloadManager dm = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = dm.enqueue(request);

        BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context ctx, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id != downloadId) return;

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                android.database.Cursor cursor = dm.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int statusCol = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int status = cursor.getInt(statusCol);
                    cursor.close();
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        callback.onComplete(downloadId);
                    } else {
                        callback.onFailed();
                    }
                }
                try { context.unregisterReceiver(this); } catch (Exception ignored) {}
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_NOT_EXPORTED);
        } else {
            context.registerReceiver(receiver,
                    new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
        }

        return downloadId;
    }

    public static void installFromDownloadManager(Context context, DownloadManager dm,
                                                  long downloadId) {
        Uri apkUri = dm.getUriForDownloadedFile(downloadId);
        if (apkUri == null) return;
        Intent install = new Intent(Intent.ACTION_VIEW);
        install.setDataAndType(apkUri, "application/vnd.android.package-archive");
        install.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        context.startActivity(install);
    }
}
