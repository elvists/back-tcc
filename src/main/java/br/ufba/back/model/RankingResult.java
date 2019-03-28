package main.java.br.ufba.model;


import java.math.BigDecimal;

public class RankingResult extends Result {
    private BigDecimal prec5;
    private BigDecimal prec10;
    private BigDecimal recall5;
    private BigDecimal recall10;
    private BigDecimal auc;
    private BigDecimal map;
    private BigDecimal ndcg;
    private BigDecimal mrr;
    private Configuration configuration;

    public BigDecimal getPrec5() {
        return prec5;
    }

    public void setPrec5(BigDecimal prec5) {
        this.prec5 = prec5;
    }

    public BigDecimal getPrec10() {
        return prec10;
    }

    public void setPrec10(BigDecimal prec10) {
        this.prec10 = prec10;
    }

    public BigDecimal getRecall5() {
        return recall5;
    }

    public void setRecall5(BigDecimal recall5) {
        this.recall5 = recall5;
    }

    public BigDecimal getRecall10() {
        return recall10;
    }

    public void setRecall10(BigDecimal recall10) {
        this.recall10 = recall10;
    }

    public BigDecimal getAuc() {
        return auc;
    }

    public void setAuc(BigDecimal auc) {
        this.auc = auc;
    }

    public BigDecimal getMap() {
        return map;
    }

    public void setMap(BigDecimal map) {
        this.map = map;
    }

    public BigDecimal getNdcg() {
        return ndcg;
    }

    public void setNdcg(BigDecimal ndcg) {
        this.ndcg = ndcg;
    }

    public BigDecimal getMrr() {
        return mrr;
    }

    public void setMrr(BigDecimal mrr) {
        this.mrr = mrr;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }
}
