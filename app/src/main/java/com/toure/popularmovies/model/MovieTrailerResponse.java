package com.toure.popularmovies.model;

import java.util.List;

public class MovieTrailerResponse {
    private int id;
    private List<MovieTrailer> results;

    public List<MovieTrailer> getResults() {
        return results;
    }

    public void setResults(List<MovieTrailer> results) {
        this.results = results;
    }

    public int getId() {

        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
