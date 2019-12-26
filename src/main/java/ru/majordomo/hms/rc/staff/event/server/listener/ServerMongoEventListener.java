package ru.majordomo.hms.rc.staff.event.server.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.mapping.event.AbstractMongoEventListener;
import org.springframework.data.mongodb.core.mapping.event.AfterConvertEvent;
import org.springframework.data.mongodb.core.mapping.event.AfterSaveEvent;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveEvent;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import ru.majordomo.hms.rc.staff.resources.*;

import java.util.List;
import java.util.stream.Collectors;

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
        if (event.getDocument() != null && event.getDocument().size() <= 2 && event.getDocument().containsKey("name")) {
            return;
        }
        loadTransientFields(event.getSource());
    }

    @Override
    public void onBeforeSave(BeforeSaveEvent<Server> event) {
        super.onBeforeSave(event);
        loadTransientFields(event.getSource());
    }

    @Override
    public void onAfterSave(AfterSaveEvent<Server> event) {
        super.onAfterSave(event);
        saveTransientFields(event.getSource());
    }

    private void loadTransientFields(Server server) {
        //TODO uncomment for migration
//        if(!server.getServiceIds().isEmpty()) {
//            server.setServices(mongoOperations.find(new Query(where("_id").in(server.getServiceIds())), Service.class));
//        }
//        if(!server.getStorageIds().isEmpty()) {
//            server.setStorages(mongoOperations.find(new Query(where("_id").in(server.getStorageIds())), Storage.class));
//        }
        //TODO comment for migration
        server.setServices(mongoOperations.find(new Query(where("serverId").is(server.getId())), Service.class));
        server.setStorages(mongoOperations.find(new Query(where("serverId").is(server.getId())), Storage.class));

        if(!server.getServerRoleIds().isEmpty()) {
            server.setServerRoles(mongoOperations.find(new Query(where("_id").in(server.getServerRoleIds())), ServerRole.class));
        }
    }

    private void saveTransientFields(Server server) {
        List<String> serviceIds = server.getServices().stream().map(Resource::getId).collect(Collectors.toList());
        mongoOperations.updateMulti(
                new Query(new Criteria("serverId").is(server.getId())),
                new Update().unset("serverId"),
                Service.class
        );

        if (!serviceIds.isEmpty()) {
            mongoOperations.updateMulti(
                    new Query(new Criteria("_id").in(serviceIds)),
                    new Update().set("serverId", server.getId()),
                    Service.class
            );
        }

        List<String> storageIds = server.getStorages().stream().map(Resource::getId).collect(Collectors.toList());
        mongoOperations.updateMulti(
                new Query(new Criteria("serverId").is(server.getId())),
                new Update().unset("serverId"),
                Storage.class
        );

        if (!storageIds.isEmpty()) {
            mongoOperations.updateMulti(
                    new Query(new Criteria("_id").in(storageIds)),
                    new Update().set("serverId", server.getId()),
                    Storage.class
            );
        }
    }
}
