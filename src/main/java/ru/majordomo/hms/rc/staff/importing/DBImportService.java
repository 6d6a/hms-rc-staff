package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DBImportService {
    private final static Logger logger = LoggerFactory.getLogger(DBImportService.class);

    private final ServiceSocketDBImportService serviceSocketDBImportService;

    @Autowired
    public DBImportService(
            ServiceSocketDBImportService serviceSocketDBImportService
    ) {
        this.serviceSocketDBImportService = serviceSocketDBImportService;
    }

    public boolean seedDB() {
        boolean seeded;

//        seeded = businessActionDBSeedService.seedDB();
//        logger.debug(seeded ? "businessFlow db_seeded" : "businessFlow db_not_seeded");

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
