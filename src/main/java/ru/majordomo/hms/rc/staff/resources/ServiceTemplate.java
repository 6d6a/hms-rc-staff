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

    public List<ConfigTemplate> getConfigTemplates() {
        return configTemplates;
    }

    @JsonIgnore
    public List<String> getConfigTemplateIds() {
        return configTemplateIds;
    }

    @JsonIgnore
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
        String configTemplateId = configTemplate.getId();
        this.configTemplates.add(configTemplate);
        if (configTemplateIds.contains(configTemplateId) == false) {
            this.configTemplateIds.add(configTemplate.getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServiceTemplate that = (ServiceTemplate) o;

        if (getConfigTemplateIds() != null ? !getConfigTemplateIds().equals(that.getConfigTemplateIds()) : that.getConfigTemplateIds() != null)
            return false;
        return getConfigTemplates() != null ? getConfigTemplates().equals(that.getConfigTemplates()) : that.getConfigTemplates() == null;

    }
}
