package br.ufba.back.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gson.annotations.SerializedName;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class PredictionResult extends Result {

    @SerializedName("MAE")
    private Double mae;

    @SerializedName("RMSE")
    private Double rmse;

    @SerializedName("nMAE")
    private Double nmae;

    @SerializedName("rMAE")
    private Double rmae;

    @SerializedName("rRMSE")
    private Double rrmse;

    public Double getPerplexity() {
        return perplexity;
    }

    public void setPerplexity(Double perplexity) {
        this.perplexity = perplexity;
    }

    @SerializedName("MPE")
    private Double mpe;


    @SerializedName("Perplexity")
    private Double perplexity;



    public Double getMae() {
        return mae;
    }

    public void setMae(Double mae) {
        this.mae = mae;
    }

    public Double getRmse() {
        return rmse;
    }

    public void setRmse(Double rmse) {
        this.rmse = rmse;
    }

    public Double getNmae() {
        return nmae;
    }

    public void setNmae(Double nmae) {
        this.nmae = nmae;
    }

    public Double getRmae() {
        return rmae;
    }

    public void setRmae(Double rmae) {
        this.rmae = rmae;
    }

    public Double getRrmse() {
        return rrmse;
    }

    public void setRrmse(Double rrmse) {
        this.rrmse = rrmse;
    }

    public Double getMpe() {
        return mpe;
    }

    public void setMpe(Double mpe) {
        this.mpe = mpe;
    }
}
