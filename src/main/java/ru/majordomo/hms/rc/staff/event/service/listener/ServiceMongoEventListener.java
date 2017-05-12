package ru.majordomo.hms.rc.staff.event.service.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class ServiceMongoEventListener extends AbstractMongoEventListener<Service> {
    private final MongoOperations mongoOperations;

    @Autowired
    public ServiceMongoEventListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<Service> event) {
        super.onAfterConvert(event);
        buildServiceTemplate(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Service> event) {
        super.onBeforeSave(event);
        buildServiceTemplate(event.getSource());
    }

    private void buildServiceTemplate(Service service) {
        service.setServiceTemplate(mongoOperations.findOne(new Query(where("_id").is(service.getServiceTemplateId())), ServiceTemplate.class));
        service.setServiceSockets(mongoOperations.find(new Query(where("_id").in(service.getServiceSocketIds())), ServiceSocket.class));
    }
}
