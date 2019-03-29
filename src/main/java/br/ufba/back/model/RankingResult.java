package br.ufba.back.model;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class RankingResult extends Result {

    @SerializedName("Pre5")
    private Double prec5;

    @SerializedName("Pre10")
    private Double prec10;

    @SerializedName("Rec5")
    private Double recall5;

    @SerializedName("Rec10")
    private Double recall10;

    @SerializedName("AUC")
    private Double auc;

    @SerializedName("MAP")
    private Double map;

    @SerializedName("NDCG")
    private Double ndcg;

    @SerializedName("MRR")
    private Double mrr;

    @SerializedName("D10")
    private Double d10;

    @SerializedName("D5")
    private Double d5;

    public Double getD10() {
        return d10;
    }

    public void setD10(Double d10) {
        this.d10 = d10;
    }

    public Double getD5() {
        return d5;
    }

    public void setD5(Double d5) {
        this.d5 = d5;
    }

    public Double getPrec5() {
        return prec5;
    }

    public void setPrec5(Double prec5) {
        this.prec5 = prec5;
    }

    public Double getPrec10() {
        return prec10;
    }

    public void setPrec10(Double prec10) {
        this.prec10 = prec10;
    }

    public Double getRecall5() {
        return recall5;
    }

    public void setRecall5(Double recall5) {
        this.recall5 = recall5;
    }

    public Double getRecall10() {
        return recall10;
    }

    public void setRecall10(Double recall10) {
        this.recall10 = recall10;
    }

    public Double getAuc() {
        return auc;
    }

    public void setAuc(Double auc) {
        this.auc = auc;
    }

    public Double getMap() {
        return map;
    }

    public void setMap(Double map) {
        this.map = map;
    }

    public Double getNdcg() {
        return ndcg;
    }

    public void setNdcg(Double ndcg) {
        this.ndcg = ndcg;
    }

    public Double getMrr() {
        return mrr;
    }

    public void setMrr(Double mrr) {
        this.mrr = mrr;
    }
}
