package edu.uci.ics.gmehta1.service.billing.models;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class DeleteRequestModel {
    private String email;
    private String movieId;

    @JsonCreator
    public DeleteRequestModel(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "movieId", required = true) String movieId)
    {
        this.email = email;
        this.movieId = movieId;
    }
    @JsonProperty
    public String getEmail() { return email; }
    @JsonProperty
    public String getMovieId() { return movieId; }
}
