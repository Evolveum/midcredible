package com.evolveum.midpoint.midcredible.comparator.ldap;

import com.evolveum.midpoint.midcredible.comparator.common.StatusLogger;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequest;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequestImpl;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifUtils;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.controls.PagedResults;
import org.apache.directory.api.ldap.model.message.controls.PagedResultsImpl;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPOutputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapImportWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LdapImportWorker.class);

    private static final StatusLogger statusLogger = new StatusLogger();

    private static final int LDAP_PAGE_SIZE = 1000;

    private static final int JDBC_BATCH_SIZE = 200;

    private CompareLdapOptions options;

    private JdbcTemplate jdbc;
    private String table;

    private LdapConnection ldapConnection;

    private LdapComparator comparator;

    private Set<String> columns;

    private boolean canceled;

    public LdapImportWorker(CompareLdapOptions options, JdbcTemplate jdbc, String table, LdapConnection ldapConnection,
                            LdapComparator comparator, Set<String> columns) {
        this.options = options;
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
        String[] ignoredAttributes = buildIgnoredAttributes();

        int count = 0;
        try {
            List<Object[]> rows = new ArrayList<>();

            SearchRequest req = buildSearchRequest();
            SearchCursor cur = ldapConnection.search(req);
            while (cur.next()) {
                if (canceled) {
                    break;
                }

                Entry entry = cur.getEntry();

                if (ignoredAttributes != null) {
                    for (String ignored : ignoredAttributes) {
                        if (!entry.containsAttribute(ignored)) {
                            continue;
                        }
                        entry.removeAttributes(ignored);
                    }
                }

                String dn = entry.getDn().getNormName().toLowerCase();
                int worker = Math.abs(dn.hashCode()) % options.getWorkers();

                LdifEntry e = new LdifEntry(entry);
                String ldif = LdifUtils.convertToLdif(e, options.getMaxCompareSize());

                byte[] ldifBinary = ldif.getBytes(StandardCharsets.UTF_8.name());

                if (options.isCompressData()) {
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    try (GZIPOutputStream gzip = new GZIPOutputStream(out)) {
                        gzip.write(ldifBinary);
                    }
                    ldifBinary = out.toByteArray();
                }

                processColumnMap(entry.getAttributes());

                rows.add(new Object[]{dn, worker, ldifBinary});

                if (rows.size() >= JDBC_BATCH_SIZE) {
                    jdbc.batchUpdate("insert into " + table + " (dn, worker, entry) values (?,?,?)", rows);
                    rows.clear();
                }

                count++;

                statusLogger.printStatus(LOG, "Imported {} entries to {}", count, table);
            }

            if (!rows.isEmpty()) {
                jdbc.batchUpdate("insert into " + table + " (dn, worker, entry) values (?,?,?)", rows);
            }
        } catch (Exception ex) {
            throw new LdapComparatorException("Ldap import worker failed, reason: " + ex.getMessage(), ex);
        } finally {
            statusLogger.printStatus(LOG, true, "Imported {} entries to {}", count, table);
        }
    }

    private String[] buildIgnoredAttributes() {
        String[] ignoredAttributes = null;

        Set<String> attrs = comparator.getAttributesToIgnore();
        attrs.remove(null);
        attrs.remove("");
        if (attrs != null && !attrs.isEmpty()) {
            ignoredAttributes = new String[attrs.size()];
            attrs.toArray(ignoredAttributes);
        }

        return ignoredAttributes;
    }

    private void processColumnMap(Collection<Attribute> attributes) {
        attributes.forEach(a -> columns.add(a.getId()));
    }

    // todo handle NONE, SIMPLE, VLV paging
    private SearchRequest buildSearchRequest() throws LdapException {
        SearchRequest req = comparator.buildSearchRequest();

        switch (comparator.getPagingType()) {
            case NONE:
                // nothing to do
                break;
            case SIMPLE:
                PagedResults simple = new PagedResultsImpl();
                // todo add params
                req.addControl(simple);
                break;
            case VLV:
                VirtualListViewRequest vlv = new VirtualListViewRequestImpl();
                // todo add params
                req.addControl(vlv);
                break;
        }

        return req;
    }
}
