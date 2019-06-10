package edu.uci.ics.gmehta1.service.api_gateway.models.billing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class IdRequestModel extends RequestModel {
    private String id;

    @JsonCreator
    public IdRequestModel(@JsonProperty(value = "id", required = true) String id)
    {
        this.id = id;
    }
    @JsonProperty
    public String getId() { return id; }
}
