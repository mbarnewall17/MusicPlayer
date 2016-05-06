package com.barnewall.matthew.musicplayer.Playlist;

import android.media.MediaMetadataRetriever;
import android.util.Log;
import android.widget.Toast;

import com.barnewall.matthew.musicplayer.Song.SongFragment;
import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by Matthew on 5/2/2016.
 */
public class PlayListParser {
    // Instance Variables
    private String  data;

    public PlayListParser(String data){
        this.data = data;
    }

    public String getEntries(){
        String entries = "(";

        try{
            File file = new File(data);
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line;
            while((line = bufferedReader.readLine()) != null){
                try {
                    if (line.charAt(0) == '/') {
                        entries = entries + "'" + line + "',";

//                        File song = new File(line);
//                        AudioFile audioFile = AudioFileIO.read(song);
//                        Tag tag = audioFile.getTag();
//                        entries.add(new SongListViewItem(tag.getFirst(FieldKey.TITLE),
//                                0l,
//                                tag.getFirst(FieldKey.ARTIST),
//                                song.getAbsolutePath(),
//                                tag.getFirst(FieldKey.TRACK),
//                                tag.getFirst(FieldKey.ALBUM),
//                                SongFragment.msToMin(Integer.toString(audioFile.getAudioHeader().getTrackLength() * 1000)),
//                                "0"));

                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
            entries = entries.substring(0,entries.length() - 1) + ")";

        }
        catch (Exception e){
            e.printStackTrace();
        }

        return entries;
    }
}
