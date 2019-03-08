package com.evolveum.midpoint.midcredible.framework.comparator.ldap;

import com.evolveum.midpoint.midcredible.framework.cmd.CompareLdapOptions;
import com.evolveum.midpoint.midcredible.framework.cmd.CsvPrinterOptions;
import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import com.evolveum.midpoint.midcredible.framework.util.GroovyUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.h2.Driver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapDbComparator {

    private static final Logger LOG = LoggerFactory.getLogger(LdapDbComparator.class);

    private static final String DEFAULT_DB_PATH = "./data";

    private static final String DEFAULT_CSV_EQUAL = "true";

    private static final String OLD_TABLE_NAME = "old_data";

    private static final String NEW_TABLE_NAME = "new_data";

    private static final long LDAP_CONNECTION_TIMEOUT = 20000L;

    static final String DN_ATTRIBUTE = "dn";

    private enum LDAP_DATASET {
        OLD, NEW
    }

    private CompareLdapOptions options;

    private ExecutorService executor;

    private LdapComparator comparator;

    public LdapDbComparator(CompareLdapOptions options) {
        this.options = options;
    }

    public void execute() {
        int workerCount = options.getWorkers();

        int poolSize = workerCount + 2;
        executor = Executors.newFixedThreadPool(poolSize);

        LOG.info("Initializing LDAP connections");

        try (HikariDataSource ds = createDataSource(poolSize);
             CsvReportPrinter printer = new CsvReportPrinter();
             LdapConnection oldCon = setupConnection(LDAP_DATASET.OLD);
             LdapConnection newCon = setupConnection(LDAP_DATASET.NEW)) {

            LOG.info("Compiling comparator groovy script");
            comparator = GroovyUtils.createTypeInstance(LdapComparator.class, options.getCompareScriptPath().getPath());

            LOG.info("Setting up csv printer");
            CsvPrinterOptions csvOpts = options.getCsvPrinterOptions();
            printer.setOutPath(csvOpts.getPath().getPath());
            printer.setPrintEqual(csvOpts.isPrintEqual());
            printer.init();

            JdbcTemplate jdbc = new JdbcTemplate(ds);

            LOG.info("Setting up database");
            setupH2(jdbc);

            Set<String> columns = ConcurrentHashMap.newKeySet();
            columns.add(DN_ATTRIBUTE);

            // todo how to handle cancelation?
            // fill in DB table
            LdapImportWorker importOldWorker = new LdapImportWorker(workerCount, jdbc, OLD_TABLE_NAME, oldCon, comparator, columns);
            LdapImportWorker importNewWorker = new LdapImportWorker(workerCount, jdbc, NEW_TABLE_NAME, newCon, comparator, columns);

            LOG.info("Starting import from LDAP");
            Future importOldFuture = executor.submit(importOldWorker);
            Future importNewFuture = executor.submit(importNewWorker);

            // wait for import workers to finish
            importOldFuture.get();
            importNewFuture.get();

            LOG.info("Found {} attributes in total", columns.size());
            Map<String, Column> columnMap = buildColumnMap(columns);

            LOG.info("Starting compare process");
            List<LdapComparatorWorker> compareWorkers = new ArrayList<>();
            List<Future> comparatorFutures = new ArrayList<>();
            for (int i = 0; i < workerCount; i++) {
                LdapComparatorWorker worker = new LdapComparatorWorker(i, ds, printer, comparator, columnMap);
                compareWorkers.add(worker);

                comparatorFutures.add(executor.submit(worker));
            }

            // wait for comparator workers to finish
            for (Future f : comparatorFutures) {
                f.get();
            }

            LOG.info("Done");
        } catch (Exception ex) {
            throw new LdapComparatorException("Ldap comparator failed, reason: " + ex.getMessage(), ex);
        } finally {
            executor.shutdown();

            cleanupH2();
        }
    }

    private void setupH2(JdbcTemplate jdbc) {
        jdbc.execute(buildCreateTable(OLD_TABLE_NAME));
        jdbc.execute(buildCreateTable(NEW_TABLE_NAME));
    }

    private String getDBPath() {
        return options.getDbPath().getPath();
    }

    private HikariDataSource createDataSource(int maxPoolSize) throws SQLException {
        deleteDbFile();

        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(maxPoolSize);
        config.setJdbcUrl("jdbc:h2:file:" + getDBPath() + ";LOG=0;CACHE_SIZE=65536;LOCK_MODE=0;UNDO_LOG=0");
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
        File data = new File(getDBPath() + ".mv.db");
        if (data.exists()) {
            data.delete();
        }

        data = new File(getDBPath() + ".trace.db");
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
                host = options.getOldHost();
                port = options.getOldPort();
                secured = options.isOldSecured();
                username = options.getOldUsername();
                password = options.getOldPasswordOrAskPassword();
                break;
            case NEW:
                host = options.getNewHost();
                port = options.getNewPort();
                secured = options.isNewSecured();
                username = options.getNewUsername();
                password = options.getNewPasswordOrAskPassword();
                break;
        }

        LOG.info("Creating LDAP connection for {} dataset", dataset);
        LdapConnection con = new LdapNetworkConnection(host, port, secured);
        con.setTimeOut(LDAP_CONNECTION_TIMEOUT);
        con.bind(username, password);

        return con;
    }

    private Map<String, Column> buildColumnMap(Set<String> columns) {
        List<String> names = new ArrayList<>();
        names.addAll(columns);

        Collections.sort(names);

        Map<String, Column> result = new HashMap<>();
        for (int i = 0; i < names.size(); i++) {
            Column column = new Column(names.get(i), i);
            result.put(column.getName(), column);
        }

        return result;
    }
}
