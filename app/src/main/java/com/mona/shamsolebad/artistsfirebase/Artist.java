package com.mona.shamsolebad.artistsfirebase;



import com.google.firebase.Timestamp;
import com.google.firebase.firestore.Exclude;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;

public class Artist {
    private String id;
    private String name;
    private String genre;
    private Timestamp addedDate;


    public Artist(){
    }

    public Artist(String name,String genre){

        this.name=name;
        this.genre=genre;
        this.addedDate=new Timestamp(new Date());
    }
    @Exclude //to make sure the id does not get sent to firestore on save
    public String getId(){return id;}

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getGenre() {
        return genre;
    }

    public Timestamp getAddedDate() {
        return addedDate;
    }

}
