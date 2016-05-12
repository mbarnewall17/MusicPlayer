package com.barnewall.matthew.musicplayer.Song;

import android.app.Activity;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;

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

        // Get the songs
        ArrayList<SongListViewItem> songs = getSongs(where,whereParams,category,getActivity(), true);

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

    public static ArrayList<SongListViewItem> getSongs(String where, String[] whereParams
            , MainActivity.MusicCategories category, Activity activity, boolean editUI){
        // Select songTitle, albumId, duration, artistName, fileLocation, trackNumber, album name
        String[] columns = new String[] {MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.TRACK,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ARTIST_ID};

        String orderBy = MediaStore.Audio.Media.TITLE + " ASC";
        boolean hideTrackNumber = true;
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        switch(category){
            case ALBUMS:
                if(editUI) {
                    // Set the album artwork
                    ImageView iv = (ImageView) activity.findViewById(R.id.albumImageView);
                    iv.setVisibility(View.VISIBLE);
                    iv.setImageBitmap(GlobalFunctions.getBitmapFromID(Long.parseLong(whereParams[whereParams.length - 1]), 200, activity));
                }
                orderBy = MediaStore.Audio.Media.TRACK + " ASC";
                hideTrackNumber = false;
                break;
            case ARTISTS:
                orderBy = MediaStore.Audio.Media.ALBUM + ", " + MediaStore.Audio.Media.TRACK + " ASC";
                break;
            case GENRES:
                uri = MediaStore.Audio.Genres.Members.getContentUri("external", Integer.parseInt(whereParams[whereParams.length - 1]));
                whereParams = Arrays.copyOfRange(whereParams, 0, whereParams.length - 1);
                orderBy = MediaStore.Audio.Media.ALBUM + "," + MediaStore.Audio.Media.TRACK + " ASC";
                break;
            case PLAYLISTS:
                break;
            default:
                break;
        }

        ContentResolver musicResolver   = activity.getContentResolver();

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
        if(musicCursor!= null && musicCursor.moveToFirst()) {
            String trackNum = null;
            do {
                // Add the track number
                if (!hideTrackNumber) {
                    if (musicCursor.getString(5) != null && musicCursor.getString(5) != "" && musicCursor.getString(5).length() == 4) {
                        trackNum = removeDigitsFromTrack(musicCursor.getString(5));
                    } else {
                        trackNum = musicCursor.getString(5);
                    }
                    if (trackNum == "") {
                        trackNum = "";
                    }
                }

                // Create song object and add to arraylist
                songs.add(new SongListViewItem(musicCursor.getString(0)
                                , musicCursor.getLong(1)
                                , musicCursor.getString(3)
                                , musicCursor.getString(4)
                                , trackNum
                                , musicCursor.getString(6)
                                , msToMin(musicCursor.getString(2))
                                , musicCursor.getString(7))
                );
            }
            while (musicCursor.moveToNext());
            musicCursor.close();
        }

        // Put the songs in the correct order of the playlist
        if(category == MainActivity.MusicCategories.PLAYLISTS){
            where = where.substring(10,where.length() - 1);
            ArrayList<String> order = new ArrayList(Arrays.asList(where.split(", ")));

            ArrayList<SongListViewItem> temp = new ArrayList<SongListViewItem>(order.size());
            for(int i = 0; i < order.size(); i++){
                temp.add(null);
            }

            for(SongListViewItem s : songs){
                temp.set(order.indexOf("'" + s.getDataLocation().replace("'","''") + "'"),s);
            }

            songs = temp;

            for(int i = 0; i < songs.size(); i++){
                if(songs.get(i) == null){
                    songs.remove(i);
                    i--;
                }
            }
        }
        return songs;
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
    private static String removeDigitsFromTrack(String trackNumber){
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
