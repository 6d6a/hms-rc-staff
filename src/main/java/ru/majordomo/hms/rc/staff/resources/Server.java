package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document
public class Server extends Resource {

    @Transient
    private List<Service> services = new ArrayList<>();
    private List<String> serviceIds = new ArrayList<>();
    @Transient
    private ServerRole serverRole;
    private String serverRoleId;
    @Transient
    private List<Storage> storages = new ArrayList<>();
    private List<String> storageIds = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public List<Service> getServices() {
        return services;
    }

    public void setServices(List<Service> services) {
        List<String> serviceIdList = new ArrayList<>();
        for (Service service: services) {
            serviceIdList.add(service.getId());
        }
        setServiceIds(serviceIdList);
        this.services = services;
    }

    @JsonIgnore
    public ServerRole getServerRole() {
        return serverRole;
    }

    public void setServerRole(ServerRole serverRole) {
        try {
            setServerRoleId(serverRole.getId());
        } catch (NullPointerException e) {}
        this.serverRole = serverRole;
    }

    @JsonIgnore
    public List<Storage> getStorages() {
        return storages;
    }

    public void setStorages(List<Storage> storages) {
        List<String> storageIdList = new ArrayList<>();
        for (Storage storage: storages) {
            storageIdList.add(storage.getId());
        }
        setStorageIds(storageIdList);
        this.storages = storages;
    }

    @JsonGetter(value = "services")
    public List<String> getServiceIds() {
        return serviceIds;
    }

    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    @JsonGetter(value = "serverRole")
    public String getServerRoleId() {
        return serverRoleId;
    }

    public void setServerRoleId(String serverRoleId) {
        this.serverRoleId = serverRoleId;
    }

    @JsonGetter(value = "storages")
    public List<String> getStorageIds() {
        return storageIds;
    }

    public void setStorageIds(List<String> storageIds) {
        this.storageIds = storageIds;
    }
}
