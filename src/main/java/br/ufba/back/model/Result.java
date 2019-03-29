package br.ufba.back.model;

import com.google.gson.annotations.SerializedName;

import java.time.Instant;

public class Result {

    private String id;
    private String algorithm;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
