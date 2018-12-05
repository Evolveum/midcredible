package com.evolveum.midpoint.dubious.framework;

import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends JdbcButler {

	private String table;

	public TableButler(String id, Context context, String resourceOid) {
		super(id, context, resourceOid);
	}

	public TableButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
	}

	@Override
	public JdbcTemplate init() throws Exception {
		ConfigurationPropertiesType configuration = getConfigurationProperties();

		String driver = getValue(configuration, "jdbcDriver");
		String url = getValue(configuration, "jdbcUrlTemplate");
		String host = getValue(configuration, "host");
		String port = getValue(configuration, "port");
		String database = getValue(configuration, "database");

		String username = getValue(configuration, "user");
		String password = getValue(configuration, "password");

		table = getValue(configuration, "table");

		String jdbcUrl = formatUrlTemplate(url, host, port, database);
		SingleConnectionDataSource ds = new SingleConnectionDataSource(jdbcUrl, username, password, true);
		ds.setDriverClassName(driver);

		return new JdbcTemplate(ds);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
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

	private long count(String where, Object[] params) {
		StringBuilder sb = new StringBuilder();
		sb.append("select count(*) from ").append(table);

		if (StringUtils.isNotEmpty(where)) {
			sb.append(where);
		}

		JdbcTemplate template = getClient();
		if (params != null) {
			return template.queryForObject(sb.toString(), params, Long.class);
		}

		return template.queryForObject(sb.toString(), Long.class);
	}
}
