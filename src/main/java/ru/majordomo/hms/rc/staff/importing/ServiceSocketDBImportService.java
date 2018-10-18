package ru.majordomo.hms.rc.staff.importing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
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
@Profile("import")
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
                "WHERE s.name = 'baton' OR s.name = 'staff' OR s.name = 'chucho' OR s.name LIKE 'web%'" +
                "GROUP BY a.server_id";

        namedParameterJdbcTemplate.query(query, this::rowMap);

        query = "SELECT ds.id, ds.name, ds.out_ip, s.id as server_id " +
                "FROM db_servers ds " +
                "LEFT JOIN servers s ON ds.name=s.name " +
                "WHERE ds.name LIKE 'mdb4' OR ds.name LIKE 'web%'";

        namedParameterJdbcTemplate.query(query, this::rowMapMysql);

        seedStaffData();
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

        logger.debug(serviceSocket.toString());

        serviceSocket = new ServiceSocket();
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("nginx-https@" + rs.getString("name"));
        serviceSocket.setAddress(rs.getString("proxy"));
        serviceSocket.setPort(443);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        logger.debug(serviceSocket.toString());

        String query = "SELECT nc.id, nc.server, nc.listen, nc.redir_to, nc.flag " +
                "FROM nginx_conf nc " +
                "WHERE nc.server = :server AND nc.flag != 'otd_ip'";
        SqlParameterSource namedParameters1 = new MapSqlParameterSource("server", rs.getString("name") + ".majordomo.ru");

        namedParameterJdbcTemplate.query(query,
                namedParameters1,
                (rs1 -> {
                    ServiceSocket apacheServiceSocket = new ServiceSocket();
                    apacheServiceSocket.setSwitchedOn(true);
                    String flag = rs1.getString("flag");
                    flag = flag.matches("(^php5-).*$") ? flag.replace("php5-", "php52-") : flag;
                    flag = flag.matches("(^php5$)") ? flag.replace("php5", "php52") : flag;
//                    flag = flag.matches("^php5$|^php5-.*") ? flag.replace("5", "52") : flag;


                    String defaultInName = flag.matches(".*-.*|.*perl.*") ? "" : "-default";
                    apacheServiceSocket.setName("apache2-" + flag + defaultInName + "-http@" + rs.getString("name"));

                    String[] redirTo = rs1.getString("redir_to").split(":");
                    apacheServiceSocket.setAddress(redirTo[0]);
                    apacheServiceSocket.setPort(Integer.valueOf(redirTo[1]));

                    governorOfServiceSocket.isValid(apacheServiceSocket);
                    governorOfServiceSocket.save(apacheServiceSocket);

                    logger.debug(apacheServiceSocket.toString());
                })
        );

        if (rs.getString("name").equals("web35")) {
            serviceSocket = new ServiceSocket();
            serviceSocket.setSwitchedOn(true);
            serviceSocket.setName("nginx-http@" + "web36");
            serviceSocket.setAddress(rs.getString("proxy"));
            serviceSocket.setPort(80);

            governorOfServiceSocket.isValid(serviceSocket);
            governorOfServiceSocket.save(serviceSocket);

            logger.debug(serviceSocket.toString());

            serviceSocket = new ServiceSocket();
            serviceSocket.setSwitchedOn(true);
            serviceSocket.setName("nginx-https@" + "web36");
            serviceSocket.setAddress(rs.getString("proxy"));
            serviceSocket.setPort(443);

            governorOfServiceSocket.isValid(serviceSocket);
            governorOfServiceSocket.save(serviceSocket);

            logger.debug(serviceSocket.toString());

            query = "SELECT nc.id, nc.server, nc.listen, nc.redir_to, nc.flag " +
                    "FROM nginx_conf nc " +
                    "WHERE nc.server = :server AND nc.flag != 'otd_ip'";
            namedParameters1 = new MapSqlParameterSource("server", rs.getString("name") + ".majordomo.ru");

            namedParameterJdbcTemplate.query(query,
                    namedParameters1,
                    (rs1 -> {
                        ServiceSocket apacheServiceSocket = new ServiceSocket();
                        apacheServiceSocket.setSwitchedOn(true);
                        String flag = rs1.getString("flag");
                        flag = flag.matches("(^php5-).*$") ? flag.replace("php5-", "php52-") : flag;
                        flag = flag.matches("(^php5$)") ? flag.replace("php5", "php52") : flag;

//                        flag = flag.replaceAll("(^php5$)", "php52");
//                        flag = flag.matches("^php5$|^php5-.*") ? flag.replace("5", "52") : flag;
                        String defaultInName = flag.matches(".*-.*|.*perl.*") ? "" : "-default";
                        apacheServiceSocket.setName("apache2-" + flag + defaultInName + "-http@" + "web36");

                        String[] redirTo = rs1.getString("redir_to").split(":");
                        apacheServiceSocket.setAddress(redirTo[0]);
                        apacheServiceSocket.setPort(Integer.valueOf(redirTo[1]));

                        governorOfServiceSocket.isValid(apacheServiceSocket);
                        governorOfServiceSocket.save(apacheServiceSocket);

                        logger.debug(apacheServiceSocket.toString());
                    })
            );
        }

        return null;
    }

    private ServiceSocket rowMapMysql(ResultSet rs, int rowNum ) throws SQLException {
        logger.debug("Found DBserver: " + rs.getString("name"));

        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket.setId(rs.getString("server_id") != null ? rs.getString("server_id") + "_mysql_socket" : rs.getString("id") + "_mysql_socket");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("mysql-mysql@" + rs.getString("name"));
        serviceSocket.setAddress(rs.getString("out_ip"));
        serviceSocket.setPort(3306);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        logger.debug(serviceSocket.toString());

        if (rs.getString("name").equals("web35")) {
            serviceSocket = new ServiceSocket();
            serviceSocket.setSwitchedOn(true);
            serviceSocket.setName("mysql-mysql@" + "web36");
            serviceSocket.setAddress(rs.getString("out_ip"));
            serviceSocket.setPort(3306);

            governorOfServiceSocket.isValid(serviceSocket);
            governorOfServiceSocket.save(serviceSocket);

            logger.debug(serviceSocket.toString());
        }

        return null;
    }

    private void seedStaffData() {
        ServiceSocket serviceSocket = new ServiceSocket();
        serviceSocket.setId("124_mysql_socket");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("mysql-mysql@staff");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(3306);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);

        serviceSocket = new ServiceSocket();
        serviceSocket.setId("5_mysql_socket");
        serviceSocket.setSwitchedOn(true);
        serviceSocket.setName("mysql-mysql@chucho");
        serviceSocket.setAddress("127.0.0.1");
        serviceSocket.setPort(3306);

        governorOfServiceSocket.isValid(serviceSocket);
        governorOfServiceSocket.save(serviceSocket);
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
