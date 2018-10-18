package ru.majordomo.hms.rc.staff.config;

import com.zaxxer.hikari.HikariDataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
@Import(value = {
        DataSourceAutoConfiguration.class,
        DataSourceTransactionManagerAutoConfiguration.class
})
@Profile("import")
public class DatabaseConfig {
    @Bean(name = "billingJdbcTemplate")
    @Primary
    @Autowired
    public JdbcTemplate billingJdbcTemplate(@Qualifier("billingDataSource") DataSource billingDataSource) {
        return new JdbcTemplate(billingDataSource);
    }

    @Bean(name = "billingNamedParameterJdbcTemplate")
    @Primary
    @Autowired
    public NamedParameterJdbcTemplate billingNamedParameterJdbcTemplate(@Qualifier("billingDataSource") DataSource billingDataSource) {
        return new NamedParameterJdbcTemplate(billingDataSource);
    }

    @Bean(name = "billingDataSourceProperties")
    @ConfigurationProperties("datasource.billing")
    @Primary
    public DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean(name = "billingDataSource")
    @Primary
    public HikariDataSource dataSource(@Qualifier("billingDataSourceProperties") DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().type(HikariDataSource.class).build();
    }
}
