package com.example.apivreme;

public class City {
    String id;
    String name;
    String country;
    Coord coord;

    public City(){

    }

    public City(String id, String name, String country, Coord coord) {
        this.id = id;
        this.name = name;
        this.country = country;
        this.coord = coord;
    }
}
