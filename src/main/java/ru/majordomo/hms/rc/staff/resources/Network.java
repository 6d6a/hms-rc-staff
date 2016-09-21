package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class Network extends Resource {

    private Integer address;
    private Integer mask;
    private Integer gatewayAddress;
    private Integer vlanNumber;
    @Transient
    private String addressAsString;
    @Transient
    private String gatewayAsString;

    @Override
    public void switchResource() {
        switchedOn = !switchedOn;
    }

    @JsonIgnore
    public Integer getAddress() {
        return address;
    }

    @JsonIgnore
    public void setAddress(Integer address) {
        this.address = address;
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    @JsonIgnore
    public Integer getGatewayAddress() {
        return gatewayAddress;
    }

    @JsonIgnore
    public void setGatewayAddress(Integer gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    public Integer getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(Integer vlanNumber) {
        this.vlanNumber = vlanNumber;
    }

    public String getAddressAsString() {
        return addressAsString;
    }

    public void setAddressAsString(String addressAsString) {
        this.addressAsString = addressAsString;
    }

    public String getGatewayAsString() {
        return gatewayAsString;
    }

    public void setGatewayAsString(String gatewayAsString) {
        this.gatewayAsString = gatewayAsString;
    }

    @Override
    public String toString() {
        return "Network{" +
                "id=" + this.getId() +
                ", name=" + this.getName() +
                ", switchedOn=" + this.getSwitchedOn() +
                ", address=" + address +
                ", mask=" + mask +
                ", gatewayAddress=" + gatewayAddress +
                ", vlanNumber=" + vlanNumber +
                ", addressAsString='" + addressAsString + '\'' +
                ", gatewayAsString='" + gatewayAsString + '\'' +
                '}';
    }
}
