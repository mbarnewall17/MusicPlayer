package com.barnewall.matthew.musicplayer.Album;

import android.graphics.Bitmap;

/**
 * Created by Matthew on 8/5/2015.
 */
public class AlbumListViewItem implements Comparable<AlbumListViewItem>{
    private long    albumID;
    private String  artistName;
    private String  title;
    private Bitmap  albumArt;
    private boolean subQuery;
    private String    artistID;
    private String  year;


    public AlbumListViewItem(long albumID, String artistName, String title
    ,Bitmap albumArt, String artistID){
        this.albumID                    = albumID;
        this.artistName                 = artistName;
        this.title                      = title;
        this.albumArt                   = albumArt;
        this.artistID                   = artistID;
        subQuery = false;
    }

    public String getArtistID() { return artistID;}

    public long getAlbumID() {
        return albumID;
    }

    public void setAlbumID(long albumID) {
        this.albumID = albumID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Bitmap getAlbumArt() {
        return albumArt;
    }

    public void setAlbumArt(Bitmap albumArt) {
        this.albumArt = albumArt;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public void setYear(String other){
        this.year = other;
    }

    public String getYear(){
        return year;
    }

    public void setSubQuery(boolean bool){
        subQuery = bool;
    }


    @Override
    public int compareTo(AlbumListViewItem another) {
        if(subQuery && this.getArtistID() == another.getArtistID())
            return another.getTitle().compareTo(this.getTitle());
        else{
            if(compareTitles(another) == 0 && another.getArtistID() != this.getArtistID()) {
                String thisVal = this.getArtistName() + this.getYear();
                String anotherVal = another.getArtistName() + this.getYear();
                return thisVal.compareTo(anotherVal);
            }
            return compareTitles(another);
        }
    }

    public int compareTitles(AlbumListViewItem another){
        String firstTitle = this.getTitle().toLowerCase();
        String secondTitle = another.getTitle().toLowerCase();

        if(firstTitle.length() > 5){
            if(firstTitle.substring(0, 4).equals("the "))
                firstTitle = firstTitle.substring(4);
            else if(firstTitle.substring(0, 2).equals("a "))
                firstTitle = firstTitle.substring(2);
        }

        if(secondTitle.length() > 5){
            if(secondTitle.substring(0, 4).equals("the ")){
                secondTitle = secondTitle.substring(4);
            }
            else if(secondTitle.substring(0, 2).equals("a ")){
                secondTitle = secondTitle.substring(2);
            }
        }

        return firstTitle.compareTo(secondTitle);
    }
}