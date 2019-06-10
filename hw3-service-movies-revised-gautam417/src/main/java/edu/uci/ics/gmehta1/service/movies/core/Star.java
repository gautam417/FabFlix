package edu.uci.ics.gmehta1.service.movies.core;

//DOES THIS HAVE TO BE A JSON CLASS??????
public class Star {
    private String id;
    private String name;
    private int birthYear;

    public Star(String id, String name, int birthYear) {
        this.id = id;
        this.name = name;
        this.birthYear = birthYear;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getBirthYear() {
        return birthYear;
    }
}
