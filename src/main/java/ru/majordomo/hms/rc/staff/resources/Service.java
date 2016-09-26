package ru.majordomo.hms.rc.staff.resources;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;
//TODO
public class Service extends Resource {

    private ServiceTemplate serviceTemplate;
    private List<ServiceSocket> serviceSocketList = new ArrayList<>();

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public ServiceTemplate getServiceTemplate() {
        return serviceTemplate;
    }

    public void setServiceTemplate(ServiceTemplate serviceTemplate) {
        this.serviceTemplate = serviceTemplate;
    }

    public List<ServiceSocket> getServiceSocketList() {
        return serviceSocketList;
    }

    public void setServiceSocketList(List<ServiceSocket> serviceSocketList) {
        this.serviceSocketList = serviceSocketList;
    }
}
