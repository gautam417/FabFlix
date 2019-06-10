package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.GetMovie;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class GetMovieModel {
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
    private GenreModel [] genres;
    private StarModel [] stars;
//    private boolean hidden;

    @JsonCreator
    public GetMovieModel
            (
                    @JsonProperty(value = "movieId", required = true) String movieId,
                    @JsonProperty(value = "title", required = true) String title,
                    @JsonProperty(value = "director") String director,
                    @JsonProperty(value = "year") int year,
                    @JsonProperty(value = "backdrop_path") String backdrop_path,
                    @JsonProperty(value = "budget") int budget,
                    @JsonProperty(value = "overview") String overview,
                    @JsonProperty(value = "poster_path") String poster_path,
                    @JsonProperty(value = "revenue") int revenue,
                    @JsonProperty(value = "rating", required = true) float rating,
                    @JsonProperty(value = "numVotes") int numVotes,
                    @JsonProperty(value = "genres", required = true) GenreModel[] genres,
                    @JsonProperty(value = "stars", required = true) StarModel[] stars
                    )//  @JsonProperty(value = "hidden") boolean hidden
    {
        this.movieId = movieId;
        this.title = title;
        this.director = director;
        this.year = year;
        this.backdrop_path =backdrop_path;
        this.budget = budget;
        this.overview = overview;
        this.rating = rating;
        this.numVotes = numVotes;
        this.poster_path = poster_path;
        this.revenue = revenue;
        this.genres = genres;
        this.stars = stars;
//        this.hidden = hidden;
    }
    public static GetMovieModel buildModelFromObject(GetMovie m) {
        return new GetMovieModel(m.getMovieId(), m.getTitle(), m.getDirector(),
                m.getYear(), m.getBackdrop_path(), m.getBudget(), m.getOverview(),
                m.getPoster_path(), m.getRevenue(), m.getRating(), m.getNumVotes(),m.getGenres(),m.getStars());
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
//    @JsonProperty("hidden")
//    public boolean getHidden() { return hidden; }
    @JsonProperty("backdrop_path")
    public String getBackdrop_path() { return backdrop_path; }
    @JsonProperty("budget")
    public int getBudget() { return budget; }
    @JsonProperty("overview")
    public String getOverview() { return overview; }
    @JsonProperty("poster_path")
    public String getPoster_path() { return poster_path; }
    @JsonProperty("revenue")
    public int getRevenue() { return revenue; }
    @JsonProperty("genres")
    public GenreModel[] getGenres() { return genres; }
    @JsonProperty("stars")
    public StarModel[] getStars() { return stars; }
}
