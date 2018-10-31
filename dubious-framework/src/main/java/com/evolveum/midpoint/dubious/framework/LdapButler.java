package com.evolveum.midpoint.dubious.framework;

import org.apache.directory.ldap.client.api.LdapNetworkConnection;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapButler extends ResourceButler<LdapNetworkConnection> {

	public LdapButler(String id, Context context) {
		super(id, context);
	}

	public LdapButler(String id, Context context, LdapNetworkConnection client) {
		super(id, context, client);
	}

	@Override
	protected LdapNetworkConnection init() throws Exception {
		// todo implement
		return super.init();
	}

	/**
	 * Starts embedded instance
	 */
	public void start() {
		// todo implement
	}

	/**
	 * Stops embedded instance
	 */
	public void stop() {
		// todo implement
	}
}
