package com.barnewall.matthew.musicplayer;

import com.barnewall.matthew.musicplayer.Song.SongListViewItem;

/**
 * Created by Matthew on 8/18/2015.
 */
public interface NewSongListener {
    public void loadNewSongInfo(SongListViewItem newSong);
    public void onFinish();
}
