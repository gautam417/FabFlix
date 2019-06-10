package edu.uci.ics.gmehta1.service.movies.core;

public class Movie {
    private String movieId;
    private String title;
    private String director;
    private int year;
    private float rating;
    private int numVotes;
    private boolean hidden;

    public Movie(String movieId, String title, String director, int year, float rating, int numVotes, boolean hidden) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.numVotes = numVotes;
        this.hidden = hidden;
    }

    public String getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public String getDirector() {
        return director;
    }

    public int getYear() {
        return year;
    }

    public float getRating() {
        return rating;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public boolean isHidden() {
        return hidden;
    }
}
