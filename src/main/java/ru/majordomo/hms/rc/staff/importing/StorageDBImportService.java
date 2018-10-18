package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

import ru.majordomo.hms.rc.staff.repositories.StorageRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;
import ru.majordomo.hms.rc.staff.resources.Storage;

@Service
@Profile("import")
public class StorageDBImportService {
    private final static Logger logger = LoggerFactory.getLogger(StorageDBImportService.class);

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final StorageRepository storageRepository;

    @Autowired
    public StorageDBImportService(
            @Qualifier("billingNamedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            StorageRepository storageRepository
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.storageRepository = storageRepository;
    }

    private void pull() {
        seedDBStorage();
        seedTestData();

        String query = "SELECT s.id, s.name, s.home_base " +
                "FROM servers s " +
                "JOIN account a ON a.server_id=s.id " +
                "WHERE s.name = 'baton' OR s.name = 'staff' OR s.name = 'chucho' OR s.name LIKE 'web%'" +
                "GROUP BY a.server_id";

        namedParameterJdbcTemplate.query(query, this::rowMap);

        query = "SELECT p.id, p.name, SUBSTRING_INDEX(SUBSTRING_INDEX(a.mailspool, '/', 9), '/', 2) as homedir " +
                "FROM poppers p " +
                "JOIN account a ON a.popper=p.id " +
                "WHERE p.name NOT IN ('storage.din.ru', 'pop3.majordomo.ru') " +
                "AND a.mailspool NOT IN ('', '/mail', '/homebiga/mail') " +
                "GROUP BY p.name, homedir";

        namedParameterJdbcTemplate.query(query, this::rowMapPop);
    }

    private ServiceSocket rowMap(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found WebStorage: " + rs.getString("name"));

        Storage storage = new Storage();
        storage.setSwitchedOn(true);
        storage.setName(rs.getString("name"));
        storage.setCapacity(8.589934592E12);
        storage.setCapacityUsed(1.314914304E9);
        storage.setMountPoint(rs.getString("home_base"));

        storageRepository.save(storage);

        if (!rs.getString("name").equals("baton")) {
            storage = new Storage();
            storage.setSwitchedOn(true);
            storage.setName(rs.getString("name"));
            storage.setCapacity(8.589934592E12);
            storage.setCapacityUsed(1.314914304E9);
            storage.setMountPoint("/mysql");

            storageRepository.save(storage);
        }

        if (rs.getString("name").equals("web35")) {
            storage = new Storage();
            storage.setSwitchedOn(true);
            storage.setName("web36");
            storage.setCapacity(8.589934592E12);
            storage.setCapacityUsed(1.314914304E9);
            storage.setMountPoint(rs.getString("home_base"));

            storageRepository.save(storage);

            storage = new Storage();
            storage.setSwitchedOn(true);
            storage.setName("web36");
            storage.setCapacity(8.589934592E12);
            storage.setCapacityUsed(1.314914304E9);
            storage.setMountPoint("/mysql");

            storageRepository.save(storage);
        }

        return null;
    }

    private ServiceSocket rowMapPop(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found PopStorage: " + rs.getString("name"));

        Storage storage = new Storage();
        storage.setSwitchedOn(true);
        String[] name = rs.getString("name").split("\\.");
        storage.setName(name[0]);
        storage.setCapacity(8.589934592E12);
        storage.setCapacityUsed(1.314914304E9);
        storage.setMountPoint(rs.getString("homedir"));

        storageRepository.save(storage);

        return null;
    }

    private void seedDBStorage() {
        Storage storage;

        storage = new Storage();
        storage.setSwitchedOn(true);
        storage.setName("mdb4");
        storage.setMountPoint("/mysql");
        storage.setCapacity(8.589934592E12);
        storage.setCapacityUsed(1.314914304E9);

        storageRepository.save(storage);
    }

    private void seedTestData() {
        Storage storage;

        storage = new Storage();
        storage.setSwitchedOn(true);
        storage.setName("web99");
        storage.setMountPoint("/home");
        storage.setCapacity(5.0E9);
        storage.setCapacityUsed(3.0E9);

        storageRepository.save(storage);

        storage = new Storage();
        storage.setSwitchedOn(true);
        storage.setName("pop99");
        storage.setMountPoint("/home");
        storage.setCapacity(8.589934592E12);
        storage.setCapacityUsed(1.314914304E9);

        storageRepository.save(storage);
    }

    public boolean importToMongo() {
        storageRepository.deleteAll();
        pull();
        return true;
    }
}
