package br.ufba.back.controller;

import br.ufba.back.model.ConfigurationData;
import br.ufba.back.model.Results;
import br.ufba.back.model.ScheduleObject;
import br.ufba.back.service.LibRecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin
public class Controller {


    @Autowired
    private LibRecService libRecService;


    @PostMapping("/recomendar")
    public Object recomendar(@RequestBody List<ConfigurationData> configs) {
        try {
            List<Map<String, Double>> resultados = new ArrayList<>();
            List<String> algoritmos = new ArrayList<>();
            for (ConfigurationData config: configs) {
                if (!config.getAsynchronous()) {
                    resultados.add(libRecService.run(config));
                    algoritmos.add(config.getRecommender());
                }else{
                    return libRecService.addConfig(configs);
                }
            }
            return new Results(libRecService.tratar(resultados, algoritmos), algoritmos);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/resultado/{id}")
    public Object resultado(@PathVariable("id") String id) {
        try {
            return libRecService.getResult(id);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @GetMapping("/list")
    public List<ScheduleObject> lista() {
        try {
            return libRecService.getList();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
