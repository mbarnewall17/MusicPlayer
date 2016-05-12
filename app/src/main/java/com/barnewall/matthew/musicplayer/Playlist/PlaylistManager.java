package com.barnewall.matthew.musicplayer.Playlist;

import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.util.Log;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Matthew on 5/2/2016.
 */
public class PlaylistManager {
    // Instance Variables
    private String  playlistFilepath;
    private File    file;

    public PlaylistManager(String playlistFilepath, Context context){

        this.playlistFilepath = playlistFilepath;
        file = new File(playlistFilepath);

        // Create the playlist if it does not exist and scan it
        if(!file.exists()){

            try {
                file.createNewFile();

                // Write the header of .m3u file
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("#EXTM3U\n");
                writer.write("#name=" + file.getName() + "\n");
                writer.write("#name=" + file.getAbsolutePath() + "\n");
                writer.close();

                // Scan the file into to MediaStore
                MediaScannerConnection.scanFile(context, new String[]{file.getAbsolutePath()}, null, null);

            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    /*
     * Gets a list of all songs in the playlist
     *
     * @return  entries An ArrayList of songs(file paths) in the playlist
     */
    public ArrayList<String> getSongs(){
        ArrayList<String> entries = new ArrayList<String>();

        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;

            // Read each line, if it starts with /, it is a song
            while((line = bufferedReader.readLine()) != null){
                try {
                    if (line.charAt(0) == '/') {
                        entries.add(line.replace("'","''"));

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return entries;
    }

    /*
     * Adds the songs passed in to the playlist
     *
     * @param   songs   An ArrayList of songs to add the the playlist
     * @return  boolean A boolean indicating if the songs were successfully added
     */
    public boolean addSongsToPlaylist(ArrayList<SongListViewItem> songs){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));

            // Write each song to the playlist, by appending
            for(SongListViewItem song: songs) {
                writer.write(song.getDataLocation() + "\n");
            }
            writer.close();
            return true;
        }
        catch(IOException e){
            e.printStackTrace();
            return false;
        }
    }
}
