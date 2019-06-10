package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenreAddResponseModel {
    private int resultCode;
    private String message;
    public GenreAddResponseModel( @JsonProperty(value = "resultCode", required = true) int resultCode,
                               @JsonProperty(value = "message", required = true) String message)
    {
        this.resultCode = resultCode;
        this.message = message;
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
}
