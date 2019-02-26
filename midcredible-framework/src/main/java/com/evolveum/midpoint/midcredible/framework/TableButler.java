package com.evolveum.midpoint.midcredible.framework;

import com.evolveum.midpoint.midcredible.framework.util.Comparator;
import com.evolveum.midpoint.midcredible.framework.util.State;
import com.evolveum.midpoint.midcredible.framework.util.structural.Jdbc.Column;
import com.evolveum.midpoint.midcredible.framework.util.structural.Outcome;
import com.evolveum.midpoint.midcredible.framework.util.structural.Statistics;
import com.evolveum.midpoint.xml.ns._public.connector.icf_1.connector_schema_3.ConfigurationPropertiesType;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class TableButler extends JdbcButler {

	private static final Logger LOG = LoggerFactory.getLogger(TableButler.class);
	private final String comparatorPath;
	private String table;
	private Statistics statistics;

	public TableButler(String id, Context context, String resourceOid, String comparatorPath) {
		super(id, context, resourceOid);
		this.comparatorPath = comparatorPath;
	}

	public TableButler(String id, Context context, String resourceOid) {
		super(id, context, resourceOid);
		comparatorPath = null;
	}

	public TableButler(String id, Context context, JdbcTemplate client) {
		super(id, context, client);
		comparatorPath = null;
	}

//	public Comparator compare(ResourceButler resourceButler) {
//
//
//
//		try {
//			executeComparison(setupComparator(comparatorPath)) ;
//		} catch (IOException e) {
//			e.printStackTrace();
//		} catch (ScriptException e) {
//			e.printStackTrace();
//		} catch (IllegalAccessException e) {
//			e.printStackTrace();
//		} catch (InstantiationException e) {
//			e.printStackTrace();
//		}
//		finally {
//
//			//TODO clean up
//
//		}
//		return null;
//	}

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

//	protected void executeComparison(Comparator comparator, ResourceButler analyzedResource) {
//
//		ResultSet oldRs = null;
//		ResultSet newRs = null;
//		try {
//			oldRs = createResultSet(comparator.query(), this.getClient().getDataSource());
//			newRs = createResultSet(comparator.query(), (JdbcButler) analyzedResource.getClient().getDataSource());
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
//
//try {
//	while (oldRs.next()) {
//		oldRow = createMapFromRow(oldRs);
//
//		if (!newRs.next()) {
//			// there's nothing left in new table, old table contains more rows than it should, mark old rows as "-"
//			printCsvRow(printer, "-", oldRs);
//			break;
//		}
//
//		newRow = createMapFromRow(newRs);
//		State state = comparator.compare(oldRow, newRow);
//		switch (state) {
//			case EQUAL:
//				continue;
//			case OLD_BEFORE_NEW:
//				// new table contains row that shouldn't be there, mark new as "+"
//				printCsvRow(printer, "+", newRs);
//				break;
//			case OLD_AFTER_NEW:
//				// new table misses some rows obviously, therefore old row should be marked as "-"
//				printCsvRow(printer, "-", oldRs);
//				break;
//		}
//	}
//
//	while (newRs.next()) {
//		newRow = createMapFromRow(newRs);
//		// these remaining records are not in old result set, mark new rows as "+"
//		// todo print it out somehow
//		printCsvRow(printer, "+", newRs);
//	}
//} catch (SQLException e){
//	// TODO
//	LOG.error("Sql exception white iterating trough result set " + e.getLocalizedMessage());
//}
//
//	}

	private Map<Column, Object> createMapFromRow(ResultSet rs) throws SQLException {
		ResultSetMetaData md = rs.getMetaData();
		int columns = md.getColumnCount();

		Map<Column, Object> map = new HashMap<>();
		for (int i = 0; i < columns; i++) {
			map.put(new Column(md.getColumnName(i), i), rs.getObject(i));
		}

		return map;
	}

	private Comparator setupComparator(String comparatorPath) throws IOException, ScriptException, IllegalAccessException, InstantiationException {
		File file = new File(comparatorPath);
		String script = FileUtils.readFileToString(file, "utf-8");

		ScriptEngineManager engineManager = new ScriptEngineManager();
		ScriptEngine engine = engineManager.getEngineByName("groovy");

		GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) engine;
		gse.compile(script);

		Class<? extends Comparator> type = null;

		GroovyClassLoader gcl = gse.getClassLoader();
		for (Class c : gcl.getLoadedClasses()) {
			if (Comparator.class.isAssignableFrom(c)) {
				type = c;
				break;
			}
		}

		if (type == null) {
			throw new IllegalStateException("Couldn't find comparatorPath class that is assignable from Comparator "
					+ ", available classes: " + Arrays.toString(gcl.getLoadedClasses()));
		}

		return type.newInstance();
	}

	private ResultSet createResultSet(String query, DataSource ds) throws SQLException {
		Connection con = ds.getConnection();

		PreparedStatement pstmt = con.prepareStatement(query);
		return pstmt.executeQuery();
	}

	public Statistics statistics(){

		return new Statistics(this);
	}
}
