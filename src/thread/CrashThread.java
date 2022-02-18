package com.farplace.farpush.thread;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.farplace.farpush.R;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class CrashThread implements Thread.UncaughtExceptionHandler {
    private Thread.UncaughtExceptionHandler defaultHandler;
    private Activity activity = null;

    public CrashThread(Activity activity) {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.activity = activity;
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        long timeMills = System.currentTimeMillis();
        String date = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.CHINA).format(timeMills);
        StringBuilder stringBuilder = new StringBuilder(date);
        stringBuilder.append(":\n");
        try {
            stringBuilder.append("VersionCode:" + activity.getPackageManager().getPackageInfo(activity.getPackageName(), 0).versionName + "\n");
            stringBuilder.append("Phone:" + Build.VERSION.SDK_INT+"\n");
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }
        stringBuilder.append(e.getMessage());
        stringBuilder.append(":\n");
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        e.printStackTrace(printWriter);
        stringBuilder.append(stringWriter.toString());
        startActivity(stringBuilder.toString());
        defaultHandler.uncaughtException(t, e);
//            Intent intent=new Intent();
//            intent.setClass(activity, CrashActivity.class);
//            intent.putExtra("data",stringBuilder.toString());
//            startActivity(intent);


    }

    private void startActivity(String mes) {
        ClipboardManager clipboardManager = (ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clipData = ClipData.newPlainText("FARPUSH", mes);
        clipboardManager.setPrimaryClip(clipData);
        String id = "9";
        Notification notification = new NotificationCompat.Builder(activity, id)
                .setSmallIcon(R.drawable.ic_outline_bug_report_24)
                .setContentTitle("Bug")
                .setContentText(activity.getString(R.string.bug_report_sum_notification))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .build();
        NotificationManager manager = (NotificationManager) activity.getSystemService(Context.NOTIFICATION_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(id, "s", NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(9, notification);
    }
}