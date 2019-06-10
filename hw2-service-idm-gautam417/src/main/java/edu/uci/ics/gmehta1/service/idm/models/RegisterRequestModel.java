package edu.uci.ics.gmehta1.service.idm.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.uci.ics.gmehta1.service.idm.core.Validate;
import edu.uci.ics.gmehta1.service.idm.logger.ServiceLogger;

public class RegisterRequestModel implements Validate {
    private String email;
    private char[] password;

    @Override
    public boolean isValid() {
        ServiceLogger.LOGGER.info("email > 0 ? " + (email.length() >= 1));
        ServiceLogger.LOGGER.info("password > 0 ? " + (password.length >= 1));
        if (password.length >= 1){
            ServiceLogger.LOGGER.info("Password is provided");
        }
        return (password.length >= 1);
    }

    @JsonCreator
    public RegisterRequestModel(
            @JsonProperty(value = "email", required = true) String email,
            @JsonProperty(value = "password", required = true) char[] password)
    {
        this.email = email;
        this.password = password;
    }
    @JsonProperty
    public String getEmail() { return email; }

    @JsonProperty
    public char[] getPassword() { return password; }
}
