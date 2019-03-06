package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import com.evolveum.midpoint.midcredible.framework.util.Diff;
import com.evolveum.midpoint.midcredible.framework.util.State;
import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.ldif.LdapLdifException;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparatorWorker implements Runnable {

    private static final Logger LOG = LoggerFactory.getLogger(LdapComparatorWorker.class);

    private int workerId;

    private DataSource dataSource;

    private CsvReportPrinter printer;

    private LdapComparator comparator;

    private Map<String, Column> columnMap;

    private List<String> columnList;

    private boolean canceled;

    public LdapComparatorWorker(int workerId, DataSource dataSource, CsvReportPrinter printer,
                                LdapComparator comparator, Map<String, Column> columnMap) {
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
        try {
            ResultSet oldRs = createResultSet("old_data", workerId);
            ResultSet newRs = createResultSet("new_data", workerId);

            boolean moveOld = true;
            boolean moveNew = true;

            Map<Column, Set<Object>> oldRow = null;
            Map<Column, Set<Object>> newRow = null;

            while (true) {
                if (canceled) {
                    break;
                }

                if (moveOld) {
                    oldRs.next();
                    moveOld = false;
                    oldRow = createEntryFromRow(oldRs);
                }

                if (oldRow == null) {
                    break;
                }

                if (moveNew) {
                    newRs.next();
                    moveNew = false;
                    newRow = createEntryFromRow(newRs);
                }

                if (newRow == null) {
                    // there's nothing left in new table, old table contains more rows than it should, mark old rows as "-"
                    printCsvRow(printer, RowState.OLD_AFTER_NEW, oldRow);
                    moveOld = true;

                    continue;
                }

                RowState state = comparator.compareIdentity(oldRow, newRow);
                switch (state) {
                    case EQUAL:
                        Map<Column, List<ColumnValue>> changes = comparator.compareData(oldRow, newRow);
                        printCsvRow(printer, changes);
                        moveOld = true;
                        moveNew = true;
                        break;
                    case OLD_BEFORE_NEW:
                        // new table contains row that shouldn't be there, mark new as "+"
                        printCsvRow(printer, RowState.OLD_BEFORE_NEW, newRow);
                        moveOld = true;
                        break;
                    case OLD_AFTER_NEW:
                        // new table misses some rows obviously, therefore old row should be marked as "-"
                        printCsvRow(printer, RowState.OLD_AFTER_NEW, oldRow);
                        moveNew = true;
                        break;
                }
            }

            while (newRs.next()) {
                if (canceled) {
                    break;
                }

                newRow = createEntryFromRow(newRs);
                if (newRow == null) {
                    break;
                }

                // these remaining records are not in old result set, mark new rows as "+"
                printCsvRow(printer, RowState.OLD_BEFORE_NEW, newRow);
            }
        } catch (Exception ex) {
            throw new LdapComparatorException("Ldap comparator worker failed, reason: " + ex.getMessage(), ex);
        }
    }

    private ResultSet createResultSet(String table, int workerId) throws SQLException {
        Connection con = dataSource.getConnection();

        PreparedStatement pstmt = con.prepareStatement("select entry from " + table + " where worker = ? order by dn");
        pstmt.setInt(1, workerId);

        return pstmt.executeQuery();
    }

    private void printCsvRow(CsvReportPrinter printer, Map<Column, List<ColumnValue>> changes) throws IOException {
        LOG.info("Printing EQUAL");
        if (1 == 1) return;

        Map<String, com.evolveum.midpoint.midcredible.framework.util.structural.Attribute> attributes = new HashMap<>();

        boolean changed = false;
        for (Map.Entry<Column, List<ColumnValue>> entry : changes.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {

            }
        }

        for (List<ColumnValue> values : changes.values()) {
            if (values == null) {
                continue;
            }

            for (ColumnValue value : values) {
                if (!ValueState.EQUAL.equals(value.getState())) {
                    changed = true;
                }

                // todo transform to entry for printer
            }
        }

        Entity entity = new Entity(null, attributes);
        entity.setChanged(getState(RowState.EQUAL, changed));

        printer.printCsvRow(columnList, entity);
    }

    private void printCsvRow(CsvReportPrinter printer, RowState rowState, Map<Column, Set<Object>> entry) throws IOException {
        LOG.info("Printing {}", rowState);

        Map<String, com.evolveum.midpoint.midcredible.framework.util.structural.Attribute> attributes = new HashMap<>();

        for (Map.Entry<Column, Set<Object>> e : entry.entrySet()) {
            if (e.getValue() == null || e.getValue().isEmpty()) {
                continue;
            }

            String attrName = e.getKey().getName();

            com.evolveum.midpoint.midcredible.framework.util.structural.Attribute attr =
                    new com.evolveum.midpoint.midcredible.framework.util.structural.Attribute(attrName);
            attr.setValues(new HashMap<>());

            attr.getValues().put(Diff.NONE, e.getValue());

            attributes.put(attr.getName(), attr);
        }

        Entity entity = new Entity(null, attributes);
        entity.setChanged(getState(rowState, true));

        printer.printCsvRow(columnList, entity);
    }

    private State getState(RowState state, boolean changed) {
        switch (state) {
            case EQUAL:
                return changed ? State.MODIFIED : State.EQUAL;
            case OLD_AFTER_NEW:
                return State.OLD_AFTER_NEW;
            case OLD_BEFORE_NEW:
                return State.OLD_BEFORE_NEW;
            default:
                return null;
        }
    }

    private Map<Column, Set<Object>> createEntryFromRow(ResultSet rs) throws SQLException {
        if (rs.isAfterLast()) {
            return null;
        }

        Map<Column, Set<Object>> result = new HashMap<>();

        String strEntry = rs.getString("entry");

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

        Collections.sort(list, (o1, o2) -> o1.getIndex() - o2.getIndex());

        columnList = new ArrayList<>(list.stream().map(c -> c.getName()).collect(Collectors.toList()));
    }
}