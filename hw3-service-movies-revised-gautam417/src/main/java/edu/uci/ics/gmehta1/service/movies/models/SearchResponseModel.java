package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.Movie;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class SearchResponseModel {
    private int resultCode;
    private String message;
    private MovieModel[] movies;
    // THIS MOVIE MODEL IS DIFFERENT THAN THE ONE IN GET
    public SearchResponseModel(
            @JsonProperty(value = "resultCode", required = true) int resultCode,
            @JsonProperty(value = "message", required = true) String message,
            @JsonProperty(value = "movies", required = true) MovieModel[] movies)
    {
        this.resultCode = resultCode;
        this.message = message;
        this.movies = movies;
    }
    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("movies")
    public MovieModel[] getMovies() {return movies;}

    public static SearchResponseModel buildModelFromList (ArrayList<Movie> movies) {
        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
        // Must convert arraylist to array.
        ServiceLogger.LOGGER.info("Creating model...");
        if (movies.size() == 0) {
            ServiceLogger.LOGGER.info("No movie list passed to model constructor.");
            return new SearchResponseModel(211,"No movies found with search parameters.",null);
        }
        ServiceLogger.LOGGER.info("Movies list is not empty...");
        int len = movies.size();
        ServiceLogger.LOGGER.info("Len of Movies list: " + len);
        MovieModel[] array = new MovieModel[len];
        for (int i = 0; i < len; ++i) {
            ServiceLogger.LOGGER.info("Adding movie " + movies.get(i).getTitle() + " to array.");
            // Convert each student in the arraylist to a StudentModel
            MovieModel mm = MovieModel.buildModelFromObject(movies.get(i));
            // If the new model has valid data, add it to array
            array[i] = mm;
        }

        return new SearchResponseModel( 210, "Found movies with search parameters.", array);
    }
}
