package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class StarAddRequestModel extends RequestModel {
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
