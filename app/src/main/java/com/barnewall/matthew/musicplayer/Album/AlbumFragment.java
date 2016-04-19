package com.barnewall.matthew.musicplayer.Album;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.Album.AlbumAdapter;
import com.barnewall.matthew.musicplayer.Album.AlbumListViewItem;
import com.barnewall.matthew.musicplayer.GlobalFunctions;
import com.barnewall.matthew.musicplayer.MainActivity;
import com.barnewall.matthew.musicplayer.MusicFragment;
import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * Created by mbarnewall17 on 2/15/2016.
 */
public class AlbumFragment extends MusicFragment {

    // Instance variables
    private boolean fling;
    private AlbumAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_album, container, false);
    }


    public void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category){

        // The Database (FROM)
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        // SELECT album, album id, artist, year, artist id
        String[] columns = new String[] {"DISTINCT " + MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ARTIST_ID};

        switch(category){
            case ARTISTS:
                break;
            case GENRES:
                uri = MediaStore.Audio.Genres.Members.getContentUri("external", Integer.parseInt(whereParams[whereParams.length - 1]));
                whereParams = Arrays.copyOfRange(whereParams, 0, whereParams.length - 1);
                break;
            default:
                break;
        }

        ContentResolver musicResolver   = getActivity().getContentResolver();

        // Query the database
        Cursor musicCursor = musicResolver.query(
                uri   //Database
                , columns                                      //Columns
                , where                                        //Where
                , whereParams                                  //Where variables
                , MediaStore.Audio.Albums.ALBUM + " ASC");     //Order By

        // Arraylist of AlbumListViewItems that will be filled from the database query
        ArrayList<AlbumListViewItem> albums = new ArrayList<AlbumListViewItem>();

        if(musicCursor!= null && musicCursor.moveToFirst()){
            do{
                // Artist
                String artist = musicCursor.getString(2);
                String year = null;
                // Concat year if not null
                if(musicCursor.getString(3) != null){
                    year = musicCursor.getString(3);
                }

                // Create object for database entry
                AlbumListViewItem item = new AlbumListViewItem(
                        musicCursor.getLong(1),                 // AlbumID
                        artist,                                   // ArtistNameAndNumberOfSongs or ArtistAndYear
                        musicCursor.getString(0),               // Title
                        null,                                   // Bitmap album artwork
                        musicCursor.getString(4));                // ArtistID

                if(year != null) {
                    item.setOther(year);
                }

                // Indicate part of a subquery
                if(category == MainActivity.MusicCategories.ARTISTS || category == MainActivity.MusicCategories.GENRES){
                    item.setSubQuery(true);
                }
                albums.add(item);
            }
            while (musicCursor.moveToNext());
        }

        // Remove duplicate albums
        removeDuplicates(albums);

        // Set the adapter
        adapter = new AlbumAdapter(albums, getActivity());
        ListView listView = (ListView) getView().findViewById(R.id.albumListView);
        listView.setAdapter(adapter);

        // Add OnItemClickListener
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getMListener().handleAlbumOnClick(adapter.getItem(position));
            }
        });

        // Add listener to handle loading of album artwork
        listView.setOnScrollListener(createListener());

        // Enable quick scrolling
        listView.setFastScrollEnabled(true);
    }

    public static ArrayList<AlbumListViewItem> removeDuplicates(ArrayList<AlbumListViewItem> duplicates){
        Collections.sort(duplicates);
        for(int i = 0; i < duplicates.size() - 1; i++){
            if(duplicates.get(i).compareTo(duplicates.get(i + 1)) == 0){
                duplicates.remove(i + 1);
                i = i - 1;
            }
        }
        return duplicates;
    }








    private AbsListView.OnScrollListener createListener(){
        return new AbsListView.OnScrollListener() {

            // AlbumListViewItems with set album arts
            private ArrayList<AlbumListViewItem> setArtworks = new ArrayList<AlbumListViewItem>();
            private boolean finished = true;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            // Need to handle removing old album art
            @Override
            public void onScroll(AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {
                // finished, keeps onScroll from creating more than one thread
                if(finished) {
                    finished = false;

                    // Makes sure the list view isn't empty
                    if (((AlbumListViewItem) adapter.getItem(firstVisibleItem)).getAlbumID() != -1) {

                        // Does all work on new thread
                        new Thread(new Runnable() {
                            @Override
                            public void run() {

                                // Current item in list view
                                AlbumListViewItem item;

                                // List of items with set album arts
                                ArrayList<AlbumListViewItem> temp = new ArrayList<AlbumListViewItem>();

                                // For each visible item, if not album art is set, set the album art
                                for (int i = -1*(visibleItemCount/2); i < 2 * visibleItemCount; i++) {

                                    // Checks if in valid range
                                    if (i + firstVisibleItem >= 0 && i + firstVisibleItem < totalItemCount) {

                                        item = (AlbumListViewItem) adapter.getItem(firstVisibleItem + i);

                                        // If not album art set, set it
                                        if (item.getAlbumArt() == null) {
                                            item.setAlbumArt(GlobalFunctions.getBitmapFromID(item.getAlbumID(), 60, getActivity()));
                                        }

                                        // Remove from list of set artworks if already in there, keeps album art from being reset
                                        if (setArtworks.indexOf(item) != -1) {
                                            setArtworks.remove(item);
                                        }

                                        // Add item to list of set items
                                        temp.add(item);
                                    }
                                }

                                // Clear old album artwork
                                for (AlbumListViewItem a : setArtworks) {
                                    a.setAlbumArt(null);
                                }

                                // change setArtworks to reflect currently set artworks
                                setArtworks = new ArrayList<AlbumListViewItem>(temp);

                                // Run UI changes on main thread
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        adapter.notifyDataSetChanged();
                                    }
                                });

                                // Insist on garbage collection to clear old album art from memory
                                System.gc();

                                // Indicate finished
                                finished = true;
                            }
                        }).start();

                    }
                    else{
                        finished = true;
                    }
                }
            }
        };
    }
}
