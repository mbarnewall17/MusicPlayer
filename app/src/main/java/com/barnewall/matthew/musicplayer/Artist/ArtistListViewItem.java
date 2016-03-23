package com.barnewall.matthew.musicplayer.Artist;

import java.util.Comparator;

/**
 * Created by Matthew on 8/5/2015.
 */
public class ArtistListViewItem implements Comparable<ArtistListViewItem>{
    private String  name;
    private String  numAlbums;
    private String     artistID;

    public ArtistListViewItem(String name, String numAlbums, String artistID){
        this.name = name;
        this.numAlbums = numAlbums;
        this.artistID = artistID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumAlbums() {
        return numAlbums;
    }

    public void setNumAlbums(String numAlbums) {
        this.numAlbums = numAlbums;
    }

    public String getArtistID(){
        return artistID;
    }


    @Override
    public int compareTo(ArtistListViewItem another) {
        String firstTitle = this.getName().toLowerCase();
        String secondTitle = another.getName().toLowerCase();
        if(firstTitle.length() > 5){
            if(firstTitle.substring(0, 4).equals("the ")){
                firstTitle = firstTitle.substring(4);
            }
            else if(firstTitle.substring(0, 2).equals("a ")){
                firstTitle = firstTitle.substring(2);
            }
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
