package com.barnewall.matthew.musicplayer.Playlist;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.MainActivity;
import com.barnewall.matthew.musicplayer.MusicFragment;
import com.barnewall.matthew.musicplayer.Playlist.PlaylistAdapter;
import com.barnewall.matthew.musicplayer.Playlist.PlaylistListViewItem;
import com.barnewall.matthew.musicplayer.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Matthew on 2/29/2016.
 */
public class PlaylistFragment extends MusicFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_playlist_listview, container, false);
    }

    public void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category) {

        final ArrayList<PlaylistListViewItem> playlists = getPlaylists(where, whereParams, category, getActivity().getContentResolver());
        // Set the adapter
        PlaylistAdapter adapter = new PlaylistAdapter(playlists, getActivity());
        ListView listView = (ListView) getView().findViewById(R.id.musicItemPlaylistListView);
        listView.setAdapter(adapter);

        // Set the OnItemClickListener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getMListener().handlePlaylistOnClick(playlists.get(position));
            }
        });
    }

    public static ArrayList<PlaylistListViewItem> getPlaylists(String where, String[] whereParams, MainActivity.MusicCategories category, ContentResolver musicResolver) {
        // Select playlistName, playlistDataLocation
        String[] columns = new String[]{MediaStore.Audio.Playlists.NAME,
                MediaStore.Audio.Playlists.DATA};

        // Query the database
        Cursor musicCursor = musicResolver.query(
                MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI     // Database
                , columns                                            // Columns
                , where                                              // Where
                , whereParams                                        // Where variables
                , MediaStore.Audio.Playlists.NAME + " ASC");         // Order By

        // Arraylist to hold playlist objects
        final ArrayList<PlaylistListViewItem> playlists = new ArrayList<PlaylistListViewItem>();

        // Add each playlist
        if (musicCursor != null && musicCursor.moveToFirst()) {
            do {
                playlists.add(new PlaylistListViewItem(musicCursor.getString(0),
                        musicCursor.getString(1)));
            }
            while (musicCursor.moveToNext());
        }

        for (int i = 0; i < playlists.size(); i++) {
            File file = new File(playlists.get(i).getPath());
            if (!file.exists()) {
                playlists.remove(i);
                i--;
            }
        }

        return playlists;
    }
}
