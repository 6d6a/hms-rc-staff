package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ServiceSocket extends Resource {

    private Long address;
    private Integer port;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public Long getAddress() {
        return address;
    }

    @JsonGetter(value = "address")
    public String getAddressAsString() {
        try {
            return Network.ipAddressInIntegerToString(address);
        } catch (NullPointerException e) {
            return null;
        }
    }

    @JsonIgnore
    public void setAddress(Long address) {
        this.address = address;
    }

    @JsonSetter(value = "address")
    public void setAddress(String address) {
        try {
            this.address = Network.ipAddressInStringToInteger(address);
        } catch (NullPointerException e) {
        }
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        ServiceSocket that = (ServiceSocket) o;

        if (getAddress() != null ? !getAddress().equals(that.getAddress()) : that.getAddress() != null) return false;
        return getPort() != null ? getPort().equals(that.getPort()) : that.getPort() == null;

    }
}
