package com.barnewall.matthew.musicplayer;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.TooManyListenersException;

/**
 * Created by Matthew on 8/18/2015.
 */
public class MediaPlayerManager {
    private MediaPlayer mediaPlayer;
    private ArrayList<SongListViewItem> queue;
    private SongListViewItem nowPlaying;
    private int nowPlayingPosition;
    private boolean startOver;
    private Handler handler;
    private ControlListener listener;
    private boolean back;
    private boolean isInValidState;
    private boolean shuffle;
    private ArrayList<SongListViewItem> alternateQueue;
    private Repeat repeat;
    private AudioManager audioManager;
    private int volume;

    private static final int BACK_DELAY = 500;

    public enum Repeat {
        NONE, REPEAT_SONG, REPEAT_ALL
    }

    public MediaPlayerManager(ArrayList<SongListViewItem> queue, int nowPlayingPosition, ControlListener listener) {
        this.queue = queue;
        this.nowPlayingPosition = nowPlayingPosition;
        this.nowPlaying = queue.get(nowPlayingPosition);
        mediaPlayer = new MediaPlayer();
        this.listener = listener;
        startOver = true;
        back = false;
        isInValidState = true;
        handler = new Handler();
        shuffle = false;
        repeat = Repeat.NONE;
        audioManager = (AudioManager) listener.getContext().getSystemService(Context.AUDIO_SERVICE);
        volume = 0;

        mediaPlayer.setOnCompletionListener(onCompletionListener);

        nowPlaying = queue.get(nowPlayingPosition);
        loadSong(nowPlaying);
        play();
    }

    public void setListener(ControlListener listener) {
        this.listener = listener;
    }

