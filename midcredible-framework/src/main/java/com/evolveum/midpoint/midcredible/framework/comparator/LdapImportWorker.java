package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapImportWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LdapImportWorker.class);

    private static final int JDBC_BATCH_SIZE = 200;

    private int workerCount;

    private JdbcTemplate jdbc;
    private String table;

    private LdapConnection ldapConnection;

    private LdapComparator comparator;

    private Set<String> columns;

    private boolean canceled;

    public LdapImportWorker(int workerCount, JdbcTemplate jdbc, String table, LdapConnection ldapConnection,
                            LdapComparator comparator, Set<String> columns) {
        this.workerCount = workerCount;
        this.jdbc = jdbc;
        this.table = table;
        this.ldapConnection = ldapConnection;
        this.comparator = comparator;
        this.columns = columns;
    }

    public void cancel() {
        canceled = true;
    }

    @Override
    public void run() {
        long lastPrintoutTime = 0;
        int count = 0;
        try {
            List<Object[]> rows = new ArrayList<>();

            // handle in separate thread
            SearchRequest req = comparator.buildSearchRequest();
            SearchCursor cur = ldapConnection.search(req);
            while (cur.next()) {
                if (canceled) {
                    break;
                }

                Entry entry = cur.getEntry();
                String dn = entry.getDn().getNormName().toLowerCase();
                int worker = dn.hashCode() % workerCount;

                LdifEntry e = new LdifEntry(entry);
                String ldif = e.toString();

                processColumnMap(entry.getAttributes());

                rows.add(new Object[]{dn, worker, ldif});

                if (rows.size() >= JDBC_BATCH_SIZE) {
                    jdbc.batchUpdate("insert into " + table + " (dn, worker, entry) values (?,?,?)", rows);
                    rows.clear();
                }

                count++;

                if (lastPrintoutTime + LdapDbComparator.PRINTOUT_TIME_FREQUENCY < System.currentTimeMillis()) {
                    printStatus(count);
                    lastPrintoutTime = System.currentTimeMillis();
                }
            }

            if (!rows.isEmpty()) {
                jdbc.batchUpdate("insert into " + table + " (dn, worker, entry) values (?,?,?)", rows);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle exception
        } finally {
            printStatus(count);
        }
    }

    private void processColumnMap(Collection<Attribute> attributes) {
        attributes.forEach(a -> columns.add(a.getId()));
    }

    private void printStatus(int count) {
        LOG.info("Imported {} entries to {}", count, table);
    }
}
