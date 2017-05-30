package ru.majordomo.hms.rc.staff.event.serverRole.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.ServiceTemplate;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class ServerRoleMongoEventListener extends AbstractMongoEventListener<ServerRole> {
    private final MongoOperations mongoOperations;

    @Autowired
    public ServerRoleMongoEventListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<ServerRole> event) {
        super.onAfterConvert(event);
        buildServiceTemplate(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<ServerRole> event) {
        super.onBeforeSave(event);
        buildServiceTemplate(event.getSource());
    }

    private void buildServiceTemplate(ServerRole serverRole) {
        if (!serverRole.getServiceTemplateIds().isEmpty()) {
            serverRole.setServiceTemplates(mongoOperations.find(new Query(where("_id").in(serverRole.getServiceTemplateIds())), ServiceTemplate.class));
        }
    }
}
