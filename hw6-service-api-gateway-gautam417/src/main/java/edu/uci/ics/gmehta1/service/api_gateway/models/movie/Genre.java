package edu.uci.ics.gmehta1.service.api_gateway.models.movie;

public class Genre {
    private int id;
    private String name;

    public Genre(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
    //    @JsonCreator
//    public Genre(
//            @JsonProperty(value = "id", required = true) int id,
//            @JsonProperty(value = "name", required = true) String name)
//    {
//        this.id = id;
//        this.name = name;
//    }
//    @JsonProperty("id")
//    public int getId() { return id; }
//    @JsonProperty("name")
//    public String getName() { return name; }
}
