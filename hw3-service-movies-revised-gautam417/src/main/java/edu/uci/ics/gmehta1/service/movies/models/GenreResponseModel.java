package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.Genre;
import edu.uci.ics.gmehta1.service.movies.logger.ServiceLogger;

import java.util.ArrayList;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenreResponseModel {
    private int resultCode;
    private String message;
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private GenreModel [] genres;
    public GenreResponseModel( @JsonProperty(value = "resultCode", required = true) int resultCode,
                               @JsonProperty(value = "message", required = true) String message,
                               @JsonProperty(value = "genres") GenreModel[] genres
                               )
    {
        this.resultCode = resultCode;
        this.message = message;
        this.genres = genres;
    }

    @JsonProperty("resultCode")
    public int getResultCode(){return resultCode;}
    @JsonProperty("message")
    public String getMessage() {return message;}
    @JsonProperty("genres")
    public GenreModel[] getGenres() { return genres; }
//    public static GenreResponseModel buildModelFromList (ArrayList<Star> stars) {
//        // Jackson cannot convert complex data structures to text. It can convert arrays of objects.
//        // Must convert arraylist to array.
//        ServiceLogger.LOGGER.info("Creating model...");
//        if (stars == null) {
//            ServiceLogger.LOGGER.info("No movie list passed to model constructor.");
//            return new StarResponseModel(null);
//        }
//        ServiceLogger.LOGGER.info("Movies list is not empty...");
//        int len = stars.size();
//        StarModel[] array = new StarModel[len];
//        for (int i = 0; i < len; ++i) {
//            ServiceLogger.LOGGER.info("Adding star " + stars.get(i).getName() + " to array.");
//            // Convert each student in the arraylist to a StudentModel
//            StarModel sm = StarModel.buildModelFromObject(stars.get(i));
//            // If the new model has valid data, add it to array
//            array[i] = sm;
//        }
//        ServiceLogger.LOGGER.info("Finished building model. Array of movies contains: ");
//        for (StarModel sm : array) {
//            ServiceLogger.LOGGER.info("\t" + sm);
//        }
//        return new StarResponseModel(array);
//    }
//    @Override
//    public boolean isValid() {
//        // If movie[] is null, return false
//        ServiceLogger.LOGGER.info("movies != null ? " + (stars != null));
//        ServiceLogger.LOGGER.info("movies.length > 0 ? " + (stars.length > 0));
//        return (stars != null) || (stars.length > 0);
//    }
}