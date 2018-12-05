package com.evolveum.midpoint.midcredible.framework;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class JdbcButler extends ResourceButler<JdbcTemplate> {

	public JdbcButler(String id, Context context) {
		super(id, context);
	}

	public JdbcButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
	}

	public JdbcButler(String id, Context context, String resourceOid) {
		super(id, context, resourceOid);
	}
}
