package com.example.androidmusic;

import android.graphics.Bitmap;

public class Song {
    private int id;
    private String title;
    private String artist;
    private Bitmap portada;
    private String uri;

    public Song(String uri) {
        this.uri=uri;
    }

    public Song(String uri, String songTitle) {
        //id=songID;
        this.uri=uri;
        title=songTitle;
        //artist=songArtist;
    }

    public Song(String uri, String title, String artist, Bitmap portada, int codigo) {
        this.uri=uri;
        this.title=title;
        this.artist = artist;
        this.portada = portada;
        this.id = codigo;
    }

    public Song(int songID, String songTitle, String songArtist) {
        id=songID;
        title=songTitle;
        artist=songArtist;
    }

    public int getID(){return id;}
    public String getTitle(){return title;}
    public String getArtist(){return artist;}
    public Bitmap getPortada(){return portada;}
    public String getUri(){return uri;}

    public void setPortada(Bitmap portada){this.portada = portada;}
}
