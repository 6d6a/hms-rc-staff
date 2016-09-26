package ru.majordomo.hms.rc.staff.resources;

import com.google.common.net.InetAddresses;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.data.mongodb.core.mapping.Document;

import java.net.InetAddress;

import ru.majordomo.hms.rc.staff.Resource;

@Document
public class Network extends Resource {

    private Integer address;
    private Integer mask;
    private Integer gatewayAddress;
    private Integer vlanNumber;

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
        return ipAddressInIntegerToString(address);
    }

    public void setAddress(Integer address) {
        this.address = address;
    }

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
    public Integer getGatewayAddress() {
        return gatewayAddress;
    }

    @JsonGetter(value = "gatewayAddress")
    public String getGatewayAddressAsString() {
        return ipAddressInIntegerToString(gatewayAddress);
    }

    public void setGatewayAddress(Integer gatewayAddress) {
        this.gatewayAddress = gatewayAddress;
    }

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

    public static String ipAddressInIntegerToString(Integer inetAddress) {
        return InetAddresses.fromInteger(inetAddress).toString().replace("/", "");
    }

    public static Integer ipAddressInStringToInteger(String address) {
        InetAddress inetAddress = InetAddresses.forString(address);
        byte[] octets = inetAddress.getAddress();
        Integer result = 0;
        for (byte octet : octets) {
            result <<= 8;
            result |= octet & 0xff;
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
                ", gatewayAddress=" + this.getAddressAsString() +
                ", vlanNumber=" + vlanNumber +
                '}';
    }
}
