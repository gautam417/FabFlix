package edu.uci.ics.gmehta1.service.movies.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarAddRequestModel {
    private String name;
    private int birthYear;

    @JsonCreator
    public StarAddRequestModel(@JsonProperty(value = "name",required = true) String name,
                               @JsonProperty(value = "birthYear") int birthYear)
    {
        this.name = name;
        this.birthYear= birthYear;
    }
    @JsonProperty
    public String getName() { return name; }
    @JsonProperty
    public int getBirthYear() { return birthYear; }
    @JsonProperty
    public void setBirthYear(int birthYear) {
        this.birthYear = birthYear;
    }
}
