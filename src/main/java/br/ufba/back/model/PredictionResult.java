package main.java.br.ufba.model;

import java.math.BigDecimal;

public class PredictionResult extends Result {
    private BigDecimal mae;
    private BigDecimal rmse;
    private BigDecimal nmae;
    private BigDecimal rmae;
    private BigDecimal rrmse;
    private BigDecimal mpe;
    private Configuration configuration;

    public BigDecimal getMae() {
        return mae;
    }

    public void setMae(BigDecimal mae) {
        this.mae = mae;
    }

    public BigDecimal getRmse() {
        return rmse;
    }

    public void setRmse(BigDecimal rmse) {
        this.rmse = rmse;
    }

    public BigDecimal getNmae() {
        return nmae;
    }

    public void setNmae(BigDecimal nmae) {
        this.nmae = nmae;
    }

    public BigDecimal getRmae() {
        return rmae;
    }

    public void setRmae(BigDecimal rmae) {
        this.rmae = rmae;
    }

    public BigDecimal getRrmse() {
        return rrmse;
    }

    public void setRrmse(BigDecimal rrmse) {
        this.rrmse = rrmse;
    }

    public BigDecimal getMpe() {
        return mpe;
    }

    public void setMpe(BigDecimal mpe) {
        this.mpe = mpe;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
