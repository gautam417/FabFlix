package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.movies.core.Genre;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class GenreModel {
    private int id;
    private String name;

    @JsonCreator
    public GenreModel(
            @JsonProperty(value = "id", required = true) int id,
            @JsonProperty(value = "name", required = true) String name)
    {
        this.id = id;
        this.name = name;
    }
    @JsonProperty("id")
    public int getId() { return id; }
    @JsonProperty("name")
    public String getName() { return name; }
    // Using a factory method to create an instance of MovieModel is more secure because it gives
    // ONLY YOU control over how the object is instantiated.
    public static GenreModel buildModelFromObject(Genre g) {
        return new GenreModel(g.getId(),g.getName());
    }

}
