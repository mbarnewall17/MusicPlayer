package com.barnewall.matthew.musicplayer.Playlist;

/**
 * Created by Matthew on 8/5/2015.
 */
public class PlaylistListViewItem {
    private String name;
    private String path;

    public PlaylistListViewItem(String name, String path){
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
