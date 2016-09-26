package ru.majordomo.hms.rc.staff.managers;

import com.google.common.net.InetAddresses;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;

@Component
public class GovernorOfNetwork extends LordOfResources {
    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    Cleaner cleaner;

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Network network = new Network();
        try {
            InetAddress inetAddress = null;
            InetAddress gwInetAddress = null;

            network = (Network) LordOfResources.setResourceParams(network, serviceMessage, cleaner);
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            if (address.equals("") || !InetAddresses.isInetAddress(address)) {
                throw new ParameterValidateException("параметр address указан неверно");
            }

            Integer netmask = (Integer) serviceMessage.getParam("mask");
            if (netmask < 0 || netmask > 30) {
                throw new ParameterValidateException("значение параметра mask должно находиться в диапазоне от 1 до 30");
            }

            String gwAddress = cleaner.cleanString((String) serviceMessage.getParam("gatewayAddress"));
            if (gwAddress.equals("") || !InetAddresses.isInetAddress(gwAddress)) {
                throw new ParameterValidateException("gatewayAddress должен быть указан");
            }

            Integer vlanNumber = (Integer) serviceMessage.getParam("vlanNumber");
            if (vlanNumber < 0 || vlanNumber > 4096) {
                throw new ParameterValidateException("значение параметра vlanNumber должно находиться в диапазоне от 0 до 4096");
            }

            SubnetUtils subnetUtils = new SubnetUtils(address + "/" + netmask);
            SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();
            if (!subnetInfo.isInRange(gwAddress)) {
                throw new ParameterValidateException(gwAddress + " не входит в сеть " + address + "/" + netmask.toString());
            }

            network.setAddress(address);
            network.setGatewayAddress(gwAddress);
            network.setVlanNumber(vlanNumber);
            network.setMask(netmask);
        } catch (ClassCastException | IllegalArgumentException e) {
            throw new ParameterValidateException("один из параметро указан неверно:" + e.getMessage());
        }
        networkRepository.save(network);

        return network;
    }

}
