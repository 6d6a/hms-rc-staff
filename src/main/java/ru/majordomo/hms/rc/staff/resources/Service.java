package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class Service extends Resource {

    @Transient
    private ServiceTemplate serviceTemplate;
    @Transient
    private List<ServiceSocket> serviceSockets = new ArrayList<>();

    private String serviceTemplateId;
    private List<String> serviceSocketIds = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public String getServiceTemplateId() {
        return serviceTemplateId;
    }

    @JsonIgnore
    public void setServiceTemplateId(String serviceTemplateId) {
        this.serviceTemplateId = serviceTemplateId;
    }

    @JsonIgnore
    public List<String> getServiceSocketIds() {
        return serviceSocketIds;
    }

    @JsonIgnore
    public void setServiceSocketIds(List<String> serviceSocketIds) {
        this.serviceSocketIds = serviceSocketIds;
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        try {
            this.serviceTemplateId = serviceTemplate.getId();
        } catch (NullPointerException e){}
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
        if (serviceSocketIds.contains(serviceSocketId) == false) {
            this.serviceSocketIds.add(serviceSocket.getId());
        }
    }

    public void addServiceSocketId(String serviceSocketId) {
        this.serviceSocketIds.add(serviceSocketId);
    }
}
