package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class ServiceTemplate extends Resource {

    private List<String> configTemplateIds = new ArrayList<>();
    @Transient
    private List<ConfigTemplate> configTemplates = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public List<ConfigTemplate> getConfigTemplates() {
        return configTemplates;
    }

    @JsonGetter(value = "configTemplates")
    public List<String> getConfigTemplateIds() {
        return configTemplateIds;
    }

    @JsonSetter(value = "configTemplates")
    public void setConfigTemplateIds(List<String> configTemplateIds) {
        this.configTemplateIds = configTemplateIds;
    }

    public void setConfigTemplates(List<ConfigTemplate> configTemplates) {
        for (ConfigTemplate configTemplate: configTemplates) {
            this.configTemplateIds.add(configTemplate.getId());
        }
        this.configTemplates = configTemplates;
    }

    public void addConfigTemplate(ConfigTemplate configTemplate) {
        this.configTemplates.add(configTemplate);
        this.configTemplateIds.add(configTemplate.getId());
    }
}
