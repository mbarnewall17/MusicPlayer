package com.barnewall.matthew.musicplayer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.widget.RemoteViews;

/**
 * Created by Matthew on 4/24/2016.
 */
public class NotificationManagement {
    private static final int NOTIFICATION_ID = 253;
    public static final String EXIT_MUSIC = "EXIT_MUSIC";
    public static final String PAUSE_MUSIC = "PAUSE_MUSIC";

    public static void createNotification(Context context, String packageName, String songName,
                                          String artistName, Bitmap artwork, boolean playing){;
        Notification notification = buildNotification(context,packageName,songName,artistName,artwork, playing);

        ((NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE)).notify(NOTIFICATION_ID, notification);
    }

    public static Notification buildNotification(Context context, String packageName, String songName,
                                                 String artistName, Bitmap artwork, boolean playing){

        int icon = R.drawable.ic_launcher;
        long when = System.currentTimeMillis();
        Notification notification = new Notification.Builder(context)
                .setSmallIcon(icon)
                .setWhen(when)
                .build();

        RemoteViews contentView = new RemoteViews(packageName, R.layout.play_music_notification);
        contentView.setImageViewResource(R.id.image, R.drawable.no_album_art);
        contentView.setTextViewText(R.id.notificationSongTextView, songName);
        contentView.setTextViewText(R.id.notificationArtistTextView, artistName);
        contentView.setImageViewBitmap(R.id.notificationImageView, artwork);

        contentView.setInt(R.id.notificationPauseButton, "setBackgroundColor", android.R.color.black);
        if(playing){
            contentView.setImageViewBitmap(R.id.notificationPauseButton, ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_action_pause)).getBitmap());
        }
        else{
            contentView.setImageViewBitmap(R.id.notificationPauseButton, ((BitmapDrawable) context.getResources().getDrawable(R.drawable.ic_action_play)).getBitmap());

        }

        Intent exitIntent = new Intent(EXIT_MUSIC);
        PendingIntent exitPendingIntent = PendingIntent.getBroadcast(context, 0, exitIntent, 0);
        contentView.setOnClickPendingIntent(R.id.notificationExitButton, exitPendingIntent);

        Intent pauseIntent = new Intent(PAUSE_MUSIC);
        PendingIntent pausePendingIntent = PendingIntent.getBroadcast(context, 0, pauseIntent, 0);
        contentView.setOnClickPendingIntent(R.id.notificationPauseButton, pausePendingIntent);
        notification.bigContentView = contentView;


        Intent notificationIntent = new Intent(context, MainActivity.class);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent contentIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.contentIntent = contentIntent;

        notification.flags |= Notification.FLAG_NO_CLEAR;
        return notification;
    }

    public static void removeNotification(Context context){
        ((NotificationManager)context.getSystemService(context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_ID);
    }
}
