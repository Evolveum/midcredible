package com.evolveum.midpoint.dubious.framework;

import com.evolveum.midpoint.test.ldap.OpenDJController;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.apache.commons.lang3.StringUtils;
import org.apache.directory.ldap.client.api.LdapConnectionConfig;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.io.IOException;
import java.net.URISyntaxException;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapButler extends ResourceButler<LdapNetworkConnection> {

	private static OpenDJController openDJController = new OpenDJController();

	private String baseContext;

	public LdapButler(String id, Context context, String resourceOid) {
		super(id, context, resourceOid);
	}

	public LdapButler(String id, Context context, LdapNetworkConnection client) {
		super(id, context, client);
	}

	@Override
	protected LdapNetworkConnection init() throws Exception {
		ConfigurationPropertiesType config = getConfigurationProperties();

		String host = getValue(config, "host");
		String port = getValue(config, "port");
		String bindDn = getValue(config, "bindDn");
		String bindPassword = getValue(config, "bindPassword");
		String timeout = getValue(config, "connectTimeout");
		if (StringUtils.isEmpty(timeout)) {
			timeout = "10000";
		}

		baseContext = getValue(config, "baseContext");

		String connectionSecurity = getValue(config, "connectionSecurity");

		LdapConnectionConfig lcc = new LdapConnectionConfig();
		lcc.setLdapHost(host);
		lcc.setLdapPort(Integer.parseInt(port));
		lcc.setTimeout(Integer.parseInt(timeout));

		if (connectionSecurity == null || "none".equals(connectionSecurity)) {
			// Nothing to do
		} else if ("ssl".equals(connectionSecurity)) {
			lcc.setUseSsl(true);
		} else if ("starttls".equals(connectionSecurity)) {
			lcc.setUseTls(true);
		}

		LdapNetworkConnection con = new LdapNetworkConnection(lcc);
		con.connect();

		try {
			con.bind(bindDn, bindPassword);
		} catch (Exception ex) {
			con.close();
			throw ex;
		}

		return con;
	}

	public String getBaseContext() {
		return baseContext;
	}

	public void setBaseContext(String baseContext) {
		this.baseContext = baseContext;
	}

	public static OpenDJController getOpenDJController() {
		return openDJController;
	}

	public void cleanStart() throws IOException, URISyntaxException {
		openDJController.startCleanServer();
	}

	/**
	 * Starts embedded instance
	 */
	public void start() {
		openDJController.start();
	}

	/**
	 * Stops embedded instance
	 */
	public void stop() {
		openDJController.stop();
	}
}
