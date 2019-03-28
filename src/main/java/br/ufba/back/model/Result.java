package main.java.br.ufba.model;

import java.time.Instant;

public class Result {

    private String id;
    private String algorithm;
    private Instant trainingTime;
    private Instant testTime;

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public Instant getTrainingTime() {
        return trainingTime;
    }

    public void setTrainingTime(Instant trainingTime) {
        this.trainingTime = trainingTime;
    }

    public Instant getTestTime() {
        return testTime;
    }

    public void setTestTime(Instant testTime) {
        this.testTime = testTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
