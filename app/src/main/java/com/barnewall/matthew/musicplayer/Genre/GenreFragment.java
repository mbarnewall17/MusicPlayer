package com.barnewall.matthew.musicplayer.Genre;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.Genre.GenreAdapter;
import com.barnewall.matthew.musicplayer.Genre.GenreListViewItem;
import com.barnewall.matthew.musicplayer.MainActivity;
import com.barnewall.matthew.musicplayer.MusicFragment;
import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;

/**
 * Created by Matthew on 2/29/2016.
 */
public class GenreFragment extends MusicFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_genre_listview, container, false);
    }

    @Override
    public void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category) {

        // Select genreName, genreID
        String[] columns = new String[] {MediaStore.Audio.Genres.NAME,
                MediaStore.Audio.Genres._ID};

        ContentResolver musicResolver   = getActivity().getContentResolver();

        // Query the database
        Cursor musicCursor = musicResolver.query(
                MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI     // Database
                , columns                                         // Columns
                , where                                           // Where
                , whereParams                                     // Where variables
                , MediaStore.Audio.Genres.NAME + " ASC");         // Order By

        // ArrayList to hold genre objects
        ArrayList<GenreListViewItem> genres = new ArrayList<GenreListViewItem>();

        // Create new genre object
        if(musicCursor!= null && musicCursor.moveToFirst()){
            do{
                genres.add(new GenreListViewItem(musicCursor.getString(0),
                        musicCursor.getString(1)));
            }
            while (musicCursor.moveToNext());
        }

        // Select albumTitle
        columns = new String[] {MediaStore.Audio.Media.TITLE};

        // For each genre in initial query, check if genre has any files associated with it
        // If not, remove the genre
        for(int i = 0; i < genres.size(); i++){

            // Get the genre
            GenreListViewItem g = genres.get(i);

            // Query the database
            musicCursor = musicResolver.query(
                    MediaStore.Audio.Genres.Members.getContentUri("external", Long.parseLong(g.getId()))    // Database
                    ,columns                                                                                // Columns
                    ,where                                                                                  // Where
                    ,whereParams                                                                            // Where variables
                    ,null);                                                                                 // Order By

            if(!(musicCursor!= null && musicCursor.moveToFirst())){
                i = i - 1;
                genres.remove(g);
            }
        }

        // Set the adapter
        final GenreAdapter adapter = new GenreAdapter(genres, getActivity());
        ListView listView = (ListView) getView().findViewById(R.id.musicItemGenreListView);
        listView.setAdapter(adapter);

        // Set OnItemClickListener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getMListener().handleGenreOnClick(adapter.getItem(position));
            }
        });
    }
}
