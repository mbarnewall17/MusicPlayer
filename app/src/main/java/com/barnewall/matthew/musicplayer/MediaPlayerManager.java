package com.barnewall.matthew.musicplayer;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;
import java.util.Collections;

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
    private ControlListener listener;
    private boolean                     skip;
    private boolean                     back;
    private boolean                     nowPlayingBoolean;
    private boolean                     shuffle;
    private ArrayList<SongListViewItem> alternateQueue;


    public MediaPlayerManager(ArrayList<SongListViewItem> queue, int nowPlayingPosition, ControlListener listener){
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
        shuffle                 = false;


        //Set up the listener
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        loadSong(queue.get(nowPlayingPosition));
        nowPlaying = queue.get(nowPlayingPosition);
    }

    public void setListener(ControlListener listener){
        this.listener = listener;
    }



    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            if (MediaPlayerManager.this.nowPlayingPosition != MediaPlayerManager.this.queue.size() - 1) {
                // Indicates a skip and not to do anything here
                if (back) {
                    back = !back;
                } else {
                    playNextSong();
                }
            } else {
                nowPlaying.setAnimated(false);
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
        stop();
        nowPlayingPosition++;
        nowPlaying = queue.get(nowPlayingPosition);
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
            listener.songPlay();
        }
        nowPlaying.setAnimated(true);
        nowPlayingBoolean = true;
        NotificationManagement.createNotification(listener.getContext(),
                listener.getApplicationName(),
                nowPlaying.getTitle(),
                nowPlaying.getArtistName(),
                GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                        (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                        listener.getContext()), true);
    }

    public void stop(){
        nowPlaying.setAnimated(false);
        mediaPlayer.stop();
        mediaPlayer.reset();
    }

    public void pause(){
        nowPlaying.setAnimated(false);
        mediaPlayer.pause();
        listener.songPause();
        NotificationManagement.createNotification(listener.getContext(),
                listener.getApplicationName(),
                nowPlaying.getTitle(),
                nowPlaying.getArtistName(),
                GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                        (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                        listener.getContext()), false);
    }

    private Runnable r  = new Runnable() {
        @Override
        public void run() {
            startOver = true;
            back = false;
        }
    };

    public void back(){
        back = true;
        if(startOver){
            startOver = false;
        }
        else if(nowPlayingPosition > 0){
            nowPlayingPosition = nowPlayingPosition - 1;
            handler.removeCallbacks(r);
        }
        handler.postDelayed(r,250);
        stop();
        nowPlaying = queue.get(nowPlayingPosition);
        loadSong(nowPlaying);
    }

    public void skip(){
        if(nowPlayingPosition != queue.size() - 1){
            playNextSong();
        }
    }

    public void loadSong(SongListViewItem item){
        item.setAnimated(true);
        try {
            if(item != null) {
                Log.d(GlobalFunctions.TAG, "song loaded from: " + item.getDataLocation());
                mediaPlayer.setDataSource(item.getDataLocation());
                mediaPlayer.prepare();
                mediaPlayer.start();
                nowPlayingBoolean = true;
            }
            NotificationManagement.createNotification(listener.getContext(),
                    listener.getApplicationName(),
                    nowPlaying.getTitle(),
                    nowPlaying.getArtistName(),
                    GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                            (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                            listener.getContext()),true);
            listener.loadNewSongInfo(item);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    public void playNext(ArrayList<SongListViewItem> toAdd){
        Collections.reverse(toAdd);
        for(SongListViewItem item: toAdd){
            queue.add(nowPlayingPosition+1, item);
        }
    }

    public void addToQueue(ArrayList<SongListViewItem> toAdd){
        for(SongListViewItem s: toAdd){
            queue.add(s);
        }
    }

    public boolean isPlaying(){
        return mediaPlayer.isPlaying();
    }

    /*
     * Plays the song in the queue at the given position
     */
    public void playSongAtPosition(int position){
        // Checks to make sure the song is in the queue
        if(position > 0 && position < queue.size()){
            stop();
            nowPlayingPosition = position;
            nowPlaying = queue.get(nowPlayingPosition);
            loadSong(nowPlaying);
        }
    }

    public SongListViewItem getNowPlaying(){
        return nowPlaying;
    }

    public int getNowPlayingPosition(){
        return nowPlayingPosition;
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

    /*
     * Indicates if mediaplayer is in playable state
     */
    public boolean getNowPlayingBoolean(){
        return nowPlayingBoolean;
    }

    /*
     * Changes now playing position to the index in the queue of the currently playing song
     *
     * @param newQueue, The arraylist of the new queue
     */
    public void updateNowPlayingPosition(){
        nowPlayingPosition = this.queue.indexOf(nowPlaying);
    }

    public void destroy(){
        stop();
        mediaPlayer.release();
        if(shuffle){
            shuffle();
        }
        nowPlaying = null;
        nowPlayingPosition = 0;
        nowPlayingBoolean = false;

        listener.destroy();
    }

    public void removeSong(int position){

        // If this is the only song, just destroy the media player
        if(queue.size() == 1){
            destroy();
        }
        else {

            // If song is currently playing
            if (position == nowPlayingPosition) {
                // If it is the last song, go back to the previous song
                if (position == queue.size() - 1) {
                    nowPlayingPosition = nowPlayingPosition - 2;
                }
                // Go to the next song
                playNextSong();
            }

            // Remove the song
            queue.remove(position);
        }
    }

    public void shuffle(){
        // Set the appropriate queue
        if (shuffle) {
            ArrayList<SongListViewItem> temp = new ArrayList<SongListViewItem>(queue);
            queue = new ArrayList<SongListViewItem>(alternateQueue);

            // Add any new songs to the end of the queue
            for(int i = 0; i < temp.size(); i++){
                if (queue.indexOf(temp.get(i)) == -1) {
                    queue.add(temp.get(i));
                }
            }

            // Remove any songs from the queue that have been removed
            for(int i = 0; i < queue.size(); i++){
                if (temp.indexOf(queue.get(i)) == -1) {
                    queue.remove(i);
                    i--;
                }
            }
            alternateQueue = null;

            // Set up the variables to be correct
            nowPlayingPosition = queue.indexOf(nowPlaying);
            nowPlaying = queue.get(nowPlayingPosition);
        }
        else {
            alternateQueue = new ArrayList<SongListViewItem>(queue);
            Collections.shuffle(queue);

            queue.remove(nowPlaying);
            queue.add(0, nowPlaying);
            nowPlayingPosition = 0;

        }

        // Invert the shuffle boolean
        shuffle = !shuffle;
    }

    public boolean isShuffle(){
        return shuffle;
    }
}
