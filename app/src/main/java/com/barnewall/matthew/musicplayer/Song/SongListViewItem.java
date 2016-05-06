package com.barnewall.matthew.musicplayer.Song;

import java.io.Serializable;

/**
 * Created by Matthew on 8/5/2015.
 */
public class SongListViewItem implements Serializable, Comparable<SongListViewItem>{
    private String title;
    private Long albumID;
    private String artistName;
    private String dataLocation;
    private String trackNumber;
    private String albumName;
    private String    duration;
    private boolean isAnimated;
    private String artistID;

    public SongListViewItem(String title, Long albumID,String artistName, String dataLocation,
                            String trackNumber, String albumName, String duration, String artistID){
        this.title                  = title;
        this.albumID                = albumID;
        this.artistName             = artistName;
        this.dataLocation           = dataLocation;
        this.trackNumber            = trackNumber;
        this.albumName              = albumName;
        this.duration               = duration;
        this.isAnimated             = false;
        this.artistID               = artistID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getAlbumID() {
        return albumID;
    }

    public void setAlbumKey(long albumID) {
        this.albumID = albumID;
    }

    public String getDataLocation() {
        return dataLocation;
    }

    public void setDataLocation(String dataLocation) {
        this.dataLocation = dataLocation;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistNameAndDuration) {
        this.artistName = artistNameAndDuration;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getAlbumName(){
        return albumName;
    }

    public String getDuration(){
        return duration;
    }

    public boolean isAnimated(){
        return isAnimated;
    }

    public void setAnimated(boolean isAnimated){
        this.isAnimated = isAnimated;
    }

    public String getArtistID(){
        return artistID;
    }

    @Override
    public int compareTo(SongListViewItem other){
        if(title == other.getTitle() && albumID == other.getAlbumID() && artistID == other.getArtistID()){
            return 0;
        }
        else if(title == other.getTitle() && albumID == other.getAlbumID()){
            return artistName.compareTo(other.getArtistName());
        }
        else if(title == other.getTitle() && artistID == other.getArtistID()){
            return albumName.compareTo(other.getAlbumName());
        }
        else{
            return title.compareTo(other.getTitle());
        }
    }

    @Override
    public String toString(){
        return artistName + "-" + title;
    }
}
