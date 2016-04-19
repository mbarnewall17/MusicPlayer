package com.barnewall.matthew.musicplayer.Artist;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.Album.AlbumFragment;
import com.barnewall.matthew.musicplayer.Album.AlbumListViewItem;
import com.barnewall.matthew.musicplayer.MainActivity;
import com.barnewall.matthew.musicplayer.MusicFragment;
import com.barnewall.matthew.musicplayer.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by Matthew on 2/29/2016.
 */
public class ArtistFragment extends MusicFragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_artist, container, false);
    }

    @Override
    public void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category) {

        // Select albumTitle, albumId, artistName, albumYear, artistID
        String[] columns = new String[] {"DISTINCT " + MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.YEAR,
                MediaStore.Audio.Media.ARTIST_ID};

        ContentResolver musicResolver   = getActivity().getContentResolver();

        // Query the database
        Cursor musicCursor = musicResolver.query(
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI    //Database
                , columns                                      //Columns
                , where                                        //Where
                , whereParams                                  //Where variables
                , MediaStore.Audio.Albums.ALBUM + " ASC");     //Order By

        ArrayList<AlbumListViewItem> albums = new ArrayList<AlbumListViewItem>();
        HashMap<String, Integer> map        = new HashMap<String, Integer>();
        HashMap<String, String> idMap       = new  HashMap<String, String>();

        // If entry from query
        if(musicCursor!= null && musicCursor.moveToFirst()){
            do{
                // Artist name
                String artistName = musicCursor.getString(2);

                // Album year
                String year = null;
                if(musicCursor.getString(3) != null){
                    year = year + "(" + musicCursor.getString(3) + ")";
                }

                map.put(artistName, 0);

                // artistName, artistID
                idMap.put(artistName, musicCursor.getString(4));

                AlbumListViewItem item = new AlbumListViewItem(musicCursor.getLong(1)
                        ,artistName, musicCursor.getString(0), null, musicCursor.getString(4));

                if(year != null){
                    item.setOther(year);
                }

                item.setSubQuery(true);
                albums.add(item);
            }
            while (musicCursor.moveToNext());

            // Remove duplicates
            albums = AlbumFragment.removeDuplicates(albums);

            // Count number of albums per artist
            for(AlbumListViewItem a : albums){
                map.put(a.getArtistName(), map.get(a.getArtistName()) + 1);
            }

            Set<String> artists = map.keySet();
            ArrayList<ArtistListViewItem> artist = new ArrayList<ArtistListViewItem>(artists.size());
            for(String s : artists){
                String numAlbums = map.get(s).toString();
                if(numAlbums.equals("1")){
                    numAlbums = "1 Album";
                }
                else{
                    numAlbums = numAlbums + " Albums";
                }
                artist.add(new ArtistListViewItem(s, numAlbums, idMap.get(s)));
            }

            // Alphabetical sort
            Collections.sort(artist);

            // Set adapter
            final ArtistAdapter adapter = new ArtistAdapter(artist, getActivity());
            ListView listView = ((ListView) getView().findViewById(R.id.artistListView));
            listView.setFastScrollEnabled(true);
            listView.setAdapter(adapter);

            // Set OnItemClickListener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getMListener().handleArtistOnClick(adapter.getItem(position));
                }
            });

        }
    }
}
