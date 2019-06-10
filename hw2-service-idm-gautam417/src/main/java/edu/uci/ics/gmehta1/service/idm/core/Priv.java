package edu.uci.ics.gmehta1.service.idm.core;

public class Priv {
    private String email;
    private int plevel;

    public Priv(String email, int plevel) {
        this.email = email;
        this.plevel = plevel;
    }

    public int getPlevel() {
        return plevel;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
