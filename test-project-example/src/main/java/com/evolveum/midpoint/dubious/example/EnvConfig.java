package com.evolveum.midpoint.dubious.example;

import org.postgresql.Driver;
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

    static{
        Driver.isRegistered();  //todo wtf? why it's there, why postgresql driver doesn't register itself automatically
    }

    @Bean
    public DataSource tableDatasource() {
        return new SingleConnectionDataSource(fakedbUrl, fakedbUsername, fakedbPassword, true);
    }

    @Bean
    public JdbcTemplate tableResource() {
        JdbcTemplate template = new JdbcTemplate(tableDatasource());

        return template;
    }
}
