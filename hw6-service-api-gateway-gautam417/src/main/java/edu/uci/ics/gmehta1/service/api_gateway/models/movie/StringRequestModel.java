package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;
@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class StringRequestModel extends RequestModel {
    private String title;

//    @JsonCreator
    public StringRequestModel(String title)
    {
        this.title = title;
    }
//    @JsonProperty
    public String getTitle() { return title; }
}
