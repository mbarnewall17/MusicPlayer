package com.barnewall.matthew.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by Matthew on 8/18/2015.
 */
public class MediaPlayerManager{
    private MediaPlayer                 mediaPlayer;
    private ArrayList<SongListViewItem> queue;
    private SongListViewItem            nowPlaying;
    private int                         nowPlayingPosition;
    private boolean                     startOver;
    private Handler                     handler;
    private Runnable                    r;
    private NewSongListener             listener;
    private boolean                     skip;
    private boolean                     back;
    private boolean                     nowPlayingBoolean;


    public MediaPlayerManager(ArrayList<SongListViewItem> queue, int nowPlayingPosition, NewSongListener listener){
        this.queue              = queue;
        this.nowPlayingPosition = nowPlayingPosition;
        this.nowPlaying         = queue.get(nowPlayingPosition);
        mediaPlayer             = new MediaPlayer();
        this.listener           = listener;
        startOver               = true;
        skip                    = false;
        back                    = false;
        nowPlayingBoolean       = true;

        handler                 = new Handler();
        r                       = new Runnable() {
            @Override
            public void run() {
                startOver = true;
            }
        };

        //Set up the listener
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        loadSong(queue.get(nowPlayingPosition));
    }



    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (MediaPlayerManager.this.nowPlayingPosition != MediaPlayerManager.this.queue.size() - 1) {
                // Indicates a skip and not to do anything here
                if (skip) {
                    skip = !skip;
                } else if (back) {
                    back = !back;
                } else {
                    playNextSong();
                }
            } else {
                mediaPlayer.release();
                // loadSong with null indicates to the PlaybackFragment to remove callbacks to the handler
                if (!skip && !back) {
                    nowPlayingBoolean = false;
                    finished();
                }
            }
        }
    };

    private void playNextSong(){
        nowPlayingPosition++;
        nowPlaying = queue.get(MediaPlayerManager.this.nowPlayingPosition);
        stop();
        loadSong(nowPlaying);
    }

    public void play(){
        if(!nowPlayingBoolean){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            loadSong(nowPlaying);
        }
        else {
            mediaPlayer.start();
        }
    }

    public void stop(){
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    public void pause(){
        mediaPlayer.pause();
    }

    public void back(){
        back = true;
        if(startOver){
            startOver = false;
        }
        else if(!startOver && nowPlayingPosition > 0){
            nowPlayingPosition = nowPlayingPosition - 1;
        }
        handler.removeCallbacks(r);
        handler.postDelayed(r, 250);
        stop();
        nowPlaying = queue.get(nowPlayingPosition);
        loadSong(nowPlaying);
    }

    public void skip(){
        if(nowPlayingPosition != queue.size() - 1){
            skip = true;
            stop();
            nowPlayingPosition++;
            nowPlaying = queue.get(nowPlayingPosition);
            loadSong(nowPlaying);
        }
    }

    public void loadSong(SongListViewItem item){
        try {
            if(item != null) {
                Log.d(GlobalFunctions.TAG, "song loaded from: " + item.getDataLocation());
                mediaPlayer.setDataSource(item.getDataLocation());
                mediaPlayer.prepare();
                mediaPlayer.start();
                nowPlayingBoolean = true;
            }
            listener.loadNewSongInfo(item);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void playNext(ArrayList<SongListViewItem> toAdd){
        ArrayList<SongListViewItem> temp = new ArrayList<SongListViewItem>(nowPlayingPosition);
        for(int i = 0; i <= nowPlayingPosition; i ++){
            temp.add(queue.get(i));
        }
        for(SongListViewItem s: toAdd){
            temp.add(s);
        }
        for(int i = nowPlayingPosition + 1; i < queue.size(); i++){
            temp.add(queue.get(i));
        }
        queue = new ArrayList<SongListViewItem>(temp);
    }

    public void addToQueue(ArrayList<SongListViewItem> toAdd){
        for(SongListViewItem s: toAdd){
            queue.add(s);
        }
    }

    public boolean isPlaying(){
        if(nowPlayingBoolean) {
            return mediaPlayer.isPlaying();
        }
        else{
            return false;
        }
    }

    public SongListViewItem getNowPlaying(){
        return nowPlaying;
    }

    public int getDuration(){
        return mediaPlayer.getDuration();
    }

    public int getCurrentPosition(){
        return mediaPlayer.getCurrentPosition();
    }

    public void seekTo(int position){
        mediaPlayer.seekTo(position);
    }

    public ArrayList<SongListViewItem> getQueue(){
        return queue;
    }

    public void finished(){
        if(listener != null){
            listener.onFinish();
        }
    }

    public boolean getNowPlayingBoolean(){
        return nowPlayingBoolean;
    }

    /*
     * Changes queue to the arraylist of songs passed in.
     * Sets the nowPlayingPosition equal to the position of now playing in the new queue
     * TODO: Create a new listviewitem for this queue, making sure to override compareTo
     *
     * @param newQueue, The arraylist of the new queue
     */
    public void updateNowPlayingPosition(){
        nowPlayingPosition = this.queue.indexOf(nowPlaying);
    }
}
