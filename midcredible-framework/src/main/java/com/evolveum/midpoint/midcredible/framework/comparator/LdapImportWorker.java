package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapImportWorker implements Runnable {

    private static final int JDBC_BATCH_SIZE = 200;

    private int workerCount;

    private JdbcTemplate jdbc;
    private String table;

    private LdapConnection ldapConnection;

    private boolean canceled;

    public LdapImportWorker(int workerCount, JdbcTemplate jdbc, String table, LdapConnection ldapConnection) {
        this.workerCount = workerCount;
        this.jdbc = jdbc;
        this.table = table;
        this.ldapConnection = ldapConnection;
    }

    public void cancel() {
        canceled = true;
    }

    @Override
    public void run() {
        try {
            List<Object[]> rows = new ArrayList<>();

            // handle in separate thread
            SearchRequest req = buildSearchRequest(); // todo handle pagedResults or vlv using while cycle
            SearchCursor cur = ldapConnection.search(req);
            cur.first();
            while (cur.available()) {
                if (canceled) {
                    break;
                }

                Entry entry = cur.getEntry();
                String dn = entry.getDn().getNormName().toLowerCase();
                int worker = dn.hashCode() % workerCount;

                LdifEntry e = new LdifEntry(entry);
                String ldif = e.toString();

                rows.add(new Object[]{dn, worker, ldif});

                if (rows.size() >= JDBC_BATCH_SIZE) {
                    jdbc.batchUpdate("insert into " + table + " (dn, worker, entry) values (?,?,?)", rows);
                    rows.clear();
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle exception
        }
    }

    // todo move to comparator interface
    private SearchRequest buildSearchRequest() throws LdapException {
        SearchRequest req = new SearchRequestImpl();
        req.setScope(SearchScope.SUBTREE);
        req.addAttributes("*");
        req.setTimeLimit(0);
        req.setBase(new Dn("ou=system"));
        req.setFilter("*");


        return req;
    }
}
