package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.h2.Driver;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparator2 {

    private ExecutorService executor;

    private FreakinComparator comparator = new FreakinComparatorImpl(); // todo as parameter

    public static void main(String[] args) {
        new LdapComparator2().execute();
    }

    public void execute() {
        int workerCount = 1;

        int threadPoolSize = workerCount > 1 ? workerCount : 2;
        executor = Executors.newFixedThreadPool(threadPoolSize);

        try (HikariDataSource ds = createDataSource(workerCount);
             CsvReportPrinter printer = new CsvReportPrinter();           // todo fix csv path
             LdapConnection oldCon = setupConnection("localhost", 1389, false, "cn=admin,dc=example,dc=com", "admin");
             LdapConnection newCon = setupConnection("localhost", 2389, false, "cn=admin,dc=example,dc=com", "admin")) {

            printer.setupCsvPrinter("./target/out.csv");

            JdbcTemplate jdbc = new JdbcTemplate(ds);

            setupH2(jdbc);

            // fill in DB table
            LdapImportWorker importOldWorker = new LdapImportWorker(workerCount, jdbc, "old_data", oldCon, comparator);
            LdapImportWorker importNewWorker = new LdapImportWorker(workerCount, jdbc, "new_data", newCon, comparator);

            Future importOldFuture = executor.submit(importOldWorker);
            Future importNewFuture = executor.submit(importNewWorker);

            // wait for import workers to finish
            importOldFuture.get();
            importNewFuture.get();

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
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle
        } finally {
            executor.shutdown();

            cleanupH2();
        }
    }

    private void setupH2(JdbcTemplate jdbc) {
        jdbc.execute(buildCreateTable("old_data"));
        jdbc.execute(buildCreateTable("new_data"));
    }

    private HikariDataSource createDataSource(int maxPoolSize) throws SQLException {
        deleteDbFile();

        HikariConfig config = new HikariConfig();
        config.setMinimumIdle(1);
        config.setMaximumPoolSize(maxPoolSize);
        config.setJdbcUrl("jdbc:h2:file:./data");
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

    private File deleteDbFile() {
        File data = new File("./data.mv.db");
        if (data.exists()) {
            data.delete();
        }

        return data;
    }

    private LdapConnection setupConnection(String host, int port, boolean secured, String username, String password)
            throws LdapException {

        LdapConnection con = new LdapNetworkConnection(host, port, secured);
        con.bind(username, password);

        return con;
    }
}
