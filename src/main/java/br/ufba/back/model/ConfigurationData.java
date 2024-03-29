package br.ufba.back.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import happy.coding.io.LineConfiger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConfigurationData {

    private StatusReading status;
    private Boolean asynchronous;
    private String dataset;
    private String outputSetup;
    private String evaluationSetup;
    private String recommender;
    private String itemRanking;
    private String algoName;
    private Integer numShrinkage;
    private Integer numNeighbors;
    private String similarity;
    private String ratingSetup;
    private Integer numFactors;
    private Integer numMaxIter;
    private Integer minTrustDegree;
    private String datasetSocial;
    private String regLambda;
    private String learnRate;
    private String pgmSetup;
    private Map<String, Double> resultados;

    public Map<String, Double> getResultados() {
        return resultados;
    }

    public void setResultados(Map<String, Double> resultados) {
        this.resultados = resultados;
    }

    public String getPgmSetup() {
        return pgmSetup;
    }

    public void setPgmSetup(String pgmSetup) {
        this.pgmSetup = pgmSetup;
    }

    public String getLearnRate() {
        return learnRate;
    }

    public void setLearnRate(String learnRate) {
        this.learnRate = learnRate;
    }

    public String getRegLambda() {
        return regLambda;
    }

    public void setRegLambda(String regLambda) {
        this.regLambda = regLambda;
    }

    public String getDatasetSocial() {
        return datasetSocial;
    }

    public void setDatasetSocial(String datasetSocial) {
        this.datasetSocial = datasetSocial;
    }

    public Integer getMinTrustDegree() {
        return minTrustDegree;
    }

    public void setMinTrustDegree(Integer minTrustDegree) {
        this.minTrustDegree = minTrustDegree;
    }

    public Integer getMaxTrustDegree() {
        return maxTrustDegree;
    }

    public void setMaxTrustDegree(Integer maxTrustDegree) {
        this.maxTrustDegree = maxTrustDegree;
    }

    private Integer maxTrustDegree;

    public Integer getNumMaxIter() {
        return numMaxIter;
    }

    public void setNumMaxIter(Integer numMaxIter) {
        this.numMaxIter = numMaxIter;
    }

    public Integer getNumFactors() {
        return numFactors;
    }

    public void setNumFactors(Integer numFactors) {
        this.numFactors = numFactors;
    }

    public String getRatingSetup() {
        return ratingSetup;
    }

    public void setRatingSetup(String ratingSetup) {
        this.ratingSetup = ratingSetup;
    }

    public String getOutputSetup() {
        return outputSetup;
    }

    public void setOutputSetup(String outputSetup) {
        this.outputSetup = outputSetup;
    }



    public String getSimilarity() {
        return similarity;
    }

    public void setSimilarity(String similarity) {
        this.similarity = similarity;
    }

    public Integer getNumNeighbors() {
        return numNeighbors;
    }

    public void setNumNeighbors(Integer numNeighbors) {
        this.numNeighbors = numNeighbors;
    }

    public Integer getNumShrinkage() {
        return numShrinkage;
    }

    public void setNumShrinkage(Integer numShrinkage) {
        this.numShrinkage = numShrinkage;
    }

    public String getAlgoName() {
        return algoName;
    }

    public void setAlgoName(String algoName) {
        this.algoName = algoName;
    }

    public String getItemRanking() {
        return itemRanking;
    }

    public void setItemRanking(String itemRanking) {
        this.itemRanking = itemRanking;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    public String getEvaluationSetup() {
        return evaluationSetup;
    }

    public void setEvaluationSetup(String evaluationSetup) {
        this.evaluationSetup = evaluationSetup;
    }

    public String getDataset() {
        return dataset;
    }

    public void setDataset(String dataset) {
        this.dataset = dataset;
    }

    public LineConfiger getParamOptions(String key) {
        return key == null ? null : new LineConfiger(key);
    }

    public Boolean getAsynchronous() {
        return asynchronous;
    }

    public void setAsynchronous(Boolean asynchronous) {
        this.asynchronous = asynchronous;
    }

    public StatusReading getStatus() {
        return status;
    }

    public void setStatus(StatusReading status) {
        this.status = status;
    }


}
