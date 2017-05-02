package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBImportService {
    private final static Logger logger = LoggerFactory.getLogger(DBImportService.class);

    private final ServiceSocketDBImportService serviceSocketDBImportService;
    private final ServiceTypeDBSeedService serviceTypeDBSeedService;
    private final ConfigTemplateDBSeedService configTemplateDBSeedService;
    private final ServiceTemplateDBSeedService serviceTemplateDBSeedService;
    private final ServiceDBSeedService serviceDBSeedService;
    private final ServerRoleDBSeedService serverRoleDBSeedService;
    private final StorageDBImportService storageDBImportService;
    private final ServerDBImportService serverDBImportService;
    private final NetworkDBSeedService networkDBSeedService;

    @Autowired
    public DBImportService(
            ServiceSocketDBImportService serviceSocketDBImportService,
            ServiceTypeDBSeedService serviceTypeDBSeedService,
            ConfigTemplateDBSeedService configTemplateDBSeedService,
            ServiceTemplateDBSeedService serviceTemplateDBSeedService,
            ServiceDBSeedService serviceDBSeedService,
            ServerRoleDBSeedService serverRoleDBSeedService,
            StorageDBImportService storageDBImportService,
            ServerDBImportService serverDBImportService,
            NetworkDBSeedService networkDBSeedService
    ) {
        this.serviceSocketDBImportService = serviceSocketDBImportService;
        this.serviceTypeDBSeedService = serviceTypeDBSeedService;
        this.configTemplateDBSeedService = configTemplateDBSeedService;
        this.serviceTemplateDBSeedService = serviceTemplateDBSeedService;
        this.serviceDBSeedService = serviceDBSeedService;
        this.serverRoleDBSeedService = serverRoleDBSeedService;
        this.storageDBImportService = storageDBImportService;
        this.serverDBImportService = serverDBImportService;
        this.networkDBSeedService = networkDBSeedService;
    }

    public boolean seedDB() {
        boolean seeded;

        seeded = networkDBSeedService.seedDB();
        logger.debug(seeded ? "network db_seeded" : "network db_not_seeded");

        seeded = serviceTypeDBSeedService.seedDB();
        logger.debug(seeded ? "serviceType db_seeded" : "serviceType db_not_seeded");

        seeded = configTemplateDBSeedService.seedDB();
        logger.debug(seeded ? "configTemplate db_seeded" : "configTemplate db_not_seeded");

        seeded = serviceTemplateDBSeedService.seedDB();
        logger.debug(seeded ? "serviceTemplate db_seeded" : "serviceTemplate db_not_seeded");

        seeded = serviceSocketDBImportService.importToMongo();
        logger.debug(seeded ? "serviceSocket db_imported" : "serviceSocket db_not_imported");

        seeded = serviceDBSeedService.seedDB();
        logger.debug(seeded ? "service db_seeded" : "service db_not_seeded");

        seeded = serverRoleDBSeedService.seedDB();
        logger.debug(seeded ? "serverRole db_seeded" : "serverRole db_not_seeded");

        seeded = storageDBImportService.importToMongo();
        logger.debug(seeded ? "storage db_imported" : "storage db_not_imported");

        seeded = serverDBImportService.importToMongo();
        logger.debug(seeded ? "server db_imported" : "server db_not_imported");

        return true;
    }

    public boolean importToMongo() {
        boolean imported;

        return true;
    }

    public boolean importToMongo(String accountId) {
        boolean imported;

        return true;
    }
}
