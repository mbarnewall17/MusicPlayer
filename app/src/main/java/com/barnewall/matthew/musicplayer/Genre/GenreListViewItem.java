package com.barnewall.matthew.musicplayer.Genre;

/**
 * Created by Matthew on 8/5/2015.
 */
public class GenreListViewItem {
    private String name;
    private String id;

    public GenreListViewItem(String name, String id){
        this.name = name;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}
