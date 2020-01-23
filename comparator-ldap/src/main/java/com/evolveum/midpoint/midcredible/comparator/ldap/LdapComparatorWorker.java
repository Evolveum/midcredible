package com.evolveum.midpoint.midcredible.comparator.ldap;

import com.evolveum.midpoint.midcredible.comparator.common.CsvReportPrinter2;
import com.evolveum.midpoint.midcredible.comparator.common.Row;
import com.evolveum.midpoint.midcredible.comparator.common.StatusLogger;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.Column;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.ColumnValue;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.RowState;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.ValueState;
import org.apache.commons.io.IOUtils;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.ldif.LdapLdifException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparatorWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LdapComparatorWorker.class);

    private static final StatusLogger statusLogger = new StatusLogger();

    private CompareLdapOptions options;

    private int workerId;

    private DataSource dataSource;

    private CsvReportPrinter2 printer;

    private LdapComparator comparator;

    private Map<String, Column> columnMap;

    private List<String> columnList;

    private boolean canceled;

    public LdapComparatorWorker(CompareLdapOptions options, int workerId, DataSource dataSource, CsvReportPrinter2 printer,
                                LdapComparator comparator, Map<String, Column> columnMap) {
        this.options = options;
        this.workerId = workerId;
        this.dataSource = dataSource;
        this.printer = printer;
        this.comparator = comparator;
        this.columnMap = columnMap;

        buildColumnList();
    }

    public void cancel() {
        canceled = true;
    }

    @Override
    public void run() {
        int count = 0;

        try (ResultSet oldRs = createResultSet("old_data", workerId);
             ResultSet newRs = createResultSet("new_data", workerId)) {

            boolean moveOld = true;
            boolean moveNew = true;

            Map<Column, Set<Object>> oldRow = null;
            Map<Column, Set<Object>> newRow = null;

            while (true) {
                count++;
                statusLogger.printStatus(LOG, "Compared {} entries", count);

                if (canceled) {
                    break;
                }

                if (moveOld) {
                    moveOld = false;
                    oldRow = oldRs.next() ? createEntryFromRow(oldRs) : null;
                }

                if (oldRow == null) {
                    break;
                }

                if (moveNew) {
                    moveNew = false;
                    newRow = newRs.next() ? createEntryFromRow(newRs) : null;
                }

                if (newRow == null) {
                    // there's nothing left in new table, old table contains more rows than it should, mark old rows as "-"
                    printCsvRow(oldRow, null);
                    moveOld = true;

                    continue;
                }

                RowState state = comparator.compareIdentity(oldRow, newRow);
                switch (state) {
                    case EQUAL:
                        Map<Column, List<ColumnValue>> changes = comparator.compareData(oldRow, newRow);

                        if (!isNoChanges(changes)) {
                            if (!isSkipPrint("modified")) {
                                printCsvRow(oldRow, newRow);
                            }
                        }

                        moveOld = true;
                        moveNew = true;
                        break;
                    case OLD_BEFORE_NEW:
                        // new table contains row that shouldn't be there, mark new as "+"
                        if (!isSkipPrint("new")) {
                            printCsvRow(oldRow, null);
                        }
                        moveOld = true;
                        break;
                    case OLD_AFTER_NEW:
                        // new table misses some rows obviously, therefore old row should be marked as "-"
                        if (!isSkipPrint("old")) {
                            printCsvRow(null, newRow);
                        }
                        moveNew = true;
                        break;
                }
            }

            while (newRs.next()) {
                count++;
                statusLogger.printStatus(LOG, "Compared {} entries", count);

                if (canceled) {
                    break;
                }

                newRow = createEntryFromRow(newRs);
                if (newRow == null) {
                    break;
                }

                // these remaining records are not in old result set, mark new rows as "+"
                if (!isSkipPrint("new")) {
                    printCsvRow(null, newRow);
                }
            }
        } catch (Exception ex) {
            throw new LdapComparatorException("Ldap comparator worker failed, reason: " + ex.getMessage(), ex);
        } finally {
            statusLogger.printStatus(LOG, true, "Compared {} entries", count);
        }
    }

    private boolean isSkipPrint(String key) {
        String skipPrint = options.getSkipPrint();
        return skipPrint != null && skipPrint.contains(key);
    }

    private ResultSet createResultSet(String table, int workerId) throws SQLException {
        Connection con = dataSource.getConnection();

        String where = options.getWorkers() > 1 ? " where worker = ? " : "";
        PreparedStatement pstmt = con.prepareStatement("select entry from " + table + where + " order by dn");
        if (options.getWorkers() > 1) {
            pstmt.setInt(1, workerId);
        }

        return pstmt.executeQuery();
    }

    private boolean isNoChanges(Map<Column, List<ColumnValue>> changes) {
        if (changes == null) {
            return true;
        }

        for (List<ColumnValue> values : changes.values()) {
            if (values == null) {
                continue;
            }

            for (ColumnValue val : values) {
                if (!ValueState.EQUAL.equals(val.getState())) {
                    return false;
                }
            }
        }

        return true;
    }

    private void printCsvRow(Map<Column, Set<Object>> oldRow, Map<Column, Set<Object>> newRow) throws IOException {
        Row oldR = createRowFromMap(oldRow);
        Row newR = createRowFromMap(newRow);

        printer.printCsvRow(oldR, newR);
    }

    private Row createRowFromMap(Map<Column, Set<Object>> row) {
        if (row == null) {
            return null;
        }

        Map<String, List<Object>> attrs = new HashMap<>();
        for (Column column : row.keySet()) {
            Set<Object> values = row.get(column);

            List<Object> list = new ArrayList<>();
            if (options.isPrintRealValues()) {
                for (Object value : values) {
                    Object txt = value instanceof Value ? ((Value) value).getValue() : value;
                    list.add(txt);
                }
            } else {
                list.addAll(values);
            }


            Collections.sort(list, (o1, o2) -> {

                String s1 = o1 != null ? o1.toString() : null;
                String s2 = o2 != null ? o2.toString() : null;

                return String.CASE_INSENSITIVE_ORDER.compare(s1, s2);
            });
            attrs.put(column.getName(), list);
        }

        String uid = (String) attrs.get(LdapDbComparator.DN_ATTRIBUTE).get(0);

        return new Row(uid, attrs);
    }

    private Map<Column, Set<Object>> createEntryFromRow(ResultSet rs) throws SQLException, IOException {
        if (rs.isAfterLast()) {
            return null;
        }

        Map<Column, Set<Object>> result = new HashMap<>();

        byte[] bEntry = rs.getBytes("entry");

        if (options.isCompressData()) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bEntry))) {
                IOUtils.copy(gzip, out);
                bEntry = out.toByteArray();
            }
        }

        String strEntry = new String(bEntry, StandardCharsets.UTF_8);

        try (LdifReader reader = new LdifReader()) {
            List<LdifEntry> entries = reader.parseLdif(strEntry);
            if (entries == null || entries.size() != 1) {
                throw new IllegalStateException("Couldn't find entry");
            }
            LdifEntry ldifEntry = entries.get(0);
            Entry entry = ldifEntry.getEntry();

            result.put(columnMap.get(LdapDbComparator.DN_ATTRIBUTE),
                    new HashSet<>(Arrays.asList(entry.getDn().getNormName())));

            for (Attribute attr : entry.getAttributes()) {
                Set<Object> values = new HashSet<>();
                attr.iterator().forEachRemaining(v -> values.add(v));

                result.put(columnMap.get(attr.getId()), values);
            }
        } catch (IOException | LdapLdifException ex) {
            throw new LdapComparatorException("Couldn't create LDAP entry from LDIF stored in DB, reason: "
                    + ex.getMessage(), ex);
        }

        return result;
    }

    private void buildColumnList() {
        List<Column> list = new ArrayList<>();
        list.addAll(columnMap.values());

        Collections.sort(list, Comparator.comparingInt(c -> c.getIndex()));

        columnList = new ArrayList<>(list.stream().map(c -> c.getName()).collect(Collectors.toList()));
    }
}
