package com.evolveum.midpoint.midcredible.framework.comparator;

import com.evolveum.midpoint.midcredible.framework.util.CsvReportPrinter;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequest;
import org.apache.directory.api.ldap.extras.controls.vlv.VirtualListViewRequestImpl;
import org.apache.directory.api.ldap.model.cursor.CursorException;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.controls.SortKey;
import org.apache.directory.api.ldap.model.message.controls.SortRequest;
import org.apache.directory.api.ldap.model.message.controls.SortRequestControlImpl;
import org.apache.directory.ldap.client.api.LdapConnection;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

            int oldOffset = 0;
            int newOffset = 0;

            SearchCursor oldCursor = executeQuery(oldCon, sortAttribute, oldOffset, PAGE_SIZE);
            SearchCursor newCursor = executeQuery(newCon, sortAttribute, newOffset, PAGE_SIZE);

            Map<Column, Object> oldRow;
            Map<Column, Object> newRow;
            while (oldCursor.next()) {
                oldRow = createMapFromRow(oldCursor);

                if (!newCursor.next()) {
                    // there's nothing left in new table, old table contains more rows than it should, mark old rows as "-"
                    printCsvRow(printer, "-", oldRow);
                    break;
                }

                newRow = createMapFromRow(newCursor);
                RowState state = comparator.compareIdentity(oldRow, newRow);
                switch (state) {
                    case EQUAL:
                        Map<Column, List<ColumnValue>> changes = comparator.compareData(oldRow, newRow);
                        printCsvRow(printer, changes);
                        continue;
                    case OLD_BEFORE_NEW:
                        // new table contains row that shouldn't be there, mark new as "+"
                        printCsvRow(printer, "+", newRow);
                        break;
                    case OLD_AFTER_NEW:
                        // new table misses some rows obviously, therefore old row should be marked as "-"
                        printCsvRow(printer, "-", oldRow);
                        break;
                }
            }

            while (newCursor.next()) {
                newRow = createMapFromRow(newCursor);
                // these remaining records are not in old result set, mark new rows as "+"
                // todo print it out somehow
                printCsvRow(printer, "+", newRow);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex); // todo handle exception
        }
    }

    private void printCsvRow(CsvReportPrinter printer, Map<Column, List<ColumnValue>> entryChanges) {

    }

    private void printCsvRow(CsvReportPrinter printer, String rowState, Map<Column, Object> entry) {

    }

    private Map<Column, Object> createMapFromRow(SearchCursor cursor) throws LdapException {
        Entry entry = getEntry(cursor);
        if (entry == null) {
            return null;
        }

        Map<Column, Object> map = new HashMap<>();
        Iterator<Attribute> iterator = entry.iterator();
        while (iterator.hasNext()) {
            Attribute a = iterator.next();
            map.put(new Column(a.getString(), 0), a.get()); // todo fix column index and value
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
