package ru.majordomo.hms.rc.staff.resources;

import ru.majordomo.hms.rc.staff.Resource;

public class ServiceSocket extends Resource {

    private Long address;
    private Character port;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    public Long getAddress() {
        return address;
    }

    public void setAddress(Long address) {
        this.address = address;
    }

    public Character getPort() {
        return port;
    }

    public void setPort(Character port) {
        this.port = port;
    }
}
