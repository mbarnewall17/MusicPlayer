package com.barnewall.matthew.musicplayer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Binder;
import android.os.IBinder;

import com.barnewall.matthew.musicplayer.BroadcastReceivers.HeadsetUnpluggedReceiver;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;

public class MusicPlayerService extends Service {
    private MediaPlayerManager manager;

    // Indicate updating the queue
    public static final String UPDATE_QUEUE = "barnewall.musicplayerservice.update";

    private IBinder mBinder = new MyBinder();

    // Sets then MediaPlayerManager's queue to the one in the extras
    private BroadcastReceiver receiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(UPDATE_QUEUE)){
                manager.updateNowPlayingPosition();
            }
            else if(intent.getAction().equals(NotificationManagement.EXIT_MUSIC)){
                manager.endPlayback();
                NotificationManagement.removeNotification(getApplicationContext());
            }
            else if(intent.getAction().equals(NotificationManagement.TOGGLE_PLAY_MUSIC)){
                if(manager.isPlaying()) {
                    manager.pause();
                }
                else{
                    manager.play();
                }

            }
            else if(intent.getAction().equals(NotificationManagement.BACK_MUSIC))
                manager.back();
            else if(intent.getAction().equals(NotificationManagement.NEXT_MUSIC))
                manager.skip();
        }
    };

    private HeadsetUnpluggedReceiver headsetUnpluggedReceiver;

    public MusicPlayerService() {}

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public MediaPlayerManager startPlaying(ArrayList<SongListViewItem> queue,int position, ControlListener listener){
        if(manager != null && manager.isInValidState()){
            manager.endPlayback();
        }
        return manager = new MediaPlayerManager(queue, position, listener);
    }

    public MediaPlayerManager getManager(){
        return manager;
    }

    public class MyBinder extends Binder{
        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UPDATE_QUEUE);
        intentFilter.addAction(NotificationManagement.EXIT_MUSIC);
        intentFilter.addAction(NotificationManagement.TOGGLE_PLAY_MUSIC);
        intentFilter.addAction(NotificationManagement.BACK_MUSIC);
        intentFilter.addAction(NotificationManagement.NEXT_MUSIC);
        registerReceiver(receiver, intentFilter);

        headsetUnpluggedReceiver = new HeadsetUnpluggedReceiver();
        registerReceiver(headsetUnpluggedReceiver, new IntentFilter(AudioManager.ACTION_HEADSET_PLUG));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receiver);
        unregisterReceiver(headsetUnpluggedReceiver);
    }
}
