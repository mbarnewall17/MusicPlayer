package com.barnewall.matthew.musicplayer;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.Song.SongAdapter;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;

public class NowPlayingActivity extends ActionBarActivity {
    ArrayList<SongListViewItem> queue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_now_playing);

        // Get the queue of songs
        queue = (ArrayList<SongListViewItem>) getIntent().getExtras().get("queue");

        // Create the adapter
        SongAdapter adapter = new SongAdapter(queue, this);

        QueueListView listView = (QueueListView) findViewById(R.id.queue_listview);

        // Set the adapter and the arraylist
        listView.setAdapter(adapter);
        listView.setArrayList(queue);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
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
}
