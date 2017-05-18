package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotNull;

import ru.majordomo.hms.rc.staff.resources.validation.ObjectId;
import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;

@Document
public class Service extends Resource {
    @NotNull(message = "Отсутствует ServiceTemplate")
    @ObjectId(ServiceTemplate.class)
    private String serviceTemplateId;

    @NotEmpty(message = "Не найден ни один ServiceSocket")
    @ObjectIdCollection(ServiceSocket.class)
    private List<String> serviceSocketIds = new ArrayList<>();

    @Transient
    private ServiceTemplate serviceTemplate;

    @Transient
    private List<ServiceSocket> serviceSockets = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public String getServiceTemplateId() {
        return serviceTemplateId;
    }

    public void setServiceTemplateId(String serviceTemplateId) {
        this.serviceTemplateId = serviceTemplateId;
    }

    public List<String> getServiceSocketIds() {
        return serviceSocketIds;
    }

    public void setServiceSocketIds(List<String> serviceSocketIds) {
        this.serviceSocketIds = serviceSocketIds;
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplateId = serviceTemplate != null ? serviceTemplate.getId() : null;
        this.serviceTemplate = serviceTemplate;
    }

    public List<ServiceSocket> getServiceSockets() {
        return serviceSockets;
    }

    public void setServiceSockets(List<ServiceSocket> serviceSockets) {
        List<String> ids = new ArrayList<>();
        for (ServiceSocket serviceSocket: serviceSockets) {
            ids.add(serviceSocket.getId());
        }
        this.serviceSocketIds = ids;
        this.serviceSockets = serviceSockets;
    }

    public void addServiceSocket(ServiceSocket serviceSocket) {
        String serviceSocketId = serviceSocket.getId();
        this.serviceSockets.add(serviceSocket);
        if (!serviceSocketIds.contains(serviceSocketId)) {
            this.serviceSocketIds.add(serviceSocket.getId());
        }
    }

    public void addServiceSocketId(String serviceSocketId) {
        this.serviceSocketIds.add(serviceSocketId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Service service = (Service) o;

        if (getServiceTemplateId() != null ? !getServiceTemplateId().equals(service.getServiceTemplateId()) : service.getServiceTemplateId() != null)
            return false;
        return getServiceSocketIds() != null ? getServiceSocketIds().equals(service.getServiceSocketIds()) : service.getServiceSocketIds() == null;
    }

    @Override
    public String toString() {
        return "Service{" +
                "serviceTemplate=" + serviceTemplate +
                ", serviceSockets=" + serviceSockets +
                ", serviceTemplateId='" + serviceTemplateId + '\'' +
                ", serviceSocketIds=" + serviceSocketIds +
                "} " + super.toString();
    }
}
