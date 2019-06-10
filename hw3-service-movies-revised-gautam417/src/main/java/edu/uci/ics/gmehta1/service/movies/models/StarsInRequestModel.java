package edu.uci.ics.gmehta1.service.movies.models;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarsInRequestModel {
    private String starid;
    private String movieId;

    @JsonCreator
    public StarsInRequestModel(@JsonProperty(value = "starId",required = true) String starid,
                               @JsonProperty(value = "movieId",required = true) String movieId)
    {
        this.starid = starid;
        this.movieId= movieId;
    }
    @JsonProperty
    public String getStarid() { return starid; }
    @JsonProperty
    public String getMovieId() { return movieId; }
}


