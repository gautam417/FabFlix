package edu.uci.ics.gmehta1.service.movies.core;

import edu.uci.ics.gmehta1.service.movies.models.GenreModel;
import edu.uci.ics.gmehta1.service.movies.models.StarModel;

public class GetMovie {
    private String movieId;
    private String title;
    private String director;
    private int year;
    private String backdrop_path;
    private int budget;
    private String overview;
    private String poster_path;
    private int revenue;
    private float rating;
    private int numVotes;
    private GenreModel[] genres;
    private StarModel[] stars;

    public GetMovie(String movieId, String title, String director, int year, String backdrop_path, int budget, String overview, String poster_path, int revenue, float rating, int numVotes, GenreModel[] genres, StarModel[] stars) {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.year = year;
        this.backdrop_path = backdrop_path;
        this.budget = budget;
        this.overview = overview;
        this.poster_path = poster_path;
        this.revenue = revenue;
        this.rating = rating;
        this.numVotes = numVotes;
        this.genres = genres;
        this.stars = stars;
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

    public String getBackdrop_path() {
        return backdrop_path;
    }

    public int getBudget() {
        return budget;
    }

    public String getOverview() {
        return overview;
    }

    public String getPoster_path() {
        return poster_path;
    }

    public int getRevenue() {
        return revenue;
    }

    public float getRating() {
        return rating;
    }

    public int getNumVotes() {
        return numVotes;
    }

    public GenreModel[] getGenres() {
        return genres;
    }

    public StarModel[] getStars() {
        return stars;
    }
}
