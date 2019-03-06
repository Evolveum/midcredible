package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapDbComparator {

    public static final String PROP_CSV_PATH = "comparator.ldap.csv.path";

    public static final String PROP_OLD_HOST = "comparator.ldap.old.host";

    public static final String PROP_OLD_PORT = "comparator.ldap.old.port";

    public static final String PROP_OLD_SECURED = "comparator.ldap.old.secured";

    public static final String PROP_OLD_USERNAME = "comparator.ldap.old.username";

    public static final String PROP_OLD_PASSWORD = "comparator.ldap.old.password";

    public static final String PROP_NEW_HOST = "comparator.ldap.old.host";

    public static final String PROP_NEW_PORT = "comparator.ldap.old.port";

    public static final String PROP_NEW_SECURED = "comparator.ldap.old.secured";

    public static final String PROP_NEW_USERNAME = "comparator.ldap.old.username";

    public static final String PROP_NEW_PASSWORD = "comparator.ldap.old.password";

    public static final String PROP_COMPARATOR_SCRIPT = "comparator.ldap.script";

    public static final String PROP_WORKER_COUNT = "comparator.ldap.workers";

    private static final Logger LOG = LoggerFactory.getLogger(LdapDbComparator.class);

    private static final String DB_PATH = "./data";

    private static final String OLD_TABLE_NAME = "old_data";

    private static final String NEW_TABLE_NAME = "new_data";

    private enum LDAP_DATASET {
        OLD, NEW
    }

    private Properties properties;

    private ExecutorService executor;

    private LdapComparator comparator;

    public LdapDbComparator(Properties properties) {
        this.properties = properties;
    }

    public void execute() {
        int workerCount = Integer.parseInt(properties.getProperty(PROP_WORKER_COUNT, "1"));

        int threadPoolSize = workerCount > 1 ? workerCount : 2;
        executor = Executors.newFixedThreadPool(threadPoolSize);

        LOG.info("Initializing LDAP connections");

        try (HikariDataSource ds = createDataSource(workerCount);
             CsvReportPrinter printer = new CsvReportPrinter();
             LdapConnection oldCon = setupConnection(LDAP_DATASET.OLD);
             LdapConnection newCon = setupConnection(LDAP_DATASET.NEW)) {

            LOG.info("Compiling comparator groovy script");
            comparator = setupComparator();

            LOG.info("Setting up csv printer");
            printer.setupCsvPrinter(properties.getProperty(PROP_CSV_PATH));

            JdbcTemplate jdbc = new JdbcTemplate(ds);

            LOG.info("Setting up database");
            setupH2(jdbc);

            // fill in DB table
            LdapImportWorker importOldWorker = new LdapImportWorker(workerCount, jdbc, OLD_TABLE_NAME, oldCon, comparator);
            LdapImportWorker importNewWorker = new LdapImportWorker(workerCount, jdbc, NEW_TABLE_NAME, newCon, comparator);

            LOG.info("Starting import from LDAP");
            Future importOldFuture = executor.submit(importOldWorker);
            Future importNewFuture = executor.submit(importNewWorker);

            // wait for import workers to finish
            importOldFuture.get();
            importNewFuture.get();

            LOG.info("Starting compare process");
            List<LdapComparatorWorker> compareWorkers = new ArrayList<>();
            List<Future> comparatorFutures = new ArrayList<>();
            for (int i = 0; i < workerCount; i++) {
                LdapComparatorWorker worker = new LdapComparatorWorker(i, ds, printer, comparator);
                compareWorkers.add(worker);

                comparatorFutures.add(executor.submit(worker));
            }

            // wait for comparator workers to finish
            for (Future f : comparatorFutures) {
                f.get();
            }

            LOG.info("Done");
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle
        } finally {
            executor.shutdown();

            cleanupH2();
        }
    }

    private void setupH2(JdbcTemplate jdbc) {
        jdbc.execute(buildCreateTable(OLD_TABLE_NAME));
        jdbc.execute(buildCreateTable(NEW_TABLE_NAME));
    }

    private HikariDataSource createDataSource(int maxPoolSize) throws SQLException {
        deleteDbFile();

        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(maxPoolSize);
        config.setJdbcUrl("jdbc:h2:file:" + DB_PATH);
        config.setUsername("sa");
        config.setPassword("");
        config.setDriverClassName(Driver.class.getName());
        config.setTransactionIsolation("TRANSACTION_READ_UNCOMMITTED");

        return new HikariDataSource(config);
    }

    private String buildCreateTable(String tableName) {
        StringBuilder sb = new StringBuilder();
        sb.append("create table ");
        sb.append(tableName);
        sb.append("(");
        sb.append("dn varchar(1000),");
        sb.append("worker tinyint not null,");
        sb.append("entry varchar not null,");
        sb.append("primary key (dn)");
        sb.append(")");

        return sb.toString();
    }

    private void cleanupH2() {
        deleteDbFile();
    }

    private void deleteDbFile() {
        File data = new File(DB_PATH + ".mv.db");
        if (data.exists()) {
            data.delete();
        }

        data = new File(DB_PATH + ".trace.db");
        if (data.exists()) {
            data.delete();
        }
    }

    private LdapConnection setupConnection(LDAP_DATASET dataset)
            throws LdapException {

        String host = null;
        Integer port = null;
        Boolean secured = null;
        String username = null;
        String password = null;

        switch (dataset) {
            case OLD:
                host = properties.getProperty(PROP_OLD_HOST, "localhost");
                port = Integer.parseInt(properties.getProperty(PROP_OLD_PORT, "389"));
                secured = Boolean.parseBoolean(properties.getProperty(PROP_OLD_SECURED, "false"));
                username = properties.getProperty(PROP_OLD_USERNAME);
                password = properties.getProperty(PROP_OLD_PASSWORD);
                break;
            case NEW:
                host = properties.getProperty(PROP_NEW_HOST, "localhost");
                port = Integer.parseInt(properties.getProperty(PROP_NEW_PORT, "389"));
                secured = Boolean.parseBoolean(properties.getProperty(PROP_NEW_SECURED, "false"));
                username = properties.getProperty(PROP_NEW_USERNAME);
                password = properties.getProperty(PROP_NEW_PASSWORD);
                break;
        }

        LdapConnection con = new LdapNetworkConnection(host, port, secured);
        con.bind(username, password);

        return con;
    }

    private LdapComparator setupComparator()
            throws IOException, ScriptException, IllegalAccessException, InstantiationException {

        File file = new File(properties.getProperty(PROP_COMPARATOR_SCRIPT));
        String script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("groovy");

        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) engine;
        gse.compile(script);

        Class<? extends LdapComparator> type = null;

        GroovyClassLoader gcl = gse.getClassLoader();
        for (Class c : gcl.getLoadedClasses()) {
            if (LdapComparator.class.isAssignableFrom(c)) {
                type = c;
                break;
            }
        }

        if (type == null) {
            throw new IllegalStateException("Couldn't find comparator class that is assignable from LdapComparator "
                    + ", available classes: " + Arrays.toString(gcl.getLoadedClasses()));
        }

        return type.newInstance();
    }

}
