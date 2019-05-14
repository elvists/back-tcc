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
                if (!config.getAsynchronous()) {
                    resultados.add(libRecService.run(config));
                    algoritmos.add(config.getRecommender());
                }
            }
            return new Results(libRecService.tratar(resultados, algoritmos), algoritmos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
