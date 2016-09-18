package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class ServiceTemplate extends Resource {

    private List<String> configTemplateIdsList = new ArrayList<>();
    @Transient
    private List<ConfigTemplate> configTemplateList = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public List<ConfigTemplate> getConfigTemplateList() {
        return configTemplateList;
    }

    public void setConfigTemplateList(List<ConfigTemplate> configTemplateList) {
        this.configTemplateList = configTemplateList;
    }

    public void addConfigTemplate(ConfigTemplate configTemplate) {
        this.configTemplateList.add(configTemplate);
    }
}
