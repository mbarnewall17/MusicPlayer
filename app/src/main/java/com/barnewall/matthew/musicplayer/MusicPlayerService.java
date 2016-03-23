package com.barnewall.matthew.musicplayer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;

public class MusicPlayerService extends Service {
    private MediaPlayerManager manager;

    private IBinder mBinder = new MyBinder();

    public MusicPlayerService() {

    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public MediaPlayerManager startPlaying(ArrayList<SongListViewItem> queue,int position, NewSongListener listener){
        return manager = new MediaPlayerManager(queue, position, listener);
    }

    public class MyBinder extends Binder{
        public MusicPlayerService getService(){
            return MusicPlayerService.this;
        }
    }
}
