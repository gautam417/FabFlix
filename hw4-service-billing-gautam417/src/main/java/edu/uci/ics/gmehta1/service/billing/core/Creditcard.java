package edu.uci.ics.gmehta1.service.billing.core;

import java.sql.Date;

public class Creditcard {
    private String id;
    private String firstName;
    private String lastName;
    //    @JsonSerialize(as = Date.class)
//    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date expiration;

    public Creditcard(String id, String firstName, String lastName, Date expiration) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.expiration = expiration;
    }

    public String getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public Date getExpiration() {
        return expiration;
    }
}
