package com.mona.shamsolebad.artistsfirebase;

import com.google.firebase.firestore.Exclude;

public class Track {

    private String id;
    private String artistId;
    private String title;
    private int rating;

    public Track() {
    }

    public Track(String artistId, String title, int rating) {
        this.artistId = artistId;
        this.title = title;
        this.rating = rating;
    }
    @Exclude //to make sure the id does not get sent to firestore on save
    public String getId(){return id;}

    public void setId(String id) {
        this.id = id;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }
}
