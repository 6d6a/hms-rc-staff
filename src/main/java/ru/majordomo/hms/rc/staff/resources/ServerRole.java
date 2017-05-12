package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;
import ru.majordomo.hms.rc.staff.resources.validation.UniqueNameResource;

@Document
@UniqueNameResource(value = ServerRole.class)
public class ServerRole extends Resource {
    @NotEmpty(message = "Не найден ни один ServiceTemplateId")
    @ObjectIdCollection(ServiceTemplate.class)
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

        return getServiceTemplateIds() != null ? getServiceTemplateIds().equals(that.getServiceTemplateIds()) : that.getServiceTemplateIds() == null;
    }
}
