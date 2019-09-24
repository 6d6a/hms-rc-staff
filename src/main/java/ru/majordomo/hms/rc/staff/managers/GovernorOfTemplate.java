package ru.majordomo.hms.rc.staff.managers;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import ru.majordomo.hms.rc.staff.event.template.TemplateUpdatedEvent;
import ru.majordomo.hms.rc.staff.exception.ParameterValidateException;
import ru.majordomo.hms.rc.staff.exception.ResourceNotFoundException;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.template.TemplateRepository;
import ru.majordomo.hms.rc.staff.resources.template.Template;
import ru.majordomo.hms.rc.staff.resources.validation.group.TemplateChecks;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class GovernorOfTemplate extends LordOfResources<Template> {
    private ServiceRepository serviceRepository;
    private Validator validator;
    private ApplicationEventPublisher publisher;

    @Autowired
    public void setServiceTemplateRepository(TemplateRepository repository) {
        this.repository = repository;
    }

    @Autowired
    public void setServiceRepository(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Autowired
    public void setValidator(Validator validator) {
        this.validator = validator;
    }

    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    @Override
    public void isValid(Template template) throws ParameterValidateException {
        Set<ConstraintViolation<Template>> constraintViolations = validator.validate(template, TemplateChecks.class);

        if (!constraintViolations.isEmpty()) {
            logger.error("template: " + template + " constraintViolations: " + constraintViolations.toString());
            throw new ConstraintViolationException(constraintViolations);
        }
    }

    @Override
    public void save(Template template) {
        isValid(template);
        repository.save(template);
        //TODO amqp service update
        if (serviceRepository.existsByTemplateId(template.getId())) {
            publisher.publishEvent(new TemplateUpdatedEvent(template.getId()));
        }
    }

    @Override
    public Template build(Map<String, String> keyValue) throws NotImplementedException {
        if (keyValue.get("resourceId") != null && keyValue.get("availableToAccounts") != null) {
            return ((TemplateRepository) repository).findByIdAndAvailableToAccounts(
                    keyValue.get("resourceId"),
                    Boolean.parseBoolean(keyValue.get("availableToAccounts"))
            );
        } else {
            throw new ResourceNotFoundException("Template не найден");
        }
    }

    @Override
    public void preDelete(String resourceId) {
        if (serviceRepository.existsByTemplateId(resourceId)) {
            throw new ParameterValidateException("Я нашла Service в котором используется" +
                    " удаляемый Template. What's wrong with this rebjatishki?");
        }
    }

    @Override
    public List<Template> buildAll(Map<String, String> keyValue) {
        if (keyValue.get("name") != null) {
            return repository.findByName(keyValue.get("name"));
        } else if (keyValue.get("availableToAccounts") != null) {
            return ((TemplateRepository) repository).findByAvailableToAccounts(Boolean.parseBoolean(keyValue.get("availableToAccounts")));
        } else {
            return repository.findAll();
        }
    }
}
