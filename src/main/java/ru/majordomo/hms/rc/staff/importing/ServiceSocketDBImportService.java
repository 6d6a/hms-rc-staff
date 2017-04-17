package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;

import ru.majordomo.hms.rc.staff.managers.GovernorOfServiceSocket;
import ru.majordomo.hms.rc.staff.repositories.ServiceSocketRepository;
import ru.majordomo.hms.rc.staff.resources.ServiceSocket;

@Service
public class ServiceSocketDBImportService {
    private final static Logger logger = LoggerFactory.getLogger(ServiceSocketDBImportService.class);

    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final ServiceSocketRepository serviceSocketRepository;
    private final GovernorOfServiceSocket governorOfServiceSocket;

    @Autowired
    public ServiceSocketDBImportService(
            @Qualifier("billingNamedParameterJdbcTemplate") NamedParameterJdbcTemplate namedParameterJdbcTemplate,
            ServiceSocketRepository serviceSocketRepository,
            GovernorOfServiceSocket governorOfServiceSocket
    ) {
        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        this.serviceSocketRepository = serviceSocketRepository;
        this.governorOfServiceSocket = governorOfServiceSocket;
    }

    private void pull() {
        seedTestData();

        String query = "SELECT s.id, s.name, s.proxy " +
                "FROM servers s " +
                "JOIN account a ON a.server_id=s.id " +
                "WHERE s.name = 'baton' OR s.name LIKE 'web%'" +
                "GROUP BY a.server_id";

        namedParameterJdbcTemplate.query(query, this::rowMap);

        query = "SELECT s.id, s.name, s.out_ip " +
                "FROM db_servers s " +
                "WHERE s.name LIKE 'mdb4' OR s.name LIKE 'web%'";

        namedParameterJdbcTemplate.query(query, this::rowMapMysql);
    }

    private ServiceSocket rowMap(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found server: " + rs.getString("name"));

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("nginx-http@" + rs.getString("name"));
        serviceSocket.setAddress(rs.getString("proxy"));
        serviceSocket.setPort(80);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        serviceSocket = new ServiceSocket();
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("nginx-https@" + rs.getString("name"));
        serviceSocket.setAddress(rs.getString("proxy"));
        serviceSocket.setPort(443);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        String query = "SELECT nc.id, nc.server, nc.listen, nc.redir_to, nc.flag " +
                "FROM nginx_conf nc " +
                "WHERE nc.server = :server AND nc.flag != 'otd_ip'";
        SqlParameterSource namedParameters1 = new MapSqlParameterSource("server", rs.getString("name") + ".majordomo.ru");

        namedParameterJdbcTemplate.query(query,
                namedParameters1,
                (rs1 -> {
                    ServiceSocket apacheServiceSocket = new ServiceSocket();
                    apacheServiceSocket.setSwitchedOn(true);
                    String defaultInName = rs1.getString("flag").matches(".*-.*|.*perl.*") ? "" : "-default";
                    apacheServiceSocket.setName("apache2-" + rs1.getString("flag") + defaultInName + "-http@" + rs.getString("name"));

                    String[] redirTo = rs1.getString("redir_to").split(":");
                    apacheServiceSocket.setAddress(redirTo[0]);
                    apacheServiceSocket.setPort(Integer.valueOf(redirTo[1]));

                    governorOfServiceSocket.isValid(apacheServiceSocket);
                    governorOfServiceSocket.save(apacheServiceSocket);
                })
        );

        return null;
    }

    private ServiceSocket rowMapMysql(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found DBserver: " + rs.getString("name"));

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("mysql-mysql@" + rs.getString("name"));
        serviceSocket.setAddress(rs.getString("out_ip"));
        serviceSocket.setPort(3306);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        return null;
    }

    private void seedTestData() {
        ServiceSocket serviceSocket;

        serviceSocket = new ServiceSocket();
        serviceSocket.setId("5814a90d4cedfd113e883e65");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("nginx-http@web99");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(80);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        serviceSocket = new ServiceSocket();
        serviceSocket.setId("5814a90d4cedfd113e883e64");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("nginx-https@web99");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(443);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        serviceSocket = new ServiceSocket();
        serviceSocket.setId("5824b63c96ccde0001c82a63");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("apache2-php56-default-http@web99");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(8056);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        serviceSocket = new ServiceSocket();
        serviceSocket.setId("5835c28d96ccde0001ddca61");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("mysql-mysql@web99");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(3306);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);
    }

    public boolean importToMongo() {
        serviceSocketRepository.deleteAll();
        pull();
        return true;
    }
}
