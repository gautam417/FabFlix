package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class SearchRequestModel {
    private String title;
    private String genre;
    private int year;
    private String director;
    private boolean hidden;
    private int limit;
    private int offset;
    private String orderby;
    private String direction;

    @JsonCreator
    public SearchRequestModel(
            @JsonProperty(value = "title") String title,
            @JsonProperty(value = "genre") String genre,
            @JsonProperty(value = "year") int year,
            @JsonProperty(value = "director") String director,
            @JsonProperty(value = "hidden") boolean hidden,
            @JsonProperty(value = "offset") int offset,
            @JsonProperty(value = "limit") int limit,
            @JsonProperty(value = "direction") String direction,
            @JsonProperty(value = "orderby") String orderby
    )
    {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.director = director;
        this.hidden = hidden;
        this.offset = offset;
        this.limit = limit;
        this.direction = direction;
        this.orderby = orderby;
    }
    @JsonProperty
    public String getTitle() { return title; }
    @JsonProperty
    public String getGenre() { return genre; }
    @JsonProperty
    public int getYear() { return year; }
    @JsonProperty
    public String getDirector() { return director; }
    @JsonProperty
    public boolean isHidden() { return hidden; }
    @JsonProperty
    public int getOffset() { return offset; }
    @JsonProperty
    public int getLimit() { return limit; }
    @JsonProperty
    public String getDirection() { return direction; }
    @JsonProperty
    public String getOrderby() { return orderby; }

}
