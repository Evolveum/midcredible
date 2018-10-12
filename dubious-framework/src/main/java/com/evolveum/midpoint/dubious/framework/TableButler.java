package com.evolveum.midpoint.dubious.framework;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends ResourceButler {

    private String resourceOid;

    private JdbcTemplate jdbcTemplate;

    public TableButler(String resourceOid) {
        this.resourceOid = resourceOid;
    }

    public TableButler(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void init() {
        if (jdbcTemplate != null) {
            return;
        }

        if (resourceOid == null) {
            throw new IllegalStateException("Resource oid must be defined");
        }


    }

    public void destroy() {

    }
}
