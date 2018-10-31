package com.evolveum.midpoint.dubious.framework;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapButler extends ResourceButler {

	public LdapButler(String id, Context context) {
		super(id, context);
	}

	public LdapButler(String id, Context context, Object client) {
		super(id, context, client);
	}
}
