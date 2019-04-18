package br.ufba.back.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;


public class Results {

    private List<String> algoritmos;

    private List<Map<String,Object>> resultado;

    public Results(List<Map<String, Object>> resultado, List<String> algoritmos) {
        this.algoritmos = algoritmos;
        this.resultado = resultado;
    }

    public List<String> getAlgoritmos() {
        return algoritmos;
    }

    public void setAlgoritmos(List<String> algoritmos) {
        this.algoritmos = algoritmos;
    }

    public List<Map<String, Object>> getResultado() {
        return resultado;
    }

    public void setResultado(List<Map<String, Object>> resultado) {
        this.resultado = resultado;
    }
}
