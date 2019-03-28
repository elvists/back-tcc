package main.java.br.ufba.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Configuration {
    private String recommender;

    private Boolean itemRanking;
    private Boolean threshold;

    private Long handSeed;

    private String testView;
    private Long ignore;

    private Long topN;

    private Long kFold;

    @JsonProperty("k_fold")
    public Long getkFold() {
        return kFold;
    }

    public void setkFold(Long kFold) {
        this.kFold = kFold;
    }

    public String getRecommender() {
        return recommender;
    }

    public void setRecommender(String recommender) {
        this.recommender = recommender;
    }

    @JsonProperty("item_ranking")
    public Boolean getItemRanking() {
        return itemRanking;
    }

    public void setItemRanking(Boolean itemRanking) {
        this.itemRanking = itemRanking;
    }

    public Boolean getThreshold() {
        return threshold;
    }

    public void setThreshold(Boolean threshold) {
        this.threshold = threshold;
    }

    @JsonProperty("hand_seed")
    public Long getHandSeed() {
        return handSeed;
    }

    public void setHandSeed(Long handSeed) {
        this.handSeed = handSeed;
    }

    @JsonProperty("test_view")
    public String getTestView() {
        return testView;
    }

    public void setTestView(String testView) {
        this.testView = testView;
    }

    public Long getIgnore() {
        return ignore;
    }

    public void setIgnore(Long ignore) {
        this.ignore = ignore;
    }

    @JsonProperty("top_n")
    public Long getTopN() {
        return topN;
    }

    public void setTopN(Long topN) {
        this.topN = topN;
    }
}
