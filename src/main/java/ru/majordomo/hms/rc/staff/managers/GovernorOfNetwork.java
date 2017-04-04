package ru.majordomo.hms.rc.staff.managers;

import com.google.common.net.InetAddresses;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.net.util.SubnetUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Component
public class GovernorOfNetwork extends LordOfResources<Network> {
    private NetworkRepository networkRepository;
    private GovernorOfServiceSocket governorOfServiceSocket;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(NetworkRepository repository) {
        this.networkRepository = repository;
    }

    @Autowired
    public void setGovernorOfServiceSocket(GovernorOfServiceSocket governorOfServiceSocket) {
        this.governorOfServiceSocket = governorOfServiceSocket;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Network createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
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
    public void isValid(Network resource) throws ParameterValidateException {
        Long addressAsLong = resource.getAddress();
        if (addressAsLong < 0L || addressAsLong > 4294967295L) {
            throw new ParameterValidateException("параметр address указан неверно");
        }

        String address = resource.getAddressAsString();
        if (address.equals("") || !InetAddresses.isInetAddress(address)) {
            throw new ParameterValidateException("параметр address указан неверно");
        }

        Integer netmask = resource.getMask();
        if (netmask < 0 || netmask > 30) {
            throw new ParameterValidateException("значение параметра mask должно находиться в диапазоне от 1 до 30");
        }

        String gwAddress = resource.getGatewayAddressAsString();
        if (gwAddress.equals("") || !InetAddresses.isInetAddress(gwAddress)) {
            throw new ParameterValidateException("gatewayAddress должен быть указан");
        }

        Integer vlanNumber = resource.getVlanNumber();
        if (vlanNumber < 0 || vlanNumber > 4096) {
            throw new ParameterValidateException("значение параметра vlanNumber должно находиться в диапазоне от 0 до 4096");
        }

        SubnetUtils subnetUtils = new SubnetUtils(address + "/" + netmask);
        SubnetUtils.SubnetInfo subnetInfo = subnetUtils.getInfo();
        if (!subnetInfo.isInRange(gwAddress)) {
            throw new ParameterValidateException(gwAddress + " не входит в сеть " + address + "/" + netmask.toString());
        }


    }

    @Override
    public Network build(String resourceId) throws ResourceNotFoundException {
        Network network = networkRepository.findOne(resourceId);
        if (network == null) {
            throw new ResourceNotFoundException("Network с ID:" + resourceId + " не найден");
        }
        return network;
    }

    @Override
    public Network build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<Network> buildAll(Map<String, String> keyValue) {

        List<Network> buildedNetworks = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (Network network : networkRepository.findByName(keyValue.get("name"))) {
                buildedNetworks.add(build(network.getId()));
            }
        } else {
            for (Network network : networkRepository.findAll()) {
                buildedNetworks.add(build(network.getId()));
            }
        }

        return buildedNetworks;
    }

    @Override
    public List<Network> buildAll() {
        return networkRepository.findAll();
    }

    @Override
    public void save(Network resource) {
        networkRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        Network network = networkRepository.findOne(resourceId);
        List<ServiceSocket> sockets = governorOfServiceSocket.buildAll();
        for (ServiceSocket socket : sockets) {
            if (network.isAddressIn(socket.getAddressAsString())) {
                throw new ParameterValidateException("Я нашла ServiceSocket с ID " + socket.getId()
                        + ", именуемый " + socket.getName() + ", он так то в удаляемом Network");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        networkRepository.delete(resourceId);
    }

}
