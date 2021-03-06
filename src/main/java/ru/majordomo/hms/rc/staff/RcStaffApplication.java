package ru.majordomo.hms.rc.staff;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

@EnableEurekaClient
@SpringBootApplication(exclude = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@EnableConfigurationProperties
public class RcStaffApplication implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(RcStaffApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(RcStaffApplication.class, args);
    }

    public void run(String... args) {
        String dbSeedOption = "--db_seed";
        String processOption = "--process";
        StringBuilder sb = new StringBuilder();
        for (String option : args) {
            sb.append(" ").append(option);

            if (option.equals(dbSeedOption)) {
                boolean seeded;

//                seeded = dbImportService.seedDB();
//                sb.append(" ").append(seeded ? "dbImportService db_seeded" : "dbImportService db_not_seeded");
            } else if (option.equals(processOption)) {
                //Do some shit
            }
        }
        sb = sb.length() == 0 ? sb.append("No Options Specified") : sb;
        logger.info(String.format("Launched application with following options: %s", sb.toString()));
    }
}
