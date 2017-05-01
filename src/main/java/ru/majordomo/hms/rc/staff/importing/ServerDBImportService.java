package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import ru.majordomo.hms.rc.staff.repositories.ServerRepository;
import ru.majordomo.hms.rc.staff.repositories.ServerRoleRepository;
import ru.majordomo.hms.rc.staff.repositories.ServiceRepository;
import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.Resource;
import ru.majordomo.hms.rc.staff.resources.Server;
import ru.majordomo.hms.rc.staff.resources.ServerRole;
import ru.majordomo.hms.rc.staff.resources.Service;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.Storage;

@Component
public class ServerDBImportService {
    private final static Logger logger = LoggerFactory.getLogger(ServerDBImportService.class);

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ServerRepository serverRepository;
    private final StorageRepository storageRepository;
    private final ServerRoleRepository serverRoleRepository;
    private final ServiceRepository serviceRepository;

    private List<Storage> storages;
    private List<ServerRole> serverRoles;
    private List<Service> services;

    @Autowired
    public ServerDBImportService(
            @Qualifier("billingNamedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            ServerRepository serverRepository,
            StorageRepository storageRepository,
            ServerRoleRepository serverRoleRepository,
            ServiceRepository serviceRepository
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.serverRepository = serverRepository;
        this.storageRepository = storageRepository;
        this.serverRoleRepository = serverRoleRepository;
        this.serviceRepository = serviceRepository;
    }

    private void pull() {
        storages = storageRepository.findAll();
        serverRoles = serverRoleRepository.findAll();
        services = serviceRepository.findAll();

        seedDBServer();
        seedTestData();

        String query = "SELECT s.id, s.name, s.home_base " +
                "FROM servers s " +
                "JOIN account a ON a.server_id=s.id " +
                "WHERE s.name = 'baton' OR s.name = 'staff' OR s.name LIKE 'web%'" +
                "GROUP BY a.server_id";

        namedParameterJdbcTemplate.query(query, this::rowMap);

        query = "SELECT p.id, p.name, p.homedir " +
                "FROM poppers p " +
                "JOIN account a ON a.popper=p.id " +
                "WHERE p.name != 'storage.din.ru'" +
                "GROUP BY a.popper";

        namedParameterJdbcTemplate.query(query, this::rowMapPop);
    }

    private ServiceSocket rowMap(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found WebServer: " + rs.getString("name"));

        String name = rs.getString("name");
        Server server = new Server();
        server.setId("web_server_" + rs.getString("id"));
        server.setSwitchedOn(true);
        server.setName(name);
        server.setServiceIds(services
                .stream()
                .filter(service -> service.getName().contains("@" + name))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setServerRoleIds(serverRoles
                .stream()
                .filter(serverRole -> (serverRole.getName().equals("shared-hosting") || (!name.equals("baton") && serverRole.getName().equals("mysql-database-server"))))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setStorageIds(storages
                .stream()
                .filter(storage -> storage.getName().equals(name))
                .map(Resource::getId)
                .collect(Collectors.toList()));

        serverRepository.save(server);

        return null;
    }

    private ServiceSocket rowMapPop(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found PopServer: " + rs.getString("name"));

        Server server = new Server();
        server.setId("mail_server_" + rs.getString("id"));
        server.setSwitchedOn(true);
        String[] name = rs.getString("name").split("\\.");
        server.setName(name[0]);
        server.setServiceIds(Collections.emptyList());
        server.setServerRoleIds(serverRoles
                .stream()
                .filter(serverRole -> serverRole.getName().equals("mail-storage"))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setStorageIds(storages
                .stream()
                .filter(storage -> storage.getName().equals(name[0]))
                .map(Resource::getId)
                .collect(Collectors.toList()));

        serverRepository.save(server);

        return null;
    }

    private void seedDBServer() {
        Server server;

        server = new Server();
        server.setSwitchedOn(true);
        server.setId("db_server_20");
        server.setName("mdb4");
        server.setServiceIds(services
                .stream()
                .filter(service -> service.getName().contains("@mdb4"))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setServerRoleIds(serverRoles
                .stream()
                .filter(serverRole -> serverRole.getName().equals("mysql-database-server"))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setStorageIds(storages
                .stream()
                .filter(storage -> storage.getName().equals("mdb4"))
                .map(Resource::getId)
                .collect(Collectors.toList()));

        serverRepository.save(server);
    }

    private void seedTestData() {
        Server server;

        server = new Server();
        server.setSwitchedOn(true);
        server.setId("5821f8c596ccde0001c82a61");
        server.setName("web99");
        server.setServiceIds(services
                .stream()
                .filter(service -> service.getName().contains("@web99"))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setServerRoleIds(serverRoles
                .stream()
                .filter(serverRole -> (serverRole.getName().equals("shared-hosting") || serverRole.getName().equals("mysql-database-server")))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setStorageIds(storages
                .stream()
                .filter(storage -> storage.getName().equals("web99"))
                .map(Resource::getId)
                .collect(Collectors.toList()));

        serverRepository.save(server);

        server = new Server();
        server.setSwitchedOn(true);
        server.setId("584eb62d96ccde00012776f7");
        server.setName("pop99");
        server.setServiceIds(Collections.emptyList());
        server.setServerRoleIds(serverRoles
                .stream()
                .filter(serverRole -> serverRole.getName().equals("mail-storage"))
                .map(Resource::getId)
                .collect(Collectors.toList()));
        server.setStorageIds(storages
                .stream()
                .filter(storage -> storage.getName().equals("pop99"))
                .map(Resource::getId)
                .collect(Collectors.toList()));

        serverRepository.save(server);
    }

    public boolean importToMongo() {
        serverRepository.deleteAll();
        pull();
        return true;
    }
}
