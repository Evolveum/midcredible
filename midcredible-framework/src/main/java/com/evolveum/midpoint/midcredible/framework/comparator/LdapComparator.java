package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import com.evolveum.midpoint.midcredible.framework.util.Diff;
import com.evolveum.midpoint.midcredible.framework.util.State;
import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequest;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequestImpl;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.controls.SortKey;
import org.apache.directory.api.ldap.model.message.controls.SortRequest;
import org.apache.directory.api.ldap.model.message.controls.SortRequestControlImpl;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.io.IOException;
import java.util.*;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparator {

    private static final int PAGE_SIZE = 1000;

    private FreakinComparator comparator = new FreakinComparatorImpl();

    public void execute() {
        try (CsvReportPrinter printer = new CsvReportPrinter("./target/out.csv");           // todo fix csv path
             LdapConnection oldCon = setupConnection("host", 1389, false);
             LdapConnection newCon = setupConnection("host", 2389, false)) {

            String sortAttribute = "dn";

            int oldOffset = 0;      // todo move offsets!
            int newOffset = 0;

            SearchCursor oldCursor = executeQuery(oldCon, sortAttribute, oldOffset, PAGE_SIZE);
            SearchCursor newCursor = executeQuery(newCon, sortAttribute, newOffset, PAGE_SIZE);

            Map<Column, List<Object>> oldRow = null;
            Map<Column, List<Object>> newRow = null;

            boolean moveOld = false;
            boolean moveNew = false;

            while (true) {
                if (moveOld) {
                    oldCursor = moveToNextEntry(oldCursor, oldCon, sortAttribute, oldOffset, PAGE_SIZE);
                    oldRow = createMapFromRow(oldCursor);
                    moveOld = false;
                }

                if (moveNew) {
                    newCursor = moveToNextEntry(newCursor, newCon, sortAttribute, newOffset, PAGE_SIZE);
                    newRow = createMapFromRow(newCursor);
                    moveNew = false;
                }

                if (oldRow == null) {
                    break;
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

            while (true) {
                newCursor = moveToNextEntry(newCursor, newCon, sortAttribute, newOffset, PAGE_SIZE);
                newRow = createMapFromRow(newCursor);

                if (newRow == null) {
                    break;
                }

                // these remaining records are not in old result set, mark new rows as "+"
                printCsvRow(printer, RowState.OLD_BEFORE_NEW, newRow);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle exception
        }
    }

    private void printCsvRow(CsvReportPrinter printer, Map<Column, List<ColumnValue>> changes) throws IOException {
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


            }
        }

        Entity entity = new Entity(null, attributes);
        entity.setChanged(getState(RowState.EQUAL, changed));

        printer.printCsvRow(comparator.getReportedAttributes(), entity);
    }

    private void printCsvRow(CsvReportPrinter printer, RowState rowState, Map<Column, List<Object>> entry) throws IOException {
        Map<String, com.evolveum.midpoint.midcredible.framework.util.structural.Attribute> attributes = new HashMap<>();

        for (Map.Entry<Column, List<Object>> e : entry.entrySet()) {
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

        printer.printCsvRow(comparator.getReportedAttributes(), entity);
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

    private Map<Column, List<Object>> createMapFromRow(SearchCursor cursor) throws LdapException {
        Entry entry = getEntry(cursor);
        if (entry == null) {
            return null;
        }

        Map<Column, List<Object>> map = new HashMap<>();
        Iterator<Attribute> iterator = entry.iterator();
        while (iterator.hasNext()) {
            Attribute a = iterator.next();

            List<Object> vals = new ArrayList<>();

            Iterator<Value> values = a.iterator();
            while (values.hasNext()) {
                vals.add(values.next().getValue());
            }

            map.put(new Column(a.getString(), -1), vals);
        }

        return map;
    }

    private Entry getEntry(SearchCursor cursor) throws LdapException {
        if (cursor != null && cursor.available()) {
            return cursor.getEntry();
        }

        return null;
    }

    private SearchCursor moveToNextEntry(SearchCursor cursor, LdapConnection connection, String sortAttribute,
                                         int offset, int pageSize) throws CursorException, LdapException {

        if (!cursor.isAfterLast()) {
            cursor.next();
        }

        SearchCursor newCursor = executeQuery(connection, sortAttribute, offset, pageSize);
        newCursor.first();

        return newCursor;
    }

    private SearchCursor executeQuery(LdapConnection connection, String sortAttribute, int offset, int pageSize)
            throws LdapException {
        SearchRequest req = comparator.buildSearchRequest();

        // vlv
        VirtualListViewRequest vlv = new VirtualListViewRequestImpl();
        vlv.setOffset(offset);
        vlv.setContentCount(pageSize);
        req.addControl(vlv);

        // sort
        SortRequest sort = new SortRequestControlImpl();
        sort.addSortKey(new SortKey(sortAttribute));
        req.addControl(sort);

        return connection.search(req);
    }

    private LdapConnection setupConnection(String host, int port, boolean secured) {
        return new LdapNetworkConnection(host, port, secured);
    }
}
