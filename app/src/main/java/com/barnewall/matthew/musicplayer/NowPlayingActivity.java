package com.barnewall.matthew.musicplayer;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.barnewall.matthew.musicplayer.Playlist.PlaylistManager;
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
    public static final String POSITION = "barnewall.musicplayer.position";
    public static final String ALBUM_FRAGMENT = "barnewall.musicplayer.album";
    private MediaPlayerManager manager;
    private ArrayList<SongListViewItem> queue;
    private boolean destroy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        destroy = false;

        // Sets the audio stream so volume changes affect music volume not system volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // Connects to the MusicPlayerService to get a reference to the MediaPlayerManager
        Intent intent = new Intent(this, MusicPlayerService.class);
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NowPlayingActivity.this.service = service;
                manager = ((MusicPlayerService.MyBinder)service).getService().getManager();

                queue = manager.getQueue();

                // Create the adapter
                NowPlayingAdapter adapter = new NowPlayingAdapter(queue, NowPlayingActivity.this);

                QueueListView listView = (QueueListView) findViewById(R.id.queue_listview);

                // Set the adapter and the arraylist
                listView.setAdapter(adapter);
                listView.setArrayList(queue);

                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

                // Open listview to the playing song
                listView.setSelection(manager.getNowPlayingPosition());

                // Starts playing the clicked on song
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        manager.playSongAtPosition(position);
                    }
                });

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
        if (id == R.id.action_save) {

            AlertDialog.Builder playlistName = new AlertDialog.Builder(this)
                    .setTitle(getResources().getString(R.string.playlist_enter_name));
            playlistName.setView(this.getLayoutInflater().inflate(R.layout.edit_text, null));
            playlistName.setCancelable(true);
            playlistName.setPositiveButton(getResources().getString(R.string.create), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String name = ((EditText) ((AlertDialog) dialog).findViewById(R.id.editText)).getText().toString();
                    if (name != null && name != "") {
                        String playlistName = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC) + "/" + name + ".m3u";
                        PlaylistManager manager = new PlaylistManager(playlistName, NowPlayingActivity.this);
                        String toastMessage;
                        if (manager.addSongsToPlaylist(queue)) {
                            toastMessage = queue.size() + " " + getResources().getString(R.string.playlist_success_adding_song);
                        } else {
                            toastMessage = getResources().getString(R.string.playlist_failure_adding_song);
                        }
                        Toast.makeText(NowPlayingActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                }
            });
            playlistName.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            playlistName.create().show();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // Creates a popup menu of options for the song, artist, album
    public void createPopUp(final View view){

        // Create popup menu
        final PopupMenu popup = new PopupMenu(this, view);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.now_playing_pop_up_menu, popup.getMenu());

        // Set a listener for when an popup option is selected
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int position = ((ListView) view.getParent().getParent()).getPositionForView(view);
                // Remove the selected song from the queue
                if (item.getTitle().equals(getResources().getString(R.string.remove_from_queue))) {

                    // Remove the song and update the ui
                    manager.removeSong(position);
                    QueueListView listView = (QueueListView) findViewById(R.id.queue_listview);
                    listView.invalidateViews();

                    // If no songs are left, go back to the main activity
                    if(manager.getNowPlaying() == null){
                        finish();
                    }
                }
                else {
                    Intent intent = getIntent();

                    // The position in the queue
                    intent.putExtra(POSITION, position);

                    // finish this activity and have mainActivity launch the correct fragment
                    if (item.getTitle().equals(getResources().getString(R.string.go_to_artist))) {
                        intent.putExtra(ALBUM_FRAGMENT, false);
                    } else if (item.getTitle().equals(getResources().getString(R.string.go_to_album))) {
                        intent.putExtra(ALBUM_FRAGMENT, true);
                    }
                    setResult(RESULT_OK, intent);
                    finish();
                }
                return false;
            }
        });
        popup.show();
    }


    @Override
    public void onDestroy(){
        super.onDestroy();
        // If the service is connected, end the music player and unbind
        if(connection != null && service.isBinderAlive()) {
            getApplicationContext().unbindService(connection);
        }
    }

    // Invalidate views to change the imageview
    @Override
    public void loadNewSongInfo(SongListViewItem newSong) {
        ListView listview = (ListView) findViewById(R.id.queue_listview);
        listview.invalidateViews();
    }

    // Invalidate views to change the imageview
    @Override
    public void onFinish() {
        ListView listview = (ListView) findViewById(R.id.queue_listview);
        listview.invalidateViews();
    }

    // Invalidate views to change the imageview
    @Override
    public void songPause() {
        ListView listview = (ListView) findViewById(R.id.queue_listview);
        listview.invalidateViews();
    }

    // Invalidate views to change the imageview
    @Override
    public void songPlay() {
        ListView listview = (ListView) findViewById(R.id.queue_listview);
        listview.invalidateViews();
    }

    public Context getContext(){
        return getApplicationContext();
    }

    public String getApplicationName(){
        return getPackageName();
    }

    public void destroy(){
        try {
            finish();
        }
        catch(Exception e){
            destroy = true;
        }
    }

    @Override
    public void onResume(){
        super.onResume();
        if(destroy){
            finish();
        }


    }
}
