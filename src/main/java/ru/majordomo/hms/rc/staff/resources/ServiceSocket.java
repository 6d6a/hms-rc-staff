package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.mongodb.core.mapping.Document;

import ru.majordomo.hms.rc.staff.Resource;
@Document
public class ServiceSocket extends Resource {

    private Integer address;
    private Integer port;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public Integer getAddress() {
        return address;
    }

    @JsonGetter(value = "address")
    public String getAddressAsString() {
        return Network.ipAddressInIntegerToString(address);
    }

    @JsonIgnore
    public void setAddress(Integer address) {
        this.address = address;
    }

    public void setAddress(String address) {
        this.address = Network.ipAddressInStringToInteger(address);
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }
}
