package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class ServerRole extends Resource {

    @Transient
    private List<ServiceTemplate> serviceTemplateList = new ArrayList<>();
    private List<String> serviceTemplateIdList = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public List<ServiceTemplate> getServiceTemplateList() {
        return serviceTemplateList;
    }

    @JsonIgnore
    public void setServiceTemplateList(List<ServiceTemplate> serviceTemplateList) {
        List<String> serviceTemplateIdList = new ArrayList<>();
        for (ServiceTemplate serviceTemplate: serviceTemplateList) {
            serviceTemplateIdList.add(serviceTemplate.getId());
        }
        this.serviceTemplateIdList = serviceTemplateIdList;
        this.serviceTemplateList = serviceTemplateList;
    }

    @JsonGetter(value = "serviceTemplateList")
    public List<String> getServiceTemplateIdList() {
        return serviceTemplateIdList;
    }

    public void setServiceTemplateIdList(List<String> serviceTemplateIdList) {
        this.serviceTemplateIdList = serviceTemplateIdList;
    }

    public void addServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplateList.add(serviceTemplate);
        this.serviceTemplateIdList.add(serviceTemplate.getId());
    }
}
