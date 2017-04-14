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

    @Autowired
    public DBImportService(
            ServiceSocketDBImportService serviceSocketDBImportService,
            ServiceTypeDBSeedService serviceTypeDBSeedService,
            ConfigTemplateDBSeedService configTemplateDBSeedService,
            ServiceTemplateDBSeedService serviceTemplateDBSeedService,
            ServiceDBSeedService serviceDBSeedService
    ) {
        this.serviceSocketDBImportService = serviceSocketDBImportService;
        this.serviceTypeDBSeedService = serviceTypeDBSeedService;
        this.configTemplateDBSeedService = configTemplateDBSeedService;
        this.serviceTemplateDBSeedService = serviceTemplateDBSeedService;
        this.serviceDBSeedService = serviceDBSeedService;
    }

    public boolean seedDB() {
        boolean seeded;

        seeded = serviceTypeDBSeedService.seedDB();
        logger.debug(seeded ? "serviceType db_seeded" : "serviceType db_not_seeded");

        seeded = configTemplateDBSeedService.seedDB();
        logger.debug(seeded ? "configTemplate db_seeded" : "configTemplate db_not_seeded");

        seeded = serviceTemplateDBSeedService.seedDB();
        logger.debug(seeded ? "serviceTemplate db_seeded" : "serviceTemplate db_not_seeded");

        seeded = serviceDBSeedService.seedDB();
        logger.debug(seeded ? "service db_seeded" : "service db_not_seeded");

        return true;
    }

    public boolean importToMongo() {
        boolean imported;

        imported = serviceSocketDBImportService.importToMongo();
        logger.debug(imported ? "serviceSocket db_imported" : "serviceSocket db_not_imported");

        return true;
    }

    public boolean importToMongo(String accountId) {
        boolean imported;

        return true;
    }
}
