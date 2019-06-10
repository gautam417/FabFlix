package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenreAddRequestModel {
    private String name;

    @JsonCreator
    public GenreAddRequestModel(@JsonProperty(value = "name",required = true) String name)
    {
        this.name = name;
    }
    @JsonProperty
    public String getName() { return name; }
}
