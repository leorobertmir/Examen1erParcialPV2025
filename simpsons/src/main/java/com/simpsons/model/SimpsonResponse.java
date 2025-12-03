package com.simpsons.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SimpsonResponse {
    private int count;
    private String next;
    private String prev;
    private int pages;
    private List<Personajes> results;

    public SimpsonResponse() {
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getNext() {
        return next;
    }

    public void setNext(String next) {
        this.next = next;
    }

    public String getPrev() {
        return prev;
    }

    public void setPrev(String prev) {
        this.prev = prev;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<Personajes> getResults() {
        return results;
    }

    public void setResults(List<Personajes> results) {
        this.results = results;
    }
}


