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
        if(!file.exists()){
            Log.d("AAA", "doesnt exist");
            try {
                file.createNewFile();
                Log.d("AAA", file.getPath());
                BufferedWriter writer = new BufferedWriter(new FileWriter(file));
                writer.write("#EXTM3U\n");
                writer.write("#name=" + file.getName() + "\n");
                writer.write("#name=" + file.getAbsolutePath() + "\n");
                writer.close();

                MediaScannerConnection.scanFile(
                        context,
                        new String[]{file.getAbsolutePath()},
                        null,
                        null);
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }

    }

    public ArrayList<String> getSongs(){
        ArrayList<String> entries = new ArrayList<String>();

        try{
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line = bufferedReader.readLine()) != null){
                try {
                    if (line.charAt(0) == '/') {
                        entries.add(line);

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

    public void createPlaylist(ArrayList<String> songs){

    }

    public boolean addSongsToPlaylist(ArrayList<SongListViewItem> songs){
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
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
