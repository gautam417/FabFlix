package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddRequestModel extends RequestModel {
    private String title;
    private String director;
    private int year;
    private String backdrop_path;
    private int budget;
    private String overview;
    private String poster_path;
    private int revenue;
    private GenreModel[] genres;

    @JsonCreator
    public AddRequestModel(
            @JsonProperty(value = "title",required = true) String title,
            @JsonProperty(value = "year",required = true) int year,
            @JsonProperty(value = "director",required = true) String director,
            @JsonProperty(value = "backdrop_path") String backdrop_path,
            @JsonProperty(value = "budget") int budget,
            @JsonProperty(value = "overview") String overview,
            @JsonProperty(value = "poster_path") String poster_path,
            @JsonProperty(value = "revenue") int revenue,
            @JsonProperty(value = "genres",required = true) GenreModel[] genres
    )
    {
        this.title = title;
        this.year = year;
        this.director = director;
        this.backdrop_path = backdrop_path;
        this.budget = budget;
        this.overview = overview;
        this.poster_path = poster_path;
        this.revenue = revenue;
        this.genres = genres;
    }
    @JsonProperty
    public String getTitle() { return title; }
    @JsonProperty
    public String getDirector() { return director; }
    @JsonProperty
    public int getYear() { return year; }
    @JsonProperty
    public String getBackdrop_path() { return backdrop_path; }
    @JsonProperty
    public int getBudget() { return budget; }
    @JsonProperty
    public String getOverview() { return overview; }
    @JsonProperty
    public String getPoster_path() { return poster_path; }
    @JsonProperty
    public int getRevenue() { return revenue; }
    @JsonProperty
    public GenreModel[] getGenres() { return genres; }
}
