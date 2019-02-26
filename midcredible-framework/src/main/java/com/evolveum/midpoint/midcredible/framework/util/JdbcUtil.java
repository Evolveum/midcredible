package com.evolveum.midpoint.midcredible.framework.util;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;

public class JdbcUtil {

    public JdbcUtil() {

    }
    public DataSource setupDataSource(String url, String username, String password, String driver){

        SingleConnectionDataSource ds = new SingleConnectionDataSource(url, username, password, true);
        ds.setDriverClassName(driver);

        return ds;
    }

}
