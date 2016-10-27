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

    public ServerRole getServerRole() {
        return serverRole;
    }

    public void setServerRole(ServerRole serverRole) {
        try {
            setServerRoleId(serverRole.getId());
        } catch (NullPointerException e) {}
        this.serverRole = serverRole;
    }

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

    @JsonIgnore
    public List<String> getServiceIds() {
        return serviceIds;
    }

    @JsonIgnore
    public void setServiceIds(List<String> serviceIds) {
        this.serviceIds = serviceIds;
    }

    @JsonIgnore
    public String getServerRoleId() {
        return serverRoleId;
    }

    @JsonIgnore
    public void setServerRoleId(String serverRoleId) {
        this.serverRoleId = serverRoleId;
    }

    @JsonIgnore
    public List<String> getStorageIds() {
        return storageIds;
    }

    @JsonIgnore
    public void setStorageIds(List<String> storageIds) {
        this.storageIds = storageIds;
    }

    public void addStorage(Storage storage) {
        String storageId = storage.getId();
        this.storages.add(storage);
        if (storageIds.contains(storageId) == false) {
            this.storageIds.add(storageId);
        }
    }

    public void addService(Service service) {
        String serviceId = service.getId();
        this.services.add(service);
        if (serviceIds.contains(serviceId) == false) {
            this.serviceIds.add(serviceId);
        }
    }
}
