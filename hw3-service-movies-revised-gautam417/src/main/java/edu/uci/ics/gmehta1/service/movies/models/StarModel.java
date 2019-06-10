package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.*;
import edu.uci.ics.gmehta1.service.movies.core.Star;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarModel {
    private String id;
    private String name;
    @JsonInclude(JsonInclude.Include.NON_DEFAULT)
    private int birthYear;
    @JsonCreator
    public StarModel
            (@JsonProperty(value = "id", required = true) String id,
             @JsonProperty(value = "name", required = true) String name,
             @JsonProperty(value = "birthYear") int birthYear)
    {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }
    @JsonProperty("id")
    public String getId() { return id; }
    @JsonProperty("name")
    public String getName() { return name; }
    @JsonProperty("birthYear")
    public int getBirthYear() { return birthYear; }

    public static StarModel buildModelFromObject(Star s) {
        return new StarModel(s.getId(),s.getName(),s.getBirthYear());
    }
}
