package ru.majordomo.hms.rc.staff.managers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ConfigTemplateRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceTemplateRepository;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

@Component
public class GovernorOfServerRole extends LordOfResources{
    private ServerRoleRepository repository;
    private ServiceTemplateRepository templateRepository;
    private ConfigTemplateRepository configTemplateRepository;
    private Cleaner cleaner;

    @Autowired
    public void setRepository(ServerRoleRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setTemplateRepository(ServiceTemplateRepository templateRepository) {
        this.templateRepository = templateRepository;
    }

    @Autowired
    public void setConfigTemplateRepository(ConfigTemplateRepository configTemplateRepository) {
        this.configTemplateRepository = configTemplateRepository;
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
            List<String> serviceTemplateIdList = cleaner.cleanListWithStrings((List<String>) serviceMessage.getParam("serviceTemplateList"));
            setServiceTemplatesByIds(serverRole, serviceTemplateIdList);
            if (serverRole.getServiceTemplates().isEmpty()) {
                throw new ParameterValidateException("Должен быть задан хотя бы один service template");
            }
            repository.save(serverRole);

        } catch (ClassCastException e) {
            throw new ParameterValidateException("Один из параметров указан неверно:" + e.getMessage());
        }
        return serverRole;
    }

    public void setServiceTemplatesByIds(ServerRole serverRole, List<String> serviceTemplateIds) throws ParameterValidateException {
        serverRole.setServiceTemplates((List<ServiceTemplate>) templateRepository.findAll(serviceTemplateIds));
    }

    @Override
    public void isValid(Resource resource) throws ParameterValidateException {
        ServerRole serverRole = (ServerRole) resource;
        if (serverRole.getServiceTemplates().isEmpty()) {
            throw new ParameterValidateException("Не найден ни один ServiceTemplate");
        }
        for (String serviceTemplateId: serverRole.getServiceTemplateIds()) {
            ServiceTemplate serviceTemplate = templateRepository.findOne(serviceTemplateId);
            if (serviceTemplate == null) {
                throw new ParameterValidateException("ServiceTemplate с ID:" + serviceTemplateId + " не найден");
            }
        }
    }

    @Override
    public Resource build(String resourceId) throws ResourceNotFoundException {
        return null;
    }
}
