package edu.uci.ics.gmehta1.service.idm.core;

public class Sessions {
    private String email;
    private String sessionID;

    public Sessions(String email, String sessionID) {
        this.email = email;
        this.sessionID = sessionID;
    }

    public String getSessionID() {
        return sessionID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
