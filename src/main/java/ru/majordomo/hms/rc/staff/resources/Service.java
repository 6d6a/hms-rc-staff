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

    @JsonGetter(value = "serviceTemplate")
    public String getServiceTemplateId() {
        return serviceTemplateId;
    }

    @JsonSetter(value = "serviceTemplate")
    public void setServiceTemplateId(String serviceTemplateId) {
        this.serviceTemplateId = serviceTemplateId;
    }

    @JsonGetter(value = "serviceSockets")
    public List<String> getServiceSocketIds() {
        return serviceSocketIds;
    }

    @JsonSetter(value = "serviceSockets")
    public void setServiceSocketIds(List<String> serviceSocketIds) {
        this.serviceSocketIds = serviceSocketIds;
    }

    @JsonIgnore
    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    @JsonIgnore
    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        try {
            this.serviceTemplateId = serviceTemplate.getId();
        } catch (NullPointerException e){}
        this.serviceTemplate = serviceTemplate;
    }

    @JsonIgnore
    public List<ServiceSocket> getServiceSockets() {
        return serviceSockets;
    }

    @JsonIgnore
    public void setServiceSockets(List<ServiceSocket> serviceSockets) {
        List<String> ids = new ArrayList<>();
        for (ServiceSocket serviceSocket: serviceSockets) {
            ids.add(serviceSocket.getId());
        }
        this.serviceSocketIds = ids;
        this.serviceSockets = serviceSockets;
    }

    public void addServiceSocket(ServiceSocket serviceSocket) {
        this.serviceSocketIds.add(serviceSocket.getId());
        this.serviceSockets.add(serviceSocket);
    }

    public void addServiceSocketId(String serviceSocketId) {
        this.serviceSocketIds.add(serviceSocketId);
    }
}
