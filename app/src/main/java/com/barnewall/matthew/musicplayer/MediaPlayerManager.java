package com.barnewall.matthew.musicplayer;

import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

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
    private ControlListener             listener;
    private boolean                     back;
    private boolean                     isInValidState;
    private boolean                     shuffle;
    private ArrayList<SongListViewItem> alternateQueue;
    private Repeat                      repeat;

    public enum Repeat{
        NONE,REPEAT_SONG,REPEAT_ALL
    }

    public MediaPlayerManager(ArrayList<SongListViewItem> queue, int nowPlayingPosition, ControlListener listener){
        this.queue              = queue;
        this.nowPlayingPosition = nowPlayingPosition;
        this.nowPlaying         = queue.get(nowPlayingPosition);
        mediaPlayer             = new MediaPlayer();
        this.listener           = listener;
        startOver               = true;
        back                    = false;
        isInValidState          = true;
        handler                 = new Handler();
        shuffle                 = false;
        repeat                  = Repeat.NONE;


        //Set up the listener
        mediaPlayer.setOnCompletionListener(onCompletionListener);
        nowPlaying = queue.get(nowPlayingPosition);
        loadSong(nowPlaying);

    }

    public void setListener(ControlListener listener){
        this.listener = listener;
    }



    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            // If repeat is enabled, replay the same song
            // Decrementing the nowPlayingPosition and then calling playNextSong repeats the same song
            if(repeat == Repeat.REPEAT_SONG){
                nowPlayingPosition--;
            }

            // Play the next song in the queue if there is another one
            if (nowPlayingPosition != MediaPlayerManager.this.queue.size() - 1) {
                playNextSong();

            // If there is no next song
            } else {

                // Repeat the queue again if repeat all is toggled
                if(repeat == Repeat.REPEAT_ALL){
                    nowPlayingPosition = -1;
                    playNextSong();
                }

                // Release the manager and set up the manager so it is no longer in a playing state
                else{
                    nowPlaying.setAnimated(false);
                    mediaPlayer.release();

                    isInValidState = false;
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
        if(!isInValidState){
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            loadSong(nowPlaying);
        }
        else {

            // If called because the back button was pressed, just load the song
            if(back){
                loadSong(nowPlaying);
            }
            else {
                mediaPlayer.start();
                listener.songPlay();
            }
        }
        nowPlaying.setAnimated(true);
        isInValidState = true;
        NotificationManagement.createNotification(listener.getContext(),
                listener.getApplicationName(),
                nowPlaying.getTitle(),
                nowPlaying.getArtistName(),
                GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                        (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                        listener.getContext()), true);
    }

    public void stop(){
        if(isInValidState) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        nowPlaying.setAnimated(false);
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
        handler.postDelayed(r,500);
        stop();
        nowPlaying = queue.get(nowPlayingPosition);
        play();
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
                isInValidState = true;
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
            Toast.makeText(listener.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
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
        if(!isInValidState){
           play();
        }
        mediaPlayer.seekTo(position);
    }

    public ArrayList<SongListViewItem> getQueue(){
        return queue;
    }

    public void finished(){
        if(listener != null){
            listener.onFinish();
        }
        NotificationManagement.removeNotification(listener.getContext());
    }

    /*
     * Indicates if mediaplayer is in playable state
     */
    public boolean getInValidState(){
        return isInValidState;
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
        isInValidState = false;

        listener.destroy();
        NotificationManagement.removeNotification(listener.getContext());
    }

    public void removeSong(int position){

        // If this is the only song, just destroy the media player
        if(queue.size() == 1){
            destroy();nowPlaying.setAnimated(false);
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

    public Repeat toggleRepeat(){
        switch (repeat){
            case NONE:
                repeat = Repeat.REPEAT_SONG;
                break;
            case REPEAT_SONG:
                repeat = Repeat.REPEAT_ALL;
                break;
            case REPEAT_ALL:
                repeat = Repeat.NONE;
                break;
        }
        return repeat;
    }

    public Repeat getRepeat(){
        return repeat;
    }
}
