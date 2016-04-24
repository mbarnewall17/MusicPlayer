package com.barnewall.matthew.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.widget.RemoteViews;

/**
 * Created by Matthew on 4/24/2016.
 */
public class NotificationManagement {
    private static final int NOTIFICATION_ID = 253;
    public static final String EXIT_MUSIC = "EXIT_MUSIC";
    public static final String PAUSE_MUSIC = "PAUSE_MUSIC";

    public static void createNotification(Context context, String packageName, String songName,
                                          String artistName, Bitmap artwork){
        int icon = R.drawable.no_album_art;
        long when = System.currentTimeMillis();
        Notification notification = new Notification(icon, artistName + " - " + songName, when);

        NotificationManager mNotificationManager = (NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE);

        RemoteViews contentView = new RemoteViews(packageName, R.layout.play_music_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.no_album_art);
        contentView.setTextViewText(R.id.notificationSongTextView, songName);
        contentView.setTextViewText(R.id.notificationArtistTextView, artistName);
        contentView.setImageViewBitmap(R.id.notificationImageView, artwork);

        Intent exitIntent = new Intent(EXIT_MUSIC);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(context, 0, exitIntent, 0);
        contentView.setOnClickPendingIntent(R.id.notificationExitButton, exitPendingIntent);

        Intent pauseIntent = new Intent(PAUSE_MUSIC);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0);
        contentView.setOnClickPendingIntent(R.id.notificationPauseButton, pausePendingIntent);
        notification.contentView = contentView;


        Intent notificationIntent = new Intent(context, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        notification.flags |= Notification.FLAG_NO_CLEAR;

        mNotificationManager.notify(NOTIFICATION_ID, notification);
    }

    public static void updateNotification(){

    }

    public static void removeNotification(Context context){
        ((NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
