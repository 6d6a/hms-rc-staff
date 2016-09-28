package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;

@Document
public class Service extends Resource {

    @Transient
    private ServiceTemplate serviceTemplate;
    @Transient
    private List<ServiceSocket> serviceSocketList = new ArrayList<>();

    private String serviceTemplateId;
    private List<String> serviceSocketIdList = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonGetter(value = "serviceTemplate")
    public String getServiceTemplateId() {
        return serviceTemplateId;
    }

    public void setServiceTemplateId(String serviceTemplateId) {
        this.serviceTemplateId = serviceTemplateId;
    }

    @JsonGetter(value = "serviceSocketList")
    public List<String> getServiceSocketIdList() {
        return serviceSocketIdList;
    }

    public void setServiceSocketIdList(List<String> serviceSocketIdList) {
        this.serviceSocketIdList = serviceSocketIdList;
    }

    @JsonIgnore
    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    @JsonIgnore
    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplateId = serviceTemplate.getId();
        this.serviceTemplate = serviceTemplate;
    }

    @JsonIgnore
    public List<ServiceSocket> getServiceSocketList() {
        return serviceSocketList;
    }

    @JsonIgnore
    public void setServiceSocketList(List<ServiceSocket> serviceSocketList) {
        List<String> ids = new ArrayList<>();
        for (ServiceSocket serviceSocket: serviceSocketList) {
            ids.add(serviceSocket.getId());
        }
        this.serviceSocketIdList = ids;
        this.serviceSocketList = serviceSocketList;
    }

    public void addServiceSocket(ServiceSocket serviceSocket) {
        this.serviceSocketIdList.add(serviceSocket.getId());
        this.serviceSocketList.add(serviceSocket);
    }

    public void addServiceSocketId(String serviceSocketId) {
        this.serviceSocketIdList.add(serviceSocketId);
    }
}
