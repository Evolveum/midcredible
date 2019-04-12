package com.evolveum.midpoint.midcredible.comparator.table;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.sql.DataSource;
import java.security.InvalidParameterException;
import java.text.MessageFormat;

public class JdbcUtil {

    public JdbcUtil() {

    }
    private static final Logger LOG = LoggerFactory.getLogger(JdbcUtil.class);

    public DataSource setupDataSource(String url, String username, String password, String driver) {
        checkParameters(url, username, password, driver);
        SingleConnectionDataSource ds = new SingleConnectionDataSource(url, username, password, true);
        ds.setDriverClassName(driver);

        return ds;
    }

    private void checkParameters(String url, String username, String password, String driver) {
        StringBuilder missingProperty = new StringBuilder();
        boolean parameterMissing = false;

        if (url != null && !url.isEmpty()) {
        } else {

            parameterMissing = true;
            missingProperty.append("url").append(" ");
        }

        if (username != null && !username.isEmpty()) {
        } else {

            parameterMissing = true;
            missingProperty.append("username").append(" ");
        }

        if (password != null && !password.isEmpty()) {
        } else {

            parameterMissing = true;
            missingProperty.append("password").append(" ");
        }

        if (driver != null && !driver.isEmpty()) {
        } else {

            parameterMissing = true;
            missingProperty.append("driver").append(" ");
        }
        if (parameterMissing) {
            LOG.error("One or more parameters are missing for connecting to the data source");
            throw new InvalidParameterException(MessageFormat.format("Parameter no supplied as property, {0}", missingProperty.toString()));
        }
    }

}
