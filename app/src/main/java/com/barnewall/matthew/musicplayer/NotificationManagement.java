package com.barnewall.matthew.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.NotificationCompat;

/**
 * Created by Matthew on 4/24/2016.
 */
public class NotificationManagement {
    private static final int NOTIFICATION_ID = 253;
    public static final String EXIT_MUSIC = "EXIT_MUSIC";
    public static final String TOGGLE_PLAY_MUSIC = "TOGGLE_PLAY_MUSIC";
    public static final String BACK_MUSIC = "mbarnewall_BACK_MUSIC";
    public static final String NEXT_MUSIC = "mbarnewall_NEXT_MUSIC";

    public static void createNotification(Context context, String packageName, String songName,
                                          String artistName, Bitmap artwork, boolean playing) {
        Notification notification = buildNotification(context, packageName, songName, artistName, artwork, playing);

        ((NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
    }

    public static Notification buildNotification(Context context, String packageName, String songName,
                                                 String artistName, Bitmap artwork, boolean isPlaying) {

        int icon = R.drawable.ic_launcher;

        PendingIntent prevPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(BACK_MUSIC), 0);
        PendingIntent playPausePendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(TOGGLE_PLAY_MUSIC), 0);
        PendingIntent nextPendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(NEXT_MUSIC), 0);

        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

        return new NotificationCompat.Builder(context)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSmallIcon(icon)
                .addAction(R.drawable.ic_action_previous, "", prevPendingIntent)
                .addAction(isPlaying ? R.drawable.ic_action_pause : R.drawable.ic_action_play, "", playPausePendingIntent)
                .addAction(R.drawable.ic_action_next, "", nextPendingIntent)
                .setContentTitle(songName)
                .setContentText(artistName)
                .setLargeIcon(artwork)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(contentIntent)
                .setOngoing(true)
                .setStyle(new NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(1))
                .build();
    }

    public static void removeNotification(Context context) {
        ((NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
