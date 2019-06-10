package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GenreAddRequestModel extends RequestModel {
    private String name;
    private boolean memory[] = new ArrayList(300);

    @JsonCreator
    public GenreAddRequestModel(@JsonProperty(value = "name",required = true) String name)
    {
        this.name = name;
    }
    @JsonProperty
    public String getName() { return name; }
}
