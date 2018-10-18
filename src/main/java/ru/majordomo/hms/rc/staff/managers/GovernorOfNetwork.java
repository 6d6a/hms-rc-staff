package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.NetworkRepository;
import ru.majordomo.hms.rc.staff.resources.Network;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.validation.group.NetworkChecks;

@Component
public class GovernorOfNetwork extends LordOfResources<Network> {
    private GovernorOfServiceSocket governorOfServiceSocket;
    private Cleaner cleaner;
    private Validator validator;

    @Autowired
    public void setRepository(NetworkRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServiceSocket(GovernorOfServiceSocket governorOfServiceSocket) {
        this.governorOfServiceSocket = governorOfServiceSocket;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
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

        repository.save(network);

        return network;
    }

    @Override
    public void isValid(Network network) throws ParameterValidateException {
        Set<ConstraintViolation<Network>> constraintViolations = validator.validate(network, NetworkChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("network: " + network + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Network build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public void preDelete(String resourceId) {
        Network network = repository.findById(resourceId).orElseThrow(() -> new ResourceNotFoundException("сеть не найдена"));
        List<ServiceSocket> sockets = governorOfServiceSocket.buildAll();
        for (ServiceSocket socket : sockets) {
            if (network.isAddressIn(socket.getAddressAsString())) {
                throw new ParameterValidateException("Я нашла ServiceSocket с ID " + socket.getId()
                        + ", именуемый " + socket.getName() + ", он так то в удаляемом Network");
            }
        }
    }
}
