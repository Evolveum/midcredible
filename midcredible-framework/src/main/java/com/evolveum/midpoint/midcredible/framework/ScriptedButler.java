package com.evolveum.midpoint.midcredible.framework;

import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ScriptedButler extends JdbcButler {

	public ScriptedButler(String id, Context context) {
		super(id, context);
	}

	public ScriptedButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
	}

	@Override
	protected JdbcTemplate init() throws Exception {
		// todo implement
		return super.init();
	}
}
