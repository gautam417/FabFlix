package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.GetMovie;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class GetResponseModel {
    private int resultCode;
    private String message;
    private GetMovieModel[] movie;
    // THIS MOVIE MODEL IS DIFFERENT THAN THE ONE IN GET
    public GetResponseModel(
            @JsonProperty(value = "resultCode", required = true) int resultCode,
            @JsonProperty(value = "message", required = true) String message,
            @JsonProperty(value = "movie", required = true) GetMovieModel[] movie)
    {
        this.resultCode = resultCode;
        this.message = message;
        this.movie = movie;
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("movie")
    public GetMovieModel[] getMovie() {return movie;}

    public static GetResponseModel buildModelFromList (ArrayList<GetMovie> movies) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");
        if (movies == null) {
            ServiceLogger.LOGGER.info("No movie list passed to model constructor.");
            return new GetResponseModel(211,"No movies found with search parameters.",null);
        }
        ServiceLogger.LOGGER.info("Movies list is not empty...");
        int len = movies.size();
        GetMovieModel[] array = new GetMovieModel[len];
        for (int i = 0; i < len; ++i) {
            ServiceLogger.LOGGER.info("Adding student " + movies.get(i).getTitle() + " to array.");
            // Convert each student in the arraylist to a StudentModel
            GetMovieModel mm = GetMovieModel.buildModelFromObject(movies.get(i));
            // If the new model has valid data, add it to array
            array[i] = mm;
        }
        return new GetResponseModel(210, "Found movies with search parameters.",array);
    }
}
