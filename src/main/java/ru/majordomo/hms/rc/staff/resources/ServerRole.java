package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class ServerRole extends Resource {

    @Transient
    private List<ServiceTemplate> serviceTemplates = new ArrayList<>();
    private List<String> serviceTemplateIds = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public List<ServiceTemplate> getServiceTemplates() {
        return serviceTemplates;
    }

    @JsonIgnore
    public void setServiceTemplates(List<ServiceTemplate> serviceTemplates) {
        List<String> serviceTemplateIdList = new ArrayList<>();
        for (ServiceTemplate serviceTemplate: serviceTemplates) {
            serviceTemplateIdList.add(serviceTemplate.getId());
        }
        this.serviceTemplateIds = serviceTemplateIdList;
        this.serviceTemplates = serviceTemplates;
    }

    @JsonGetter(value = "serviceTemplates")
    public List<String> getServiceTemplateIds() {
        return serviceTemplateIds;
    }

    @JsonSetter(value = "serviceTemplates")
    public void setServiceTemplateIds(List<String> serviceTemplateIds) {
        this.serviceTemplateIds = serviceTemplateIds;
    }

    public void addServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplates.add(serviceTemplate);
        this.serviceTemplateIds.add(serviceTemplate.getId());
    }
}
