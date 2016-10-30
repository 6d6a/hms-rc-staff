package ru.majordomo.hms.rc.staff.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javafx.util.Pair;
import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Service
public class GovernorOfServiceSocket extends LordOfResources {

    private Cleaner cleaner;
    private NetworkRepository networkRepository;
    private ServiceSocketRepository serviceSocketRepository;

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfServiceSocket.class);

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setServiceSocketRepository(ServiceSocketRepository serviceSocketRepository) {
        this.serviceSocketRepository = serviceSocketRepository;
    }

    @Autowired
    public void setNetworkRepository(NetworkRepository networkRepository) {
        this.networkRepository = networkRepository;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {

        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceSocket serviceSocket = new ServiceSocket();
        LordOfResources.setResourceParams(serviceSocket, serviceMessage, cleaner);

        try {
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            Integer port = (Integer) serviceMessage.getParam("port");
            serviceSocket.setAddress(address);
            serviceSocket.setPort(port);
            isValid(serviceSocket);
            save(serviceSocket);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return serviceSocket;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServiceSocket socket = (ServiceSocket) resource;
        String address = socket.getAddressAsString();
        Integer port = socket.getPort();
        Boolean inRange = Boolean.FALSE;
        List<Network> networkList = new ArrayList<>();
        networkList = networkRepository.findAll();

        for (Network network : networkList) {
            if (network.isAddressIn(address)) {
                inRange = Boolean.TRUE;
                break;
            }
        }
        if (!inRange) {
            throw new ParameterValidateException("Адрес: " + address + " не принадлежит ни к одной известной сети");
        }
        if (port < 1 || port > 65535) {
            throw new ParameterValidateException("Значение параметра port может находиться в пределах диапазоне 1-65535");
        }

    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        ServiceSocket serviceSocket = serviceSocketRepository.findOne(resourceId);
        if (serviceSocket == null) {
            throw new ResourceNotFoundException("ServiceSocket с ID:" + resourceId + " не найден");
        }
        return serviceSocket;
    }

    @Override
    public List<ServiceSocket> buildAll(String key) {
        switch (key) {
            case "": {
                return serviceSocketRepository.findAll();
            }
            default: {
                return serviceSocketRepository.findByName(key);
            }
        }
    }

    @Override
    public void save(Resource resource) {
        serviceSocketRepository.save((ServiceSocket) resource);
    }

    @Override
    public void delete(String resourceId) {
        serviceSocketRepository.delete(resourceId);
    }

}
