package com.barnewall.matthew.musicplayer;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.Song.SongAdapter;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;

public class NowPlayingActivity extends ActionBarActivity {
    // Variables for interacting with service that plays the music
    private IBinder                     service;
    private ServiceConnection           connection;

    ArrayList<SongListViewItem> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        Intent intent = new Intent(this, MusicPlayerService.class);
        this.connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                NowPlayingActivity.this.service = service;
                queue = ((MusicPlayerService.MyBinder)service).getService().getManager().getQueue();

                // Create the adapter
                SongAdapter adapter = new SongAdapter(queue, NowPlayingActivity.this);

                QueueListView listView = (QueueListView) findViewById(R.id.queue_listview);

                // Set the adapter and the arraylist
                listView.setAdapter(adapter);
                listView.setArrayList(queue);

                listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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


    @Override
    public void onDestroy(){
        super.onDestroy();
        getApplicationContext().unbindService(connection);
    }
}
