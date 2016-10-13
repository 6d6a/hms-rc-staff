package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

//TODO
@Document
public class Server extends Resource {

    @Transient
    private List<Service> serviceList = new ArrayList<>();
    private List<String> serviceIdList = new ArrayList<>();
    @Transient
    private ServerRole serverRole;
    private String serverRoleId;
    @Transient
    private List<Storage> storageList;
    private List<String> storageIdList = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public List<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Service> serviceList) {
        List<String> serviceIdList = new ArrayList<>();
        for (Service service: serviceList) {
            serviceIdList.add(service.getId());
        }
        setServiceIdList(serviceIdList);
        this.serviceList = serviceList;
    }

    @JsonIgnore
    public ServerRole getServerRole() {
        return serverRole;
    }

    public void setServerRole(ServerRole serverRole) {
        setServerRoleId(serverRole.getId());
        this.serverRole = serverRole;
    }

    @JsonIgnore
    public List<Storage> getStorageList() {
        return storageList;
    }

    public void setStorageList(List<Storage> storageList) {
        List<String> storageIdList = new ArrayList<>();
        setStorageIdList(storageIdList);
        this.storageList = storageList;
    }

    @JsonGetter(value = "serviceList")
    public List<String> getServiceIdList() {
        return serviceIdList;
    }

    public void setServiceIdList(List<String> serviceIdList) {
        this.serviceIdList = serviceIdList;
    }

    @JsonGetter(value = "serverRole")
    public String getServerRoleId() {
        return serverRoleId;
    }

    public void setServerRoleId(String serverRoleId) {
        this.serverRoleId = serverRoleId;
    }

    @JsonGetter(value = "storageList")
    public List<String> getStorageIdList() {
        return storageIdList;
    }

    public void setStorageIdList(List<String> storageIdList) {
        this.storageIdList = storageIdList;
    }
}