    private MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {

            if (repeat == Repeat.REPEAT_SONG)
                nowPlayingPosition--;

            if (nowPlayingPosition != MediaPlayerManager.this.queue.size() - 1)
                playNextSong();
            else {
                if (repeat == Repeat.REPEAT_ALL) {
                    nowPlayingPosition = -1;
                    playNextSong();
                } else {
                    endPlayback();
                }
            }
        }
    };

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    pause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                    play();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    pause(); // TODO: This needs to be changed to endPlayback once saving state is implemented
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) (volume * .5), 0);
                    break;
            }
        }
    };

    private void playNextSong() {
        stop();

        nowPlayingPosition++;
        nowPlaying = queue.get(nowPlayingPosition);

        loadSong(nowPlaying);
        play();
    }

    public void play() {
        if (!isInValidState) {
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            loadSong(nowPlaying);
        } else if (back)
            loadSong(nowPlaying);

        if (requestAudioFocus(audioManager, onAudioFocusChangeListener)) {
            mediaPlayer.start();
            listener.songPlay();
            launchNotification(true);
            nowPlaying.setAnimated(true);
        }
    }

    public boolean requestAudioFocus(AudioManager manager, AudioManager.OnAudioFocusChangeListener listener) {
        int result = manager.requestAudioFocus(listener,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
    }

    public void stop() {
        if (isInValidState) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
        nowPlaying.setAnimated(false);
    }

    public void pause() {
        nowPlaying.setAnimated(false);
        mediaPlayer.pause();
        listener.songPause();

        audioManager.abandonAudioFocus(onAudioFocusChangeListener);

        launchNotification(false);
    }

    // Resets the variables needed for the back button
    private Runnable r = new Runnable() {
        @Override
        public void run() {
            startOver = true;
            back = false;
        }
    };

    /*
     * Goes backwards in the play queue
     */
    public void back() {
        back = true;

        if (startOver) {
            startOver = false;
        } else if (nowPlayingPosition > 0) {
            nowPlayingPosition = nowPlayingPosition - 1;
            handler.removeCallbacks(r);
        }

        handler.postDelayed(r, BACK_DELAY);

        stop();
        nowPlaying = queue.get(nowPlayingPosition);
        play();
    }

    public void skip() {
        if (nowPlayingPosition != queue.size() - 1)
            playNextSong();
    }

    public void loadSong(SongListViewItem item) {
        try {
            if (item != null) {
                Log.d(GlobalFunctions.TAG, "song loaded from: " + item.getDataLocation());
                mediaPlayer.setDataSource(item.getDataLocation());
                mediaPlayer.prepare();
                isInValidState = true;
            }

            launchNotification(false);

            listener.loadNewSongInfo(item);
        } catch (Exception e) {
            Toast.makeText(listener.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /*
     * Adds the passed in songs between the current song and the next song
     *
     * @param   toAdd   An ArrayList of SongListViewItems to add to the queue
     */
    public void playNext(ArrayList<SongListViewItem> toAdd) {
        Collections.reverse(toAdd);
        for (SongListViewItem item : toAdd) {
            queue.add(nowPlayingPosition + 1, item);
        }
    }

    /*
     * Adds the passed in songs to the end of the queue
     *
     * @param   toAdd   An ArrayList of SongListViewItems to add to the queue
     */
    public void addToQueue(ArrayList<SongListViewItem> toAdd) {
        for (SongListViewItem s : toAdd) {
            queue.add(s);
        }
    }

    /*
     * Returns whether or not the song is playing
     *
     * @return  boolean A boolean indicating if the song is playing
     */
    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    /*
     * Plays the song in the queue at the given position
     *
     * @param   position    An int indicating which song in the queue to play
     */
    public void playSongAtPosition(int position) {
        if (position > 0 && position < queue.size()) {
            stop();
            nowPlayingPosition = position;
            nowPlaying = queue.get(nowPlayingPosition);
            loadSong(nowPlaying);
            play();
        }
    }

    /*
     * Returns the current song being played
     *
     * @return  nowPlaying  A SongListViewItem of the song that is playing
     */
    public SongListViewItem getNowPlaying() {
        return nowPlaying;
    }

    /*
     * Returns the position in the queue of the song that is plying
     *
     * @return  int An int indicating the position in the queue of the song that is playing
     */
    public int getNowPlayingPosition() {
        return nowPlayingPosition;
    }

    /*
     * Returns the duration of the playing song
     *
     * @return  int An int indicating the duration of the song in ms
     */
    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    /*
     * Returns the current time in the song being played in ms
     *
     * @return  int An int indicating the current time of the song in ms
     */
    public int getCurrentPosition() {
        return mediaPlayer.getCurrentPosition();
    }

    /*
     * Seeks the media player to the passed in position
     *
     * @param   position    The position the media player is to seek to
     */
    public void seekTo(int position) {
        if (!isInValidState) {
            play();
        }
        mediaPlayer.seekTo(position);
    }

    /*
     * Returns the play queue
     *
     * @return  queue   An ArrayList of the songs being played (SongListViewItem)s
     */
    public ArrayList<SongListViewItem> getQueue() {
        return queue;
    }

    /*
     * Indicates if mediaplayer is in playable state
     *
     * @return  boolean A boolean indicating if the manager is in a valid state
     */
    public boolean isInValidState() {
        return isInValidState;
    }

    /*
     * Changes now playing position to the index in the queue of the currently playing song
     *
     * @param newQueue, The arraylist of the new queue
     */
    public void updateNowPlayingPosition() {
        nowPlayingPosition = this.queue.indexOf(nowPlaying);
    }

    public void endPlayback() {
        stop();
        audioManager.abandonAudioFocus(onAudioFocusChangeListener);
        mediaPlayer.release();
        if (shuffle) {
            shuffle();
        }

        nowPlaying = null;
        nowPlayingPosition = 0;
        isInValidState = false;

        if (listener != null) {
            listener.onFinish();
            listener.destroy();
        }
        NotificationManagement.removeNotification(listener.getContext());
    }

    /*
     * Removes the song in the queue at the position passed in
     *
     * @param   position    An int indicating the position to remove in the queue
     */
    public void removeSong(int position) {

        // If this is the only song, just endPlayback the media player
        if (queue.size() == 1) {
            endPlayback();
            nowPlaying.setAnimated(false);
        } else {

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

    /*
     * Toggles shuffle
     *  If shuffle was enabled, return to the un-shuffled queue, new songs added to the end,
     *      removed songs removed.
     *  If shuffle was not enabled, shuffle the queue
     */
    public void shuffle() {
        // Set the appropriate queue
        if (shuffle) {
            ArrayList<SongListViewItem> temp = new ArrayList<SongListViewItem>(queue);
            queue = new ArrayList<SongListViewItem>(alternateQueue);

            // Add any new songs to the end of the queue
            for (int i = 0; i < temp.size(); i++) {
                if (queue.indexOf(temp.get(i)) == -1) {
                    queue.add(temp.get(i));
                }
            }

            // Remove any songs from the queue that have been removed
            for (int i = 0; i < queue.size(); i++) {
                if (temp.indexOf(queue.get(i)) == -1) {
                    queue.remove(i);
                    i--;
                }
            }
            alternateQueue = null;

            // Set up the variables to be correct
            nowPlayingPosition = queue.indexOf(nowPlaying);
            nowPlaying = queue.get(nowPlayingPosition);
        } else {
            // Create a backup of the un-shuffled queue
            alternateQueue = new ArrayList<SongListViewItem>(queue);

            // Shuffle the queue
            Collections.shuffle(queue);

            // Add the song that is playing now to the first index
            queue.remove(nowPlaying);
            queue.add(0, nowPlaying);
            nowPlayingPosition = 0;

        }

        // Invert the shuffle boolean
        shuffle = !shuffle;
    }

    /*
     * Returns whether shuffle is enabled or not
     *
     * @return  boolean Boolean indicating if shuffle is enabled
     */
    public boolean isShuffle() {
        return shuffle;
    }

    /*
     * Toggles repeat
     *  None => Repeat the song
     *  Repeat the song => Repeat the queue
     *  Repeat the queue => None
     *
     * @return  Repeat  The managers repeat setting
     */
    public Repeat toggleRepeat() {
        switch (repeat) {
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

    /*
     * Indicates what the repeat is set to
     *
     * @return  Repeat  The managers repeat setting
     */
    public Repeat getRepeat() {
        return repeat;
    }

    public void launchNotification(boolean isPlaying) {
        NotificationManagement.createNotification(listener.getContext(),
                listener.getApplicationName(),
                nowPlaying.getTitle(),
                nowPlaying.getArtistName(),
                GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                        (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                        listener.getContext()), isPlaying);
    }
}
