package ru.majordomo.hms.rc.staff.managers;

import com.google.common.net.InetAddresses;

import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;

import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@Component
public class GovernorOfNetwork extends LordOfResources {
    private NetworkRepository networkRepository;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(NetworkRepository repository) {
        this.networkRepository = repository;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Network network = new Network();
        try {
            network = (Network) LordOfResources.setResourceParams(network, serviceMessage, cleaner);
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            Integer netmask = (Integer) serviceMessage.getParam("mask");
            String gwAddress = cleaner.cleanString((String) serviceMessage.getParam("gatewayAddress"));
            Integer vlanNumber = (Integer) serviceMessage.getParam("vlanNumber");

            network.setAddress(address);
            network.setGatewayAddress(gwAddress);
            network.setVlanNumber(vlanNumber);
            network.setMask(netmask);
            isValid(network);

        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }

        networkRepository.save(network);

        return network;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        Network network = (Network) resource;

        Long addressAsLong = network.getAddress();
        if (addressAsLong < 0L || addressAsLong > 4294967295L) {
            throw new ParameterValidateException("параметр address указан неверно");
        }

        String address = network.getAddressAsString();
        if (address.equals("") || !InetAddresses.isInetAddress(address)) {
            throw new ParameterValidateException("параметр address указан неверно");
        }

        Integer netmask = network.getMask();
        if (netmask < 0 || netmask > 30) {
            throw new ParameterValidateException("значение параметра mask должно находиться в диапазоне от 1 до 30");
        }

        String gwAddress = network.getGatewayAddressAsString();
        if (gwAddress.equals("") || !InetAddresses.isInetAddress(gwAddress)) {
            throw new ParameterValidateException("gatewayAddress должен быть указан");
        }

        Integer vlanNumber = network.getVlanNumber();
        if (vlanNumber < 0 || vlanNumber > 4096) {
            throw new ParameterValidateException("значение параметра vlanNumber должно находиться в диапазоне от 0 до 4096");
        }

        SubnetUtils subnetUtils = new SubnetUtils(address + "/" + netmask);
        SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();
        if (!subnetInfo.isInRange(gwAddress)) {
            throw new ParameterValidateException(gwAddress + " не входит в сеть " + address + "/" + netmask.toString());
        }


    }

}
