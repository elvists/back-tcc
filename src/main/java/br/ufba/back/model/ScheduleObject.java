package br.ufba.back.model;

import java.util.ArrayList;
import java.util.List;

public class ScheduleObject {

    private List<ConfigurationData> listConfiguration;

    private StatusReading status;

    private String id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public StatusReading getStatus() {
        return status;
    }

    public void setStatus(StatusReading status) {
        this.status = status;
    }


    public ScheduleObject() {
        listConfiguration = new ArrayList<>();
    }

    public List<ConfigurationData> getListConfiguration() {
        return listConfiguration;
    }

    public void setListConfiguration(List<ConfigurationData> listConfiguration) {
        this.listConfiguration = listConfiguration;
    }
}
