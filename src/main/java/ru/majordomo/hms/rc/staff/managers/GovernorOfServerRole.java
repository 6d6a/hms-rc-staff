package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServerRoleChecks;

@Component
public class GovernorOfServerRole extends LordOfResources<ServerRole> {
    private GovernorOfServer governorOfServer;
    private Cleaner cleaner;
    private Validator validator;

    @Autowired
    public void setServerRoleRepository(ServerRoleRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
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
    public ServerRole createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        ServerRole serverRole = new ServerRole();
        try {
            LordOfResources.setResourceParams(serverRole, serviceMessage, cleaner);

            @SuppressWarnings("unchecked") List<String> serviceTemplateIds = (List<String>)serviceMessage.getParam("serviceTemplateIds");
            serverRole.setServiceTemplateIds(serviceTemplateIds);

            isValid(serverRole);
            save(serverRole);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return serverRole;
    }

    @Override
    public void isValid(ServerRole serverRole) throws ParameterValidateException {
        Set<ConstraintViolation<ServerRole>> constraintViolations = validator.validate(serverRole, ServerRoleChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("serverRole: " + serverRole + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public ServerRole build(Map<String, String> keyValue) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public List<ServerRole> buildAll(Map<String, String> keyValue) {

        List<ServerRole> buildedServerRoles = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            ServerRole serverRole = repository.findOneByName(keyValue.get("name"));
            buildedServerRoles.add(build(serverRole.getId()));

        } else {
            ServerRole serverRole = repository.findOneByName(keyValue.get("name"));
            buildedServerRoles.add(build(serverRole.getId()));
        }

        return buildedServerRoles;
    }

    @Override
    public Page<ServerRole> buildAllPageable(Pageable pageable) {
        throw new NotImplementedException();
    }

    @Override
    public Page<ServerRole> buildAllPageable(Map<String, String> keyValue, Pageable pageable) {
        throw new NotImplementedException();
    }


    @Override
    public void preDelete(String resourceId) {
        List<Server> servers = governorOfServer.buildAll();
        for (Server server : servers) {
            if (server.getServerRoleIds().contains(resourceId)) {
                throw new ParameterValidateException("Я нашла Server с ID " + server.getId()
                        + ", именуемый " + server.getName() + ", так вот в нём имеется удаляемый ServerRole.");
            }
        }
    }
}
