package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class ServerRole extends Resource {

    private List<String> serviceTemplateIds = new ArrayList<>();
    @Transient
    private List<ServiceTemplate> serviceTemplates = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public List<ServiceTemplate> getServiceTemplates() {
        return serviceTemplates;
    }

    public void setServiceTemplates(List<ServiceTemplate> serviceTemplates) {
        for (ServiceTemplate serviceTemplate: serviceTemplates) {
            this.serviceTemplateIds.add(serviceTemplate.getId());
        }
        this.serviceTemplates = serviceTemplates;
    }

    @JsonIgnore
    public List<String> getServiceTemplateIds() {
        return serviceTemplateIds;
    }

    @JsonIgnore
    public void setServiceTemplateIds(List<String> serviceTemplateIds) {
        this.serviceTemplateIds = serviceTemplateIds;
    }

    public void addServiceTemplate(ServiceTemplate serviceTemplate) {
        String serviceTemplateId = serviceTemplate.getId();
        this.serviceTemplates.add(serviceTemplate);
        if (!serviceTemplateIds.contains(serviceTemplateId)) {
            this.serviceTemplateIds.add(serviceTemplate.getId());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServerRole that = (ServerRole) o;

        if (getServiceTemplateIds() != null ? !getServiceTemplateIds().equals(that.getServiceTemplateIds()) : that.getServiceTemplateIds() != null)
            return false;
        return getServiceTemplates() != null ? getServiceTemplates().equals(that.getServiceTemplates()) : that.getServiceTemplates() == null;

    }
}
