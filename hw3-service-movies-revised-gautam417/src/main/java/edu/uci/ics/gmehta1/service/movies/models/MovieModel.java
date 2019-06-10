package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.Movie;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class MovieModel {
    private String movieId;
    private String title;
    private String director;
    private int year;
    private float rating;
    private int numVotes;
    private boolean hidden;
    @JsonCreator
    public MovieModel
            (
                    @JsonProperty(value = "movieId", required = true) String movieId,
                    @JsonProperty(value = "title", required = true) String title,
                    @JsonProperty(value = "director", required = true) String director,
                    @JsonProperty(value = "year", required = true) int year,
                    @JsonProperty(value = "rating", required = true) float rating,
                    @JsonProperty(value = "numVotes", required = true) int numVotes,
                    @JsonProperty(value = "hidden") boolean hidden
            )
    {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.year = year;
        this.rating = rating;
        this.numVotes = numVotes;
        this.hidden = hidden;
    }
    public static MovieModel buildModelFromObject(Movie m) {
        return new MovieModel(m.getMovieId(), m.getTitle(), m.getDirector(),
                m.getYear(), m.getRating(), m.getNumVotes(),m.isHidden());
    }
    @JsonProperty("movieId")
    public String getMovieId() { return movieId; }
    @JsonProperty("title")
    public String getTitle() { return title; }
    @JsonProperty("director")
    public String getDirector() { return director; }
    @JsonProperty("year")
    public int getYear() { return year; }
    @JsonProperty("rating")
    public float getRating() { return rating; }
    @JsonProperty("numVotes")
    public int getNumVotes() { return numVotes; }
    @JsonProperty("hidden")
    public boolean getHidden() { return hidden; }

}
