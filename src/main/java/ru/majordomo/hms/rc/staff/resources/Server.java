package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class Server extends Resource {

    private List<Service> serviceList = new ArrayList<>();
    private ServerRole serverRole;
    private List<Storage> storageList;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public List<Service> getServiceList() {
        return serviceList;
    }

    public void setServiceList(List<Service> serviceList) {
        this.serviceList = serviceList;
    }

    public ServerRole getServerRole() {
        return serverRole;
    }

    public void setServerRole(ServerRole serverRole) {
        this.serverRole = serverRole;
    }

    public List<Storage> getStorageList() {
        return storageList;
    }

    public void setStorageList(List<Storage> storageList) {
        this.storageList = storageList;
    }
}
