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
import ru.majordomo.hms.rc.staff.resources.socket.Socket;
import ru.majordomo.hms.rc.staff.resources.template.Template;

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
        if (service.getServiceTemplateId() != null) {
            service.setServiceTemplate(mongoOperations.findOne(new Query(where("_id").is(service.getServiceTemplateId())), ServiceTemplate.class));
        }
        if (!service.getServiceSocketIds().isEmpty()) {
            service.setServiceSockets(mongoOperations.find(new Query(where("_id").in(service.getServiceSocketIds())), ServiceSocket.class));
        }
        if (service.getTemplateId() != null) {
            service.setTemplate(mongoOperations.findOne(new Query(where("_id").is(service.getTemplateId())), Template.class));
        }
        if (!service.getSocketIds().isEmpty()) {
            service.setSockets(mongoOperations.find(new Query(where("_id").in(service.getSocketIds())), Socket.class));
        }
    }
}
