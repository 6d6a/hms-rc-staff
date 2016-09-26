package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import ru.majordomo.hms.rc.staff.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Service
public class GovernorOfServiceSocket extends LordOfResources{

    @Autowired
    Cleaner cleaner;

    @Autowired
    NetworkRepository networkRepository;

    @Autowired
    ServiceSocketRepository serviceSocketRepository;

    private static final Logger logger = LoggerFactory.getLogger(GovernorOfServiceSocket.class);

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        Boolean inRange = Boolean.FALSE;
        String loggerPrefix = "OPERATION IDENTITY:" + serviceMessage.getOperationIdentity() + " ACTION IDENTITY:" + serviceMessage.getActionIdentity() + " ";
        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket = (ServiceSocket) LordOfResources.setResourceParams(serviceSocket,serviceMessage,cleaner);

        try {
            String address = cleaner.cleanString((String) serviceMessage.getParam("address"));
            List<Network> networkList = new ArrayList<>();
            networkList = networkRepository.findAll();
            for (Network network: networkList) {
                if (network.isAddressIn(address)) {
                    inRange = Boolean.TRUE;
                    break;
                }
            }
            if (!inRange) {
                throw new ParameterValidateException("Адрес: " + address + " не принадлежит ни к одной известной сети");
            }

            Integer port = (Integer) serviceMessage.getParam("port");
            if (port < 1 || port > 65535) {
                throw new ParameterValidateException("Значение параметра port может находиться в пределах диапазоне 1-65535");
            }
            serviceSocket.setAddress(Network.ipAddressInStringToInteger(address));
            serviceSocket.setPort(port);
            serviceSocketRepository.save(serviceSocket);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }
        return serviceSocket;
    }
}
