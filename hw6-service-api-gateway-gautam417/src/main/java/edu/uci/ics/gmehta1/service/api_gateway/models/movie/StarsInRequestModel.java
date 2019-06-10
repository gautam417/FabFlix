package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarsInRequestModel extends RequestModel {
    private String starid;
    private String movieId;

    @JsonCreator
    public StarsInRequestModel(@JsonProperty(value = "starid",required = true) String starid,
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


