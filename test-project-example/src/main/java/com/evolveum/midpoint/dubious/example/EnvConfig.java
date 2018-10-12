package com.evolveum.midpoint.dubious.example;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

/**
 * Created by Viliam Repan (lazyman).
 */
@Configuration
public class EnvConfig {

    @Value("${fakedb.url}")
    private String fakedbUrl;
    @Value("${fakedb.username}")
    private String fakedbUsername;
    @Value("${fakedb.password}")
    private String fakedbPassword;

    @Bean
    public DataSource tableDatasource() {
        SingleConnectionDataSource ds = new SingleConnectionDataSource(fakedbUrl, fakedbUsername, fakedbPassword, true);
        ds.setDriverClassName("org.postgresql.Driver");
        return ds;
    }

    @Bean
    public JdbcTemplate tableResource() {
        JdbcTemplate template = new JdbcTemplate(tableDatasource());

        return template;
    }
}
