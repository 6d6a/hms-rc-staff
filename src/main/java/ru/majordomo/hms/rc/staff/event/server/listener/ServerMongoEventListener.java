package ru.majordomo.hms.rc.staff.event.server.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.Storage;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Component
public class ServerMongoEventListener extends AbstractMongoEventListener<Server> {
    private final MongoOperations mongoOperations;

    @Autowired
    public ServerMongoEventListener(MongoOperations mongoOperations) {
        this.mongoOperations = mongoOperations;
    }

    @Override
    public void onAfterConvert(AfterConvertEvent<Server> event) {
        super.onAfterConvert(event);
        buildServiceTemplate(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Server> event) {
        super.onBeforeSave(event);
        buildServiceTemplate(event.getSource());
    }

    private void buildServiceTemplate(Server server) {
        if(!server.getServiceIds().isEmpty()) {
            server.setServices(mongoOperations.find(new Query(where("_id").in(server.getServiceIds())), Service.class));
        }
        if(!server.getStorageIds().isEmpty()) {
            server.setStorages(mongoOperations.find(new Query(where("_id").in(server.getStorageIds())), Storage.class));
        }
        if(!server.getServerRoleIds().isEmpty()) {
            server.setServerRoles(mongoOperations.find(new Query(where("_id").in(server.getServerRoleIds())), ServerRole.class));
        }
    }
}
