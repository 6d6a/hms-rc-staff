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
import java.util.regex.Pattern;

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
        String query = "SELECT s.id, s.name, s.proxy " +
                "FROM servers s " +
                "JOIN account a ON a.server_id=s.id " +
                "GROUP BY a.server_id";

        namedParameterJdbcTemplate.query(query, this::rowMap);
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
                "WHERE nc.server = :server";
        SqlParameterSource namedParameters1 = new MapSqlParameterSource("server", rs.getString("name") + ".majordomo.ru");

        namedParameterJdbcTemplate.query(query,
                namedParameters1,
                (rs1 -> {
                    ServiceSocket apacheServiceSocket = new ServiceSocket();
                    apacheServiceSocket.setSwitchedOn(true);
                    String defaultInName = rs1.getString("flag").matches(".*-.*") ? "" : "-default";
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

    public boolean importToMongo() {
        serviceSocketRepository.deleteAll();
        pull();
        return true;
    }
}
