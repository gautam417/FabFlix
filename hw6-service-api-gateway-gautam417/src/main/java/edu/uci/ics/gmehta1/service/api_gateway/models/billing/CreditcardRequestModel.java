package edu.uci.ics.gmehta1.service.api_gateway.models.billing;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

import java.sql.Date;

//import java.util.Date;

@JsonInclude(JsonInclude.Include.NON_NULL) // Tells Jackson to ignore all fields with value of NULL
public class CreditcardRequestModel extends RequestModel {
    private String id;
    private String firstName;
    private String lastName;

//    @JsonSerialize(as = Date.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date expiration;

    @JsonCreator
    public CreditcardRequestModel(@JsonProperty(value = "id", required = true) String id,
                                  @JsonProperty(value = "firstName", required = true) String firstName,
                                  @JsonProperty(value = "lastName", required = true) String lastName,
                                  @JsonProperty(value = "expiration", required = true) Date expiration)
    {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.expiration = expiration;
    }
    @JsonProperty
    public String getId() { return id; }
    @JsonProperty
    public String getFirstName() { return firstName; }
    @JsonProperty
    public String getLastName() { return lastName; }

//    @JsonSerialize(as = Date.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd") // HOPEFULLY THIS CHANGES THE EXPIRATION FORMAT
    @JsonProperty
    public Date getExpiration() { return expiration; }

}
