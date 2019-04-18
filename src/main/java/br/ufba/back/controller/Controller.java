package br.ufba.back.controller;

import br.ufba.back.model.ConfigurationData;
import br.ufba.back.model.Results;
import br.ufba.back.service.LibRecService;
import librec.intf.Recommender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class Controller {


    @Autowired
    private LibRecService libRecService;


    @PostMapping("/recomendar")
    public Results recomendar(@RequestBody List<ConfigurationData> configs) {
        try {
            List<Map<String, Double>> resultados = new ArrayList<>();
            List<String> algoritmos = new ArrayList<>();

            for (ConfigurationData config: configs) {
                resultados.add(libRecService.run(config));
                algoritmos.add(config.getRecommender());
            }
            return new Results(tratar(resultados, algoritmos), algoritmos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<Map<String, Object>> tratar(List<Map<String, Double>> resultados, List<String> algoritmos) {
        List< Map<String,Object> > todos = new ArrayList();
        Map<String,Object> a1 = new HashMap<>();
        Map<String,Object> a2 = new HashMap<>();
        Map<String,Object> a3 = new HashMap<>();
        Map<String,Object> a4 = new HashMap<>();
        Map<String,Object> a5 = new HashMap<>();
        Map<String,Object> a6 = new HashMap<>();
        Map<String,Object> a7 = new HashMap<>();
        Map<String,Object> a8 = new HashMap<>();
        int i=0;
        for (Map<String,Double> a:resultados){

            if (a.get("isRankingPred") == 1) {
                if(i==0){
                    a1.put("name",String.valueOf(Recommender.Measure.Pre5));
                    a2.put("name",String.valueOf(Recommender.Measure.Pre10));
                    a3.put("name",String.valueOf(Recommender.Measure.Rec5));
                    a4.put("name",String.valueOf(Recommender.Measure.Rec10));
                    a5.put("name",String.valueOf(Recommender.Measure.AUC));
                    a6.put("name",String.valueOf(Recommender.Measure.MAP));
                    a7.put("name",String.valueOf(Recommender.Measure.NDCG));
                    a8.put("name",String.valueOf(Recommender.Measure.MRR));
                }
                a1.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Pre5)));
                a2.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Pre10)));
                a3.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Rec5)));
                a4.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.Rec10)));
                a5.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.AUC)));
                a6.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MAP)));
                a7.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.NDCG)));
                a8.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MRR)));
            } else {
                if(i==0){
                    a1.put("name",String.valueOf(Recommender.Measure.MAE));
                    a2.put("name",String.valueOf(Recommender.Measure.RMSE));
                    a3.put("name",String.valueOf(Recommender.Measure.NMAE));
                    a4.put("name",String.valueOf(Recommender.Measure.rMAE));
                    a5.put("name",String.valueOf(Recommender.Measure.rRMSE));
                    a6.put("name",String.valueOf(Recommender.Measure.MPE));
                }
                a1.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MAE)));
                a2.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.RMSE)));
                a3.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.NMAE)));
                a4.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.rMAE)));
                a5.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.rRMSE)));
                a6.put(algoritmos.get(i), a.get(String.valueOf(Recommender.Measure.MPE)));
            }
            i++;
        }

        todos.add(a1);
        todos.add(a2);
        todos.add(a3);
        todos.add(a4);
        todos.add(a5);
        todos.add(a6);
        if(!a8.isEmpty()){
            todos.add(a7);
            todos.add(a8);
        }
        return todos;
    }
}
