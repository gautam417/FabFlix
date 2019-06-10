package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.Movie;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class AddResponseModel {
    private int resultCode;
    private String message;
    private String movieid;
//    private int[] genreid;
    private GenreModel[] genreid;

    public AddResponseModel(@JsonProperty(value = "resultCode", required = true) int resultCode,
                            @JsonProperty(value = "message", required = true) String message,
                            @JsonProperty(value = "movieid", required = true) String movieid,
                            @JsonProperty(value = "genreid", required = true) GenreModel[] genreid)
    {
        this.resultCode = resultCode;
        this.message = message;
        this.movieid = movieid;
        this.genreid = genreid;
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("movieid")
    public String getMovieid() { return movieid; }
    @JsonProperty("genreid")
    public GenreModel[] getGenreid() { return genreid; }
}
