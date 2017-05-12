package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.resources.validation.ObjectIdCollection;

@Document
public class Server extends Resource {
    @NotEmpty(message = "Не найден ни один Service")
    @ObjectIdCollection(value = Service.class)
    private List<String> serviceIds = new ArrayList<>();

    @NotEmpty(message = "Не найден ни один ServerRole")
    @ObjectIdCollection(value = ServerRole.class)
    private List<String> serverRoleIds = new ArrayList<>();

    @NotEmpty(message = "Не найден ни один Storage")
    @ObjectIdCollection(value = Storage.class)
    private List<String> storageIds = new ArrayList<>();

    @Transient
    private List<Service> services = new ArrayList<>();

    @Transient
    private List<ServerRole> serverRoles = new ArrayList<>();

    @Transient
    private List<Storage> storages = new ArrayList<>();

    @Transient
    private String activeMailboxStorageMountPoint;

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

    public List<ServerRole> getServerRoles() {
        return serverRoles;
    }

    public void setServerRoles(List<ServerRole> serverRoles) {
        List<String> serverRoleIds = new ArrayList<>();
        for (ServerRole serverRole: serverRoles) {
            serverRoleIds.add(serverRole.getId());
        }
        setServerRoleIds(serverRoleIds);
        this.serverRoles = serverRoles;
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
    public Storage getActiveMailboxStorage() {
        for (Storage storage : storages) {
            if (storage.getMountPoint() != null && storage.getMountPoint().equals(activeMailboxStorageMountPoint)) {
                return storage;
            }
        }
        return null;
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
    public List<String> getServerRoleIds() {
        return serverRoleIds;
    }

    @JsonIgnore
    public void setServerRoleIds(List<String> serverRoleIds) {
        this.serverRoleIds = serverRoleIds;
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
        if (!storageIds.contains(storageId)) {
            this.storageIds.add(storageId);
        }
    }

    public void addService(Service service) {
        String serviceId = service.getId();
        this.services.add(service);
        if (!serviceIds.contains(serviceId)) {
            this.serviceIds.add(serviceId);
        }
    }

    public void addServerRole(ServerRole serverRole) {
        String serverRoleId = serverRole.getId();
        this.serverRoles.add(serverRole);
        if (!serverRoleIds.contains(serverRoleId)) {
            this.serverRoleIds.add(serverRoleId);
        }
    }

    public void setActiveMailboxStorageMountPoint(String activeMailboxStorageMountPoint) {
        this.activeMailboxStorageMountPoint = activeMailboxStorageMountPoint;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Server server = (Server) o;

        if (getServiceIds() != null ? !getServiceIds().equals(server.getServiceIds()) : server.getServiceIds() != null)
            return false;
        if (getServerRoleIds() != null ? !getServerRoleIds().equals(server.getServerRoleIds()) : server.getServerRoleIds() != null)
            return false;
        return getStorageIds() != null ? getStorageIds().equals(server.getStorageIds()) : server.getStorageIds() == null;
    }
}
