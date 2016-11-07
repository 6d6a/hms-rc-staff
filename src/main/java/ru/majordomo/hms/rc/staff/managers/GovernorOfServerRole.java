package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfServerRole extends LordOfResources{
    private ServerRoleRepository serverRoleRepository;
    private GovernorOfServiceTemplate governorOfServiceTemplate;
    private Cleaner cleaner;

    @Autowired
    public void setServerRoleRepository(ServerRoleRepository serverRoleRepository) {
        this.serverRoleRepository = serverRoleRepository;
    }

    @Autowired
    public void setGovernor(GovernorOfServiceTemplate governorOfServiceTemplate) {
        this.governorOfServiceTemplate = governorOfServiceTemplate;
    }

    @Autowired
    public void setCleaner(Cleaner cleaner) {
        this.cleaner = cleaner;
    }

    @Override
    public Resource createResource(ServiceMessage serviceMessage) throws ParameterValidateException {
        ServerRole serverRole = new ServerRole();
        try {
            LordOfResources.setResourceParams(serverRole, serviceMessage, cleaner);

            List<ServiceTemplate> serviceTemplates = (List<ServiceTemplate>)serviceMessage.getParam("serviceTemplates");
            serverRole.setServiceTemplates(serviceTemplates);
            isValid(serverRole);

            if (serverRole.getServiceTemplates().isEmpty()) {
                throw new ParameterValidateException("Должен быть задан хотя бы один service template");
            }

            //Имя сервереной роли должно быть уникально
            List<ServerRole> existedServerRoles = build();
            for (ServerRole existedServerRole: existedServerRoles) {
                if (existedServerRole.getName().equals(serverRole.getName())) {
                    throw new ParameterValidateException("Server Role c именем: " + serverRole.getName() + " уже существует");
                }
            }

            save(serverRole);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return serverRole;
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServerRole serverRole = (ServerRole) resource;
        if (serverRole.getServiceTemplates().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ServiceTemplate");
        }
        if (serverRole.getServiceTemplateIds().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ServiceTemplateId");
        }

        //Валидация ServiceTemplate
        for (ServiceTemplate serviceTemplateToValidate : serverRole.getServiceTemplates()) {
            ServiceTemplate serviceTemplateFromRepository = (ServiceTemplate) governorOfServiceTemplate.build(serviceTemplateToValidate.getId());
            if (serviceTemplateFromRepository == null) {
                throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " не найден");
            }
            if(!serviceTemplateFromRepository.equals(serviceTemplateToValidate)) {
                throw new ParameterValidateException("ServiceTemplate с ID: " + serviceTemplateToValidate.getId() + " задан некорректно");
            }
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        ServerRole serverRole = serverRoleRepository.findOne(resourceId);
        if (serverRole == null) {
            throw new ResourceNotFoundException("ServerRole с ID:" + resourceId + " не найден");
        }

        for (String serviceTemplateId: serverRole.getServiceTemplateIds()) {
            ServiceTemplate serviceTemplate = (ServiceTemplate) governorOfServiceTemplate.build(serviceTemplateId);

            serverRole.addServiceTemplate(serviceTemplate);
        }
        return serverRole;
    }

    @Override
    public List<ServerRole> build(Map<String, String> keyValue) {

        List<ServerRole> buildedServerRoles = new ArrayList<>();

        Boolean byName = false;

        for (Map.Entry<String, String> entry : keyValue.entrySet()) {
            if (entry.getKey().equals("name")) {
                byName = true;
            }
        }

        if (byName) {
            for (ServerRole serverRole : serverRoleRepository.findByName(keyValue.get("name"))) {
                buildedServerRoles.add((ServerRole) build(serverRole.getId()));
            }
        } else {
            for (ServerRole serverRole : serverRoleRepository.findAll()) {
                buildedServerRoles.add((ServerRole) build(serverRole.getId()));
            }
        }

        return buildedServerRoles;
    }

    @Override
    public List<ServerRole> build() {
        List<ServerRole> buildedServerRoles = new ArrayList<>();
        for (ServerRole serverRole : serverRoleRepository.findAll()) {
            buildedServerRoles.add((ServerRole) build(serverRole.getId()));
        }
        return buildedServerRoles;
    }

    @Override
    public void save(Resource resource) {
        serverRoleRepository.save((ServerRole) resource);
    }

    @Override
    public void delete(String resourceId) {
        serverRoleRepository.delete(resourceId);
    }

}
