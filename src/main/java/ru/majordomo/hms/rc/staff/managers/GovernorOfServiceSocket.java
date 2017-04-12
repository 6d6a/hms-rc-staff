package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Service
public class GovernorOfServiceSocket extends LordOfResources<ServiceSocket> {

    private Cleaner cleaner;
    private NetworkRepository networkRepository;
    private ServiceSocketRepository serviceSocketRepository;
    private GovernorOfService governorOfService;

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

    @Autowired
    public void setGovernorOfService(GovernorOfService governorOfService) {
        this.governorOfService = governorOfService;
    }

    @Override
    public ServiceSocket createResource(ServiceMessage serviceMessage) throws ParameterValidateException {

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
    public void isValid(ServiceSocket resource) throws ParameterValidateException {
        String address = resource.getAddressAsString();
        Integer port = resource.getPort();
        Boolean inRange = Boolean.FALSE;
        List<Network> networkList;
        networkList = networkRepository.findAll();

        for (Network network : networkList) {
            if (network.isAddressIn(address)) {
                inRange = Boolean.TRUE;
                break;
            }
        }
        if (!inRange) {
            //TODO вернуть после загрузки адресов
//            throw new ParameterValidateException("Адрес: " + address + " не принадлежит ни к одной известной сети");
        }
        if (port < 1 || port > 65535) {
            throw new ParameterValidateException("Значение параметра port может находиться в пределах диапазоне 1-65535");
        }

    }

    @Override
    public ServiceSocket build(String resourceId) throws ResourceNotFoundException {
        ServiceSocket serviceSocket = serviceSocketRepository.findOne(resourceId);
        if (serviceSocket == null) {
            throw new ResourceNotFoundException("ServiceSocket с ID:" + resourceId + " не найден");
        }
        return serviceSocket;
    }

    @Override
    public ServiceSocket build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<ServiceSocket> buildAll(Map<String, String> keyValue) {

        List<ServiceSocket> buildedServiceSockets = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ServiceSocket serviceSocket : serviceSocketRepository.findByName(keyValue.get("name"))) {
                buildedServiceSockets.add(build(serviceSocket.getId()));
            }
        } else {
            for (ServiceSocket serviceSocket : serviceSocketRepository.findAll()) {
                buildedServiceSockets.add(build(serviceSocket.getId()));
            }
        }

        return buildedServiceSockets;
    }

    @Override
    public List<ServiceSocket> buildAll() {
        return serviceSocketRepository.findAll();
    }

    @Override
    public void save(ServiceSocket resource) {
        serviceSocketRepository.save(resource);
    }

    @Override
    public void preDelete(String resourceId) {
        List<ru.majordomo.hms.rc.staff.resources.Service> services = governorOfService.buildAll();
        for (ru.majordomo.hms.rc.staff.resources.Service service : services) {
            if (service.getServiceSocketIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла Service с ID " + service.getId()
                        + ", именуемый " + service.getName() + ", так вот в нём имеется удаляемый ServiceSocket");
            }
        }
    }

    @Override
    public void delete(String resourceId) {
        preDelete(resourceId);
        serviceSocketRepository.delete(resourceId);
    }

}
