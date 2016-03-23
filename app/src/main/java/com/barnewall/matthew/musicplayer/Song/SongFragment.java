package com.barnewall.matthew.musicplayer.Song;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import com.barnewall.matthew.musicplayer.GlobalFunctions;
import com.barnewall.matthew.musicplayer.MainActivity;
import com.barnewall.matthew.musicplayer.MusicFragment;
import com.barnewall.matthew.musicplayer.R;
import com.barnewall.matthew.musicplayer.Song.SongAdapter;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.util.ArrayList;

/**
 * Created by Matthew on 2/29/2016.
 */
public class SongFragment extends MusicFragment {
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_song, container, false);
    }

    // whereParams.length - 1 must be: bitmapid for album or genreid for genre
    @Override
    public void populateListView(String where, String[] whereParams, MainActivity.MusicCategories category) {

        // Select songTitle, albumId, duration, artistName, fileLocation, trackNumber, album name
        String[] columns = new String[] {MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ALBUM};

        String orderBy = MediaStore.Audio.Media.TITLE + " ASC";
        boolean hideTrackNumber = true;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch(category){
            case ALBUMS:
                // Set the album artwork
                ImageView iv = (ImageView) getView().findViewById(R.id.albumImageView);
                iv.setVisibility(View.VISIBLE);
                iv.setImageBitmap(GlobalFunctions.getBitmapFromID(Long.parseLong(whereParams[whereParams.length - 1]), 200, getActivity()));
                orderBy = MediaStore.Audio.Media.TRACK + " ASC";
                hideTrackNumber = false;
                break;
            default:
                break;
        }

        ContentResolver musicResolver   = getActivity().getContentResolver();

        // Query the database
        Cursor musicCursor = musicResolver.query(
                uri                                 // Database
                ,columns                            // Columns
                ,where                              // Where
                ,whereParams                        // Where variables
                ,orderBy);                          // Order By

        // House song objects
        ArrayList<SongListViewItem> songs = new ArrayList<SongListViewItem>();

        // Navigate through query results
        if(musicCursor!= null && musicCursor.moveToFirst()){
            String trackNum = null;
            do{
                // Add the track number
                if(!hideTrackNumber) {
                    if (musicCursor.getString(5) != null && musicCursor.getString(5) != "" && musicCursor.getString(5).length() == 4) {
                        trackNum = removeDigitsFromTrack(musicCursor.getString(5));
                    } else {
                        trackNum = musicCursor.getString(5);
                    }
                    if(trackNum == ""){
                        trackNum = "0";
                    }
                }

                // Create song object and add to arraylist
                songs.add(new SongListViewItem(musicCursor.getString(0)
                                ,musicCursor.getLong(1)
                                , musicCursor.getString(3)
                                ,musicCursor.getString(4)
                                ,trackNum
                                ,musicCursor.getString(6)
                                ,msToMin(musicCursor.getString(2)))
                );
            }
            while (musicCursor.moveToNext());
            musicCursor.close();

            // Add adapter
            final ArrayList<SongListViewItem> items = new ArrayList<SongListViewItem>(songs);
            final SongAdapter adapter = new SongAdapter(songs, getActivity());
            ListView listView = (ListView) getView().findViewById(R.id.songListView);
            listView.setAdapter(adapter);
            listView.setFastScrollEnabled(true);

            // Set OnItemClickListener
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    getMListener().handleSongOnClick(items, position);
                }
            });
        }

    }

    public static String msToMin(String ms){
        try {
            int milli = Integer.parseInt(ms);
            milli = milli / 1000;
            int rem = milli % 60;
            String remain = Integer.toString(rem);
            if (Integer.toString(rem).length() == 1) {
                remain = "0" + rem;
            }
            milli = milli / 60;
            return milli + ":" + remain;
        }
        catch(Exception e){
            return "";
        }
    }

    // Removes leading zeros
    private String removeDigitsFromTrack(String trackNumber){
        trackNumber = trackNumber.substring(1);
        if(trackNumber.length() >= 1){
            if(trackNumber.substring(0,1).equals("0")){
                trackNumber = trackNumber.substring(1);
            }
        }
        if(trackNumber.length() >= 1){
            if(trackNumber.substring(0,1).equals("0")){
                trackNumber = trackNumber.substring(1);
            }
        }
        return trackNumber;
    }
}
