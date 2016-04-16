package com.barnewall.matthew.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;

import com.barnewall.matthew.musicplayer.Song.NowPlayingAdapter;
import com.barnewall.matthew.musicplayer.Song.SongAdapter;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;
import java.util.ResourceBundle;

public class NowPlayingActivity extends ActionBarActivity implements ControlListener {
    // Variables for interacting with service that plays the music
    private IBinder                     service;
    private ServiceConnection           connection;
    public static final int NOW_PLAYING = 253;

    ArrayList<SongListViewItem> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        // Sets the audio stream so volume changes affect music volume not system volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Connects to the MusicPlayerService to get a reference to the MediaPlayerManager
        Intent intent = new Intent(this, MusicPlayerService.class);
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NowPlayingActivity.this.service = service;
                MediaPlayerManager manager = ((MusicPlayerService.MyBinder)service).getService().getManager();

                queue = manager.getQueue();

                // Create the adapter
                NowPlayingAdapter adapter = new NowPlayingAdapter(queue, NowPlayingActivity.this);

                QueueListView listView = (QueueListView) findViewById(R.id.queue_listview);

                // Set the adapter and the arraylist
                listView.setAdapter(adapter);
                listView.setArrayList(queue);

                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                // Set up for animation
                manager.setListener(NowPlayingActivity.this);
                if(manager.isPlaying()){
                    songPlay();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {

            }
        };
        getApplicationContext().bindService(intent, connection, Context.BIND_AUTO_CREATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_now_playing, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Creates a popup menu of options for the song, artist, album
    public void createPopUp(View view){
        PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.pop_up_menu, popup.getMenu());
        popup.show();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(connection);
    }

    //TODO: Implement the now playing animation, and implement these methods to control the animation
    @Override
    public void loadNewSongInfo(SongListViewItem newSong) {
        // Set the animation based on the new song
    }

    @Override
    public void onFinish() {

    }

    @Override
    public void songPause() {
        // stop the animation and remove the background
        recordingAnimation.stop();
        nowPlayingImageView.setBackground(null);
    }

    private ImageView nowPlayingImageView;
    private AnimationDrawable recordingAnimation;

    // Doesn't crash but animation doesn't show
    @Override
    public void songPlay() {
        // Get the song in the listview that is playing
        int position = ((MusicPlayerService.MyBinder)service).getService().getManager().getNowPlayingPosition();
        ListView listview = (ListView) findViewById(R.id.queue_listview);

        View view = listview.getAdapter().getView(position, null, listview);
        nowPlayingImageView = (ImageView) view.findViewById(R.id.playingAnimationImageView);

        // set up the animation and start it
        nowPlayingImageView.setBackgroundResource(R.drawable.now_playing);
        recordingAnimation = (AnimationDrawable) nowPlayingImageView.getBackground();
        recordingAnimation.start();
    }
}
