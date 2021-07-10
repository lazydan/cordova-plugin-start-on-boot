package com.tsubik.cordova.start_on_boot;

import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.content.Context;

public class StartOnBootReceiver extends BroadcastReceiver {
    public static final String TAG = "StartOnBootPlugin";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String mainActivityName = getMainActivityName(context);
            Intent serviceIntent = new Intent(context, Class.forName(mainActivityName));
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d(TAG, "Starting " + mainActivityName + " on boot");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                String CHANNEL_ID = "sob_notification_id";
                String CHANNEL_NAME = "sob_notification_name";
                android.support.v4.app.NotificationCompat.Builder builder = new android.support.v4.app.NotificationCompat.Builder(context, CHANNEL_ID);
                builder.setSmallIcon(getAppResource(context, "ic_launcher", "mipmap"));
                builder.setContentTitle(context.getString(getAppResource(context, "app_name", "string")));
                builder.setContentText("点击启动应用");
                builder.setLargeIcon(android.graphics.BitmapFactory.decodeResource(context.getResources(), getAppResource(context, "ic_launcher", "mipmap")));
                builder.setPriority(android.support.v4.app.NotificationCompat.PRIORITY_HIGH);
                builder.setCategory(android.support.v4.app.NotificationCompat.CATEGORY_CALL);

                android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(context, 0, serviceIntent, android.app.PendingIntent.FLAG_UPDATE_CURRENT);

                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);

                builder.setFullScreenIntent(pendingIntent, true);

                android.app.NotificationManager manager = (android.app.NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    android.app.NotificationChannel channel = new android.app.NotificationChannel(CHANNEL_ID, CHANNEL_NAME, android.app.NotificationManager.IMPORTANCE_HIGH);
                    manager.createNotificationChannel(channel);
                }
                manager.notify(115, builder.build());
            } else {
                context.startActivity(serviceIntent);
            }
        } catch (Exception ex) {
            Log.d(TAG, "Cannot start app on boot. Exception" + ex.toString());
        }
    }

    private int getAppResource(Context context, String name, String type) {
        return context.getResources().getIdentifier(name, type, context.getPackageName());
    }


    private String getMainActivityName(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            ActivityInfo[] activities = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES).activities;
            if (activities.length > 0) {
                return activities[0].name;
            }
        } catch (PackageManager.NameNotFoundException e) {
        }
        //return default name
        return context.getPackageName() + ".MainActivity";
    }
}
