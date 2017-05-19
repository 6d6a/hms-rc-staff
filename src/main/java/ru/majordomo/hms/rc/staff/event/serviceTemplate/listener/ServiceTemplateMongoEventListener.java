package ru.majordomo.hms.rc.staff.event.serviceTemplate.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.resources.ConfigTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;
import ru.majordomo.hms.rc.staff.resources.ServiceType;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class ServiceTemplateMongoEventListener extends AbstractMongoEventListener<ServiceTemplate> {
    private final MongoOperations mongoOperations;

    @Autowired
    public ServiceTemplateMongoEventListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<ServiceTemplate> event) {
        super.onAfterConvert(event);
        buildServiceTemplate(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<ServiceTemplate> event) {
        super.onBeforeSave(event);
        buildServiceTemplate(event.getSource());
    }

    private void buildServiceTemplate(ServiceTemplate serviceTemplate) {
        if (serviceTemplate.getServiceTypeName() != null) {
            serviceTemplate.setServiceType(mongoOperations.findOne(new Query(where("name").is(serviceTemplate.getServiceTypeName())), ServiceType.class));
        }
        if (!serviceTemplate.getConfigTemplateIds().isEmpty()) {
            serviceTemplate.setConfigTemplates(mongoOperations.find(new Query(where("_id").in(serviceTemplate.getConfigTemplateIds())), ConfigTemplate.class));
        }
    }
}
