package br.ufba.back.controller;

import br.ufba.back.model.ConfigurationData;
import br.ufba.back.service.LibRecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class Controller {


    @Autowired
    private LibRecService libRecService;


    @PostMapping("/recomendar")
    public ConfigurationData recomendar(@RequestBody ConfigurationData config) {
        try {
            libRecService.run(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return config;
    }
}
