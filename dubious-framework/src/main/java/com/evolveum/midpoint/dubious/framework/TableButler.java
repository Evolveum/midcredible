package com.evolveum.midpoint.dubious.framework;

import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ResourceType;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends JdbcButler {

	private String resourceOid;

	public TableButler(String id, Context context, String resourceOid) {
		super(id, context);

		this.resourceOid = resourceOid;
	}

	public TableButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
	}

	@Override
	public JdbcTemplate init() throws Exception {
		if (resourceOid == null) {
			throw new IllegalStateException("Resource oid must be defined");
		}

		Context ctx = getContext();
		RestJaxbService midpoint = ctx.getMidpoint();
		ResourceType resource = midpoint.resources().oid(resourceOid).get();

		ConfigurationPropertiesType configuration = getConfigurationProperties(resource);

		String driver = getValue(configuration, "jdbcDriver");
		String url = getValue(configuration, "jdbcUrlTemplate");
		String host = getValue(configuration, "host");
		String port = getValue(configuration, "port");
		String database = getValue(configuration, "database");

		String username = getValue(configuration, "user");
		String password = getValue(configuration, "password");

		String jdbcUrl = formatUrlTemplate(url, host, port, database);
		SingleConnectionDataSource ds = new SingleConnectionDataSource(jdbcUrl, username, password, true);
		ds.setDriverClassName(driver);

		return new JdbcTemplate(ds);
	}

	private String formatUrlTemplate(String url, String host, String port, String database) {
		StringBuilder sb = new StringBuilder();
		int len = url.length();

		for (int i = 0; i < len; ++i) {
			char ch = url.charAt(i);
			if (ch != '%') {
				sb.append(ch);
			} else if (i + 1 < len) {
				++i;
				ch = url.charAt(i);
				if (ch == '%') {
					sb.append(ch);
				} else if (ch == 'h') {
					sb.append(host);
				} else if (ch == 'p') {
					sb.append(port);
				} else if (ch == 'd') {
					sb.append(database);
				}
			}
		}

		return sb.toString();
	}

	private long count() {
		return count(null, null);
	}

	private long count(String where, Map<String, Object> params) {
		// todo implement
		return 0L;
	}
}
