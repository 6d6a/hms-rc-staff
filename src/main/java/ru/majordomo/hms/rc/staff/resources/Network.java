package ru.majordomo.hms.rc.staff.resources;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import com.fasterxml.jackson.annotation.JsonSetter;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Network extends Resource {

    private Long address;
    private Integer mask;
    private Long gatewayAddress;
    private Integer vlanNumber;

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
        return ipAddressInIntegerToString(address);
    }

    public void setAddress(Long address) {
        this.address = address;
    }

    @JsonSetter
    public void setAddress(String address) {
        this.address = ipAddressInStringToInteger(address);
    }

    public Integer getMask() {
        return mask;
    }

    public void setMask(Integer mask) {
        this.mask = mask;
    }

    @JsonIgnore
    public Long getGatewayAddress() {
        return gatewayAddress;
    }

    @JsonGetter(value = "gatewayAddress")
    public String getGatewayAddressAsString() {
        return ipAddressInIntegerToString(gatewayAddress);
    }

    public void setGatewayAddress(Long gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

    @JsonSetter
    public void setGatewayAddress(String gatewayAddress) {
        this.gatewayAddress = ipAddressInStringToInteger(gatewayAddress);
    }

    public Integer getVlanNumber() {
        return vlanNumber;
    }

    public void setVlanNumber(Integer vlanNumber) {
        this.vlanNumber = vlanNumber;
    }

    public Boolean isAddressIn(String address) {
        SubnetUtils subnetUtils = new SubnetUtils(getAddressAsString() + "/" + getMask());
        SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();
        return subnetInfo.isInRange(address);
    }

    public static String ipAddressInIntegerToString(Long ip) {
        return ((ip >> 24) & 0xFF) + "."
                + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 8) & 0xFF) + "."
                + (ip & 0xFF);
    }

    public static Long ipAddressInStringToInteger(String address) {
        long result = 0;
        String[] ipAddressInArray = address.split("\\.");
        for (int i = 3; i >= 0; i--) {
            long ip = Long.parseLong(ipAddressInArray[3-i]);
            result |= ip << (i * 8);
        }

        return result;
    }

    @Override
    public String toString() {
        return "Network{" +
                "id=" + this.getId() +
                ", name=" + this.getName() +
                ", switchedOn=" + this.getSwitchedOn() +
                ", address=" + this.getAddressAsString() +
                ", mask=" + mask +
                ", gatewayAddress=" + this.getGatewayAddressAsString() +
                ", vlanNumber=" + vlanNumber +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        Network network = (Network) o;

        if (getAddress() != null ? !getAddress().equals(network.getAddress()) : network.getAddress() != null)
            return false;
        if (getMask() != null ? !getMask().equals(network.getMask()) : network.getMask() != null) return false;
        if (getGatewayAddress() != null ? !getGatewayAddress().equals(network.getGatewayAddress()) : network.getGatewayAddress() != null)
            return false;
        return getVlanNumber() != null ? getVlanNumber().equals(network.getVlanNumber()) : network.getVlanNumber() == null;

    }
}
