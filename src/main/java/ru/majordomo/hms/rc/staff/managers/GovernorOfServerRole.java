package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
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
    public ServerRole buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        ServerRole serverRole = new ServerRole();

        try {
            LordOfResources.setResourceParams(serverRole, serviceMessage, cleaner);

            @SuppressWarnings("unchecked") List<String> serviceTemplateIds = (List<String>)serviceMessage.getParam("serviceTemplateIds");
            serverRole.setServiceTemplateIds(serviceTemplateIds);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("???????? ???? ???????????????????? ???????????? ??????????????:" + e.getMessage());
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
    public void preDelete(String resourceId) {
        List<Server> servers = governorOfServer.buildAll();
        for (Server server : servers) {
            if (server.getServerRoleIds().contains(resourceId)) {
                throw new ParameterValidateException("?? ?????????? Server ?? ID " + server.getId()
                        + ", ?????????????????? " + server.getName() + ", ?????? ?????? ?? ?????? ?????????????? ?????????????????? ServerRole.");
            }
        }
    }
}
