package com.barnewall.matthew.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.PowerManager;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.barnewall.matthew.musicplayer.BroadcastReceivers.RemoteControlReceiver;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by Matthew on 8/18/2015.
 */
public class MediaPlayerManager extends MediaSessionCompat.Callback
        implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener {
    public static MediaSessionCompat mediaSession;

    private MediaPlayer mediaPlayer;
    private ArrayList<SongListViewItem> queue;
    private SongListViewItem nowPlaying;
    private int nowPlayingPosition;
    private ControlListener listener;
    private boolean isInValidState;
    private boolean shuffle;
    private ArrayList<SongListViewItem> alternateQueue;
    private Repeat repeat;
    private int volume;
    private PowerManager.WakeLock wakeLock;
    private Context context;

    public enum Repeat {
        NONE, REPEAT_SONG, REPEAT_ALL
    }

    public MediaPlayerManager(ArrayList<SongListViewItem> queue, int nowPlayingPosition, ControlListener listener) {
        this.queue = queue;
        this.nowPlayingPosition = nowPlayingPosition;
        this.nowPlaying = queue.get(nowPlayingPosition);
        this.listener = listener;
        this.context = listener.getContext();
        isInValidState = false;
        shuffle = false;
        repeat = Repeat.NONE;
        volume = 0;

        setUpMediaSession();

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(this);
        mediaPlayer.setOnPreparedListener(this);
        mediaPlayer.setOnErrorListener(this);

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, GlobalFunctions.TAG);
        wakeLock.acquire();

        loadSong(nowPlaying);
    }

    private void setUpMediaSession() {
        ComponentName mediaButtonReceiver = new ComponentName(context, RemoteControlReceiver.class);
        mediaSession = new MediaSessionCompat(context, GlobalFunctions.TAG, mediaButtonReceiver, null);

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(this);

        setMediaSessionState();

        mediaSession.setActive(true);
    }

    private void setMediaSessionState() {
        PlaybackStateCompat playback = new PlaybackStateCompat.Builder()
                .setActions(PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT |
                        PlaybackStateCompat.ACTION_PLAY_PAUSE
                )
                .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, getCurrentTimeOfSongPlayingInMS(), 1)
                .build();
        mediaSession.setPlaybackState(playback);
    }

    public void setListener(ControlListener listener) {
        this.listener = listener;
    }

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
                onStop();
            }
        }
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        isInValidState = true;

        setMetadata(nowPlaying);

        launchNotification(false);

        onPlay();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        mediaPlayer.release();
        if (wakeLock.isHeld())
            wakeLock.release();
        return false;
    }

    private AudioManager.OnAudioFocusChangeListener onAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    onPause();
                    break;
                case AudioManager.AUDIOFOCUS_GAIN:
                    ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                    onPlay();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    onPause(); // TODO: This needs to be changed to onStop once saving state is implemented
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
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
    }

    @Override
    public void onPlay() {
        super.onPlay();

        if (requestAudioFocus((AudioManager) context.getSystemService(Context.AUDIO_SERVICE), onAudioFocusChangeListener)) {
            mediaPlayer.start();
            listener.songPlay();
            launchNotification(true);
            nowPlaying.setAnimated(true);
        }

        setMediaSessionState();
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

    @Override
    public void onPause() {
        super.onPause();

        nowPlaying.setAnimated(false);
        mediaPlayer.pause();
        listener.songPause();

        launchNotification(false);
        setMediaSessionState();
    }

    @Override
    public void onSkipToPrevious() {
        if (mediaPlayer.getCurrentPosition() > 10000 || nowPlayingPosition == 0) {
            stop();
            loadSong(nowPlaying);
        } else {
            nowPlayingPosition = nowPlayingPosition - 2;
            playNextSong();
        }
    }

    @Override
    public void onSkipToNext() {
        super.onSkipToNext();
        if (nowPlayingPosition != queue.size() - 1)
            playNextSong();
    }

    public void loadSong(SongListViewItem item) {
        try {
            if (item != null) {
                Log.d(GlobalFunctions.TAG, "song loaded from: " + item.getDataLocation());
                mediaPlayer.setDataSource(item.getDataLocation());
                mediaPlayer.prepareAsync();
                listener.loadNewSongInfo(nowPlaying);
            }
        } catch (IOException e) {
            Log.e(GlobalFunctions.TAG, e.toString());
        }
    }

    private void setMetadata(SongListViewItem item) {
        MediaMetadataCompat metadata = new MediaMetadataCompat.Builder().putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, GlobalFunctions.getBitmapFromID(item.getAlbumID(), 300, context))
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, item.getAlbumName())
                .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, item.getArtistName())
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, item.getTitle())
                .build();

        mediaSession.setMetadata(metadata);
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

    public boolean isPlaying() {
        return isInValidState ? mediaPlayer.isPlaying() : false;
    }

    public void playSongAtPosition(int position) {
        if (position > 0 && position < queue.size()) {
            stop();
            nowPlayingPosition = position;
            nowPlaying = queue.get(nowPlayingPosition);
            loadSong(nowPlaying);
        }
    }

    public SongListViewItem getNowPlaying() {
        return nowPlaying;
    }

    public int getNowPlayingPosition() {
        return nowPlayingPosition;
    }

    public int getDuration() {
        return mediaPlayer.getDuration();
    }

    public int getCurrentTimeOfSongPlayingInMS() {
        return isInValidState ? mediaPlayer.getCurrentPosition() : 0;
    }

    public void onSeekTo(int pos) {
        mediaPlayer.seekTo(pos);
    }

    public ArrayList<SongListViewItem> getQueue() {
        return queue;
    }

    public boolean isInValidState() {
        return isInValidState;
    }

    public void updateNowPlayingPosition() {
        nowPlayingPosition = this.queue.indexOf(nowPlaying);
    }

    @Override
    public void onStop() {
        nowPlaying = null;
        nowPlayingPosition = 0;
        isInValidState = false;

        if (listener != null) {
            listener.onFinish();
            listener.destroy();
        }

        ((AudioManager) context.getSystemService(Context.AUDIO_SERVICE)).abandonAudioFocus(onAudioFocusChangeListener);
        mediaPlayer.release();
        mediaSession.release();
        NotificationManagement.removeNotification(context);

        if (wakeLock.isHeld())
            wakeLock.release();

    }

    /*
     * Removes the song in the queue at the position passed in
     *
     * @param   position    An int indicating the position to remove in the queue
     */
    public void removeSong(int position) {

        // If this is the only song, just stop the media player
        if (queue.size() == 1)
            onStop();
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

        shuffle = !shuffle;
    }

    public boolean isShuffleEnabled() {
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

    private void launchNotification(boolean isPlaying) {
        NotificationManagement.createNotification(context,
                listener.getApplicationName(),
                nowPlaying.getTitle(),
                nowPlaying.getArtistName(),
                GlobalFunctions.getBitmapFromID(nowPlaying.getAlbumID(),
                        (int) listener.getContext().getResources().getDimension(R.dimen.layoutHeight),
                        context), isPlaying);
    }
}
