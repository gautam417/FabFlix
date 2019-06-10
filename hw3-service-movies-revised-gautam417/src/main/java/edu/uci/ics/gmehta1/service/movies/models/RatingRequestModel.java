package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RatingRequestModel {
    private String id;
    private float rating;

    @JsonCreator
    public RatingRequestModel(@JsonProperty(value = "id",required = true) String id,
                               @JsonProperty(value = "rating",required = true) float rating)
    {
        this.id = id;
        this.rating= rating;
    }
    @JsonProperty
    public String getId() { return id; }
    @JsonProperty
    public float getRating() { return rating; }
}
