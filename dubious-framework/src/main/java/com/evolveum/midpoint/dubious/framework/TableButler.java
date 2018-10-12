package com.evolveum.midpoint.dubious.framework;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends ResourceButler<JdbcTemplate> {

    private String resourceOid;

    public TableButler(String resourceOid) {
        this.resourceOid = resourceOid;
    }

    public TableButler(JdbcTemplate jdbcTemplate) {
        super(jdbcTemplate);
    }

    @Override
    public void init() {
        if (getClient() != null) {
            // client already initialized
            return;
        }

        if (resourceOid == null) {
            throw new IllegalStateException("Resource oid must be defined");
        }


    }
}
