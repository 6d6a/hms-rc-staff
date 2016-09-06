package ru.majordomo.hms.rc.staff.resources;

import org.springframework.data.mongodb.core.mapping.Document;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class Network extends Resource {

    private Long address;
    private Short mask;
    private Long gatewayAddress;
    private Character vlanNumber;

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

    public Short getMask() {
        return mask;
    }

    public void setMask(Short mask) {
        this.mask = mask;
    }

    public Long getGatewayAddress() {
        return gatewayAddress;
    }

    public void setGatewayAddress(Long gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    public Character getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(Character vlanNumber) {
        this.vlanNumber = vlanNumber;
    }
}
