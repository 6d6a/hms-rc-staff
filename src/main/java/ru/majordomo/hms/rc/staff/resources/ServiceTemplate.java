package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectId;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;

import java.util.ArrayList;
import java.util.List;

@Document
public class ServiceTemplate extends Resource {
    @ObjectIdCollection(value = ConfigTemplate.class, message = "ConfigTemplate с указанным id не найден в БД")
    @NotEmpty(message = "Не найден ни один ConfigTemplateId")
    private List<String> configTemplateIds = new ArrayList<>();

    @NotBlank(message = "Отсутствует ServiceType")
    @ObjectId(value = ServiceType.class, fieldName = "name", message = "ServiceType с указанным именем не найден в БД")
    private String serviceTypeName;

    @Transient
    private List<ConfigTemplate> configTemplates = new ArrayList<>();

    @Transient
    private ServiceType serviceType;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public List<ConfigTemplate> getConfigTemplates() {
        return configTemplates;
    }

    public void setConfigTemplates(List<ConfigTemplate> configTemplates) {
//        for (ConfigTemplate configTemplate: configTemplates) {
//            this.configTemplateIds.add(configTemplate.getId());
//        }
        this.configTemplates = configTemplates;
    }

    public ServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceType serviceType) {
        if (serviceType != null) {
            this.serviceType = serviceType;
//            this.serviceTypeName = serviceType.getName();
        }
    }

    @JsonIgnore
    public List<String> getConfigTemplateIds() {
        return configTemplateIds;
    }

    @JsonIgnore
    public void setConfigTemplateIds(List<String> configTemplateIds) {
        this.configTemplateIds = configTemplateIds;
    }

    @JsonIgnore
    public String getServiceTypeName() {
        return serviceTypeName;
    }

    @JsonIgnore
    public void setServiceTypeName(String serviceTypeName) {
        this.serviceTypeName = serviceTypeName;
    }

    public void addConfigTemplate(ConfigTemplate configTemplate) {
        if (configTemplate != null) {
            String configTemplateId = configTemplate.getId();
            this.configTemplates.add(configTemplate);
            if (!configTemplateIds.contains(configTemplateId)) {
                this.configTemplateIds.add(configTemplate.getId());
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServiceTemplate that = (ServiceTemplate) o;

        return (getConfigTemplateIds() != null ?
                getConfigTemplateIds().equals(that.getConfigTemplateIds()) :
                that.getConfigTemplateIds() == null) &&
                (getConfigTemplates() != null ?
                        getConfigTemplates().equals(that.getConfigTemplates()) :
                        that.getConfigTemplates() == null);
    }

    @Override
    public String toString() {
        return "ServiceTemplate{" +
                "configTemplateIds=" + configTemplateIds +
                ", serviceTypeName='" + serviceTypeName + '\'' +
                ", configTemplates=" + configTemplates +
                ", serviceType=" + serviceType +
                "} " + super.toString();
    }
}
