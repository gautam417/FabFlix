package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class StarRequestModel {
    private String name;
    private int birthYear;
    private String movieTitle;
    private int offset;
    private int limit;
    private String direction;
    private String orderby;

    @JsonCreator
    public StarRequestModel
            (@JsonProperty(value = "name") String name,
             @JsonProperty(value = "birthYear") int birthYear,
             @JsonProperty(value = "movieTitle") String movieTitle,
             @JsonProperty(value = "offset") int offset,
             @JsonProperty(value = "limit") int limit,
             @JsonProperty(value = "direction") String direction,
             @JsonProperty(value = "orderby") String orderby
            )
    {
        this.name = name;
        this.birthYear = birthYear;
        this.movieTitle = movieTitle;
        this.offset = offset;
        this.limit = limit;
        this.direction = direction;
        this.orderby = orderby;
    }
    @JsonProperty
    public String getName() { return name; }
    @JsonProperty
    public int getBirthYear() { return birthYear; }
    @JsonProperty
    public String getMovieTitle() { return movieTitle; }
    @JsonProperty
    public int getOffset() { return offset; }
    @JsonProperty
    public int getLimit() { return limit; }
    @JsonProperty
    public String getDirection() { return direction; }
    @JsonProperty
    public String getOrderby() { return orderby; }
}
