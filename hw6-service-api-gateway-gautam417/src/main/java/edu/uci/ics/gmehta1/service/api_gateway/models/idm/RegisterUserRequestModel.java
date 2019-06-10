package edu.uci.ics.gmehta1.service.api_gateway.models.idm;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.api_gateway.models.RequestModel;

public class RegisterUserRequestModel extends RequestModel {
    private String email;
    private char[] password;

    @JsonCreator
    public RegisterUserRequestModel(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "password", required = true) char[] password) {
        this.email = email;
        this.password = password;
    }

    @Override
    public String toString() {
        return "RegisterUserRequestModel [Email: " + email + ", has password?: " + (password != null);
    }

    @JsonProperty("email")
    public String getEmail() {
        return email;
    }

    @JsonProperty("password")
    public char[] getPassword() {
        return password;
    }
}