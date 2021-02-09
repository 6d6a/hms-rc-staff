package ru.majordomo.hms.rc.staff.managers;

import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;
import java.util.*;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;

import ru.majordomo.hms.personmgr.exception.ParameterValidationException;
import ru.majordomo.hms.rc.staff.api.amqp.ServiceAmqpController;
import ru.majordomo.hms.rc.staff.common.MessageKeys;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.resources.*;
import ru.majordomo.hms.rc.staff.api.message.ServiceMessage;
import ru.majordomo.hms.rc.staff.cleaner.Cleaner;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.resources.template.DatabaseServer;
import ru.majordomo.hms.rc.staff.resources.template.HttpServer;
import ru.majordomo.hms.rc.staff.resources.template.ResourceType;
import ru.majordomo.hms.rc.staff.resources.template.Template;
import ru.majordomo.hms.rc.staff.resources.validation.group.ServiceChecks;

@Component
public class GovernorOfService extends LordOfResources<Service> {
    private GovernorOfServer governorOfServer;
    private GovernorOfSocket governorOfSocket;
    private GovernorOfTemplate governorOfTemplate;
    private Cleaner cleaner;
    private Validator validator;

    @Setter
    @Autowired
    private ServiceAmqpController serviceAmqpController;

    @Autowired
    public void setRepository(ServiceRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setGovernorOfServer(GovernorOfServer governorOfServer) {
        this.governorOfServer = governorOfServer;
    }

    @Autowired
    public void setGovernorOfSocket(GovernorOfSocket governorOfSocket) {
        this.governorOfSocket = governorOfSocket;
    }

    @Autowired
    public void setGovernorOfTemplate(GovernorOfTemplate governorOfTemplate) {
        this.governorOfTemplate = governorOfTemplate;
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
    public Service buildResourceFromServiceMessage(ServiceMessage serviceMessage) throws ClassCastException, UnsupportedEncodingException {
        //content_type:	application/json
        //{"operationIdentity":null,"actionIdentity":"5c5e0632aab6960001e90779","accountId":"100800","objRef":null,"params":{"templateId":"5d8251523ddcc03e078e0d91", "serverId":"5d1cc4a6e1442a0001c88634"}}
        Service service = new Service();

        try {
            LordOfResources.setResourceParams(service, serviceMessage, cleaner);
            String templateId = (String) serviceMessage.getParam("templateId");
            String serverId = (String) serviceMessage.getParam("serverId");
            String accountId = serviceMessage.getAccountId() != null ? cleaner.cleanString(serviceMessage.getAccountId()) : null;
            String homedir = Objects.toString(serviceMessage.getParam(MessageKeys.UNIX_ACCOUNT_HOMEDIR), "");

            if (accountId == null || accountId.equals("")) {
                throw new ParameterValidationException("Не указан accountId");
            }

            Server server;

            if (serverId != null) {
                server = governorOfServer.build(serverId);

                if (server != null) {
                    service.setServerId(serverId);
                } else {
                    throw new ParameterValidationException("Не найден Server с serverId: " + serverId);
                }
            } else  {
                throw new ParameterValidationException("Не указан serverId");
            }

            Template template;

            if (templateId != null) {
                template = governorOfTemplate.build(templateId);

                if (template == null) {
                    throw new ParameterValidationException("Не найден template с templateId: " + templateId);
                }

                if (!template.getAvailableToAccounts()) {
                    throw new ParameterValidationException("Указанный templateId не может быть использован для создания сервиса привязанного к аккаунту");
                }
            } else  {
                throw new ParameterValidationException("Не указан templateId");
            }

            if (((ServiceRepository) repository).existsByAccountIdAndTemplateId(accountId, templateId)) {
                throw new ParameterValidateException("Service с указанным templateId для этого аккаунта уже создан");
            }

            String serviceName = accountId + "-" + template.getName() + "@" + server.getName();
            service.setName(serviceName);
            service.setTemplateId(templateId);
            //TODO убрать потом после полного отказа от serviceTemplate
            if (template.getMigratedServiceTemplateIds().iterator().hasNext()) {
                service.setServiceTemplateId(template.getMigratedServiceTemplateIds().iterator().next());
            }
            service.setAccountId(accountId);
            if (StringUtils.isNotEmpty(accountId)) {
                service.addInstanceProp(Template.Spec.UNIX_ACCOUNT_HOMEDIR, homedir);
            }
            if (CollectionUtils.isNotEmpty(template.getNetworkingProtocols())) {
                for (String protocol : template.getNetworkingProtocols()) {
                    service.addSocket(governorOfSocket.generateForAccount(serviceName, protocol));
                }
            }
            if (CollectionUtils.isNotEmpty(template.getUnixSocketTemplates())) {
                for (String fileTemplate : template.getUnixSocketTemplates()) {
                    service.addSocket(governorOfSocket.generateUnixSocket(serviceName, fileTemplate, homedir));
                }
            }
            service.setSwitchedOn(true);
        } catch (ClassCastException e) {
            throw new ParameterValidateException("один из параметров указан неверно:" + e.getMessage());
        }

        return service;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Service update(ServiceMessage serviceMessage) throws ParameterValidationException {
        String resourceId = null;

        if (serviceMessage.getParam("resourceId") != null) {
            resourceId = (String) serviceMessage.getParam("resourceId");
        }

        String accountId = serviceMessage.getAccountId();
        Map<String, String> keyValue = new HashMap<>();
        keyValue.put("resourceId", resourceId);
        keyValue.put("accountId", accountId);

        Service service = build(keyValue);

        if (service != null) {
            try {
                for (Map.Entry<Object, Object> entry : serviceMessage.getParams().entrySet()) {
                    switch (entry.getKey().toString()) {
                        case "templateId":
                            service.setTemplateId((String) serviceMessage.getParam("templateId"));
                            break;
                        case "serverId":
                            service.setServerId((String) serviceMessage.getParam("serverId"));
                            break;
                        case "switchedOn":
                            service.setSwitchedOn((Boolean) entry.getValue());
                            break;
                        case "socketIds":
                            service.setSocketIds((List<String>) serviceMessage.getParam("socketIds"));
                            break;
                        default:
                            break;
                    }
                }
            } catch (ClassCastException e) {
                throw new ParameterValidationException("Один из параметров указан неверно");
            }

            preValidate(service);
            isValid(service);
            save(service);
        } else {
            throw new ResourceNotFoundException("Service по accountId и resourceId не найден");
        }

        return service;
    }

    @Override
    public void isValid(Service service) throws ParameterValidateException {
        Set<ConstraintViolation<Service>> constraintViolations = validator.validate(service, ServiceChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("service: " + service + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public Service build(Map<String, String> keyValue) throws NotImplementedException {
        if (keyValue.get("accountId") != null && keyValue.get("resourceId") != null) {
            Service service = repository.findById(keyValue.get("resourceId")).orElse(null);

            if (service == null || !keyValue.get("accountId").equals(service.getAccountId())) {
                throw new ResourceNotFoundException("Service по accountId и resourceId не найден");
            }

            return service;
        } else {
            throw new ResourceNotFoundException("Service не найден");
        }
    }

    @Override
    public List<Service> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("accountId") != null && keyValue.get("serverId") != null && keyValue.get("service-type") != null) {
            List<Service> services = new ArrayList<>();

            List<Service> accountServices = ((ServiceRepository) repository).findByAccountIdAndServerId(
                    keyValue.get("accountId"),
                    keyValue.get("serverId")
            );
            services.addAll(filterByServiceTypeName(accountServices, keyValue.get("service-type")));

            List<Service> serverServices = ((ServiceRepository) repository).findByServerIdAndWithoutAccountId(keyValue.get("serverId"));
            services.addAll(filterByServiceTypeName(serverServices, keyValue.get("service-type")));

            return services;
        } else if (keyValue.get("accountId") != null && keyValue.get("templateId") != null) {
            return ((ServiceRepository) repository).findByAccountIdAndTemplateId(keyValue.get("accountId"), keyValue.get("templateId"));
        } else if (keyValue.get("accountId") != null && keyValue.get("serverId") != null) {
            List<Service> services = ((ServiceRepository) repository).findByAccountIdAndServerId(
                    keyValue.get("accountId"),
                    keyValue.get("serverId")
            );

            List<Service> serverServices = ((ServiceRepository) repository).findByServerIdAndWithoutAccountId(keyValue.get("serverId"));

            services.addAll(serverServices);

            return services;
        } else if (keyValue.get("accountId") != null) {
            return ((ServiceRepository) repository).findByAccountId(keyValue.get("accountId"));
        } else if (keyValue.get("name") != null) {
            if (keyValue.get("regex") != null) {
                return repository.findByNameRegEx(keyValue.get("name"));
            }
            return repository.findByName(keyValue.get("name"));
        } else if (keyValue.get("serverId") != null && keyValue.get("service-type") != null) {
            List<Service> services = ((ServiceRepository) repository).findByServerId(keyValue.get("serverId"));
            return filterByServiceTypeName(services, keyValue.get("service-type"));
        } else if (keyValue.get("serverId") != null) {
            return ((ServiceRepository) repository).findByServerId(keyValue.get("serverId"));
        } else {
            return repository.findAll();
        }
    }

    private List<Service> filterByServiceTypeName(List<Service> services, String serviceTypeName) {
        List<Service> filteredServices = new ArrayList<>();

        if (serviceTypeName.matches("DATABASE|WEBSITE|STAFF|ACCESS")) {
            try {
                ResourceType resourceType = ResourceType.valueOf(serviceTypeName);
                filteredServices.addAll(
                        services.stream()
                                .filter(service -> service.getTemplate() != null
                                        && service.getTemplate().getResourceType() == resourceType)
                                .collect(Collectors.toList())
                );
            } catch (IllegalArgumentException e) {
                logger.error("Can not create ResourceType from " + serviceTypeName);
            }
        } else if (serviceTypeName.equals("STAFF_NGINX")) {
            filteredServices.addAll(
                    services.stream()
                            .filter(service -> service.getTemplate() != null
                                    && service.getTemplate().getResourceType() == ResourceType.WEBSITE
                                    && service.getTemplate() instanceof HttpServer)
                            .collect(Collectors.toList())
            );
        } else if (serviceTypeName.equals("DATABASE_MYSQL")) {
            filteredServices.addAll(
                    services.stream()
                            .filter(service -> service.getTemplate() != null
                                    && service.getTemplate().getResourceType() == ResourceType.DATABASE
                                    && service.getTemplate() instanceof DatabaseServer
                                    && ((DatabaseServer) service.getTemplate()).getType() == DatabaseServer.Type.MYSQL)
                            .collect(Collectors.toList())
            );
        } else {
            String[] parts = serviceTypeName.split("_");
            try {
                ResourceType resourceType = ResourceType.valueOf(parts[0]);
                filteredServices.addAll(
                        services.stream()
                                .filter(service -> service.getTemplate() != null
                                        && service.getTemplate().getResourceType() == resourceType)
                                .collect(Collectors.toList())
                );
            } catch (IllegalArgumentException e) {
                logger.error("Can not create ResourceType from " + parts[0]);
            }
        }

        return filteredServices;
    }

    /**
     * @param service
     * @param sendTeUpdate отправить сообщение об обновлении в TE через RabbitMQ
     */
    public void save(Service service, boolean sendTeUpdate) {
        save(service);
        if (sendTeUpdate) {
            serviceAmqpController.sendStaffToTEUpdate(service);
        }
    }
}
