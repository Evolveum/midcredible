package com.evolveum.midpoint.midcredible.comparator.ldap;

import com.evolveum.midpoint.midcredible.comparator.ldap.util.Column;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.ColumnValue;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.RowState;
import com.evolveum.midpoint.midcredible.comparator.ldap.util.ValueState;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by Viliam Repan (lazyman).
 */
public abstract class LdapComparator {

    private CompareLdapOptions options;

    public LdapComparator(CompareLdapOptions options) {
        this.options = options;
    }

    private static final Logger LOG = LoggerFactory.getLogger(LdapComparator.class);

    public enum Paging {

        NONE, SIMPLE, VLV;
    }

    RowState compareIdentity(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        String oldDn = firstAsString(oldEntry.get(new Column("dn")));
        String newDn = firstAsString(newEntry.get(new Column("dn")));

        int result = String.CASE_INSENSITIVE_ORDER.compare(oldDn, newDn);
        RowState rs;
        if (result == 0) {
            rs = RowState.EQUAL;
        } else if (result < 0) {
            rs = RowState.OLD_BEFORE_NEW;
        } else {
            rs = RowState.OLD_AFTER_NEW;
        }

        LOG.trace("Compare entity: {}\n{}\n{}", rs, oldEntry, newEntry);

        return rs;
    }

    private String firstAsString(Set<Object> set) {
        if (set == null || set.isEmpty()) {
            return null;
        }

        return set.iterator().next().toString();
    }

    public Paging getPagingType() {
        return Paging.NONE;
    }

    public Set<String> getAttributesToIgnore() {
        String attrs = options.getAttributesToIgnore();

        if (StringUtils.isEmpty(attrs)) {
            return getAttributesToIgnore(null);

        }

        String[] attributes = attrs.split(",");
        return getAttributesCompareIgnoreCase(attributes);
    }

    public Set<String> getAttributesCompareIgnoreCase() {

        String attrs = options.getAttributesCompareIgnoreCase();

        if (StringUtils.isEmpty(attrs)) {
            return getAttributesCompareIgnoreCase(null);

        }

        String[] attributes = attrs.split(",");
        return getAttributesCompareIgnoreCase(attributes);
    }

    private Set<String> getAttributesToIgnore(String[] attributes) {

        return getAttributes(attributes);
    }

    private Set<String> getAttributesCompareIgnoreCase(String[] attributes) {

        return getAttributes(attributes);
    }

    private Set<String> getAttributes(String[] attributes) {

        Set<String> result = new HashSet<>();

        if (attributes != null && attributes.length != 0) {
            for (String attribute : attributes) {

                String a = attribute.trim();
                if (StringUtils.isEmpty(a)) {

                    continue;
                }
                result.add(a);
            }
        }
        return result;
    }

    protected Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        Set<String> attributesToIgnore = getAttributesToIgnore();
        Set<String> attributesCompareIgnoreCase = getAttributesCompareIgnoreCase();

        Map<Column, List<ColumnValue>> changes = new HashMap<>();

        Set<String> comparedAttributes = new HashSet<>();

        for (Map.Entry<Column, Set<Object>> entry : oldEntry.entrySet()) {
            Column column = entry.getKey();
            String columnName = column.getName();
            if (attributesToIgnore.contains(columnName)) {
                continue;
            }

            Set<Object> oldValues = entry.getValue();

            comparedAttributes.add(columnName);

            Set<Object> newValues = newEntry.get(column);

            if (newValues == null) {
                // all old values for this attribute were removed
                List<ColumnValue> changedVals = new ArrayList<>();
                oldValues.forEach(o -> changedVals.add(new ColumnValue(o, ValueState.REMOVED)));
                changes.put(column, changedVals);

                continue;
            }

            List<ColumnValue> changedVals = new ArrayList<>();

            if (attributesCompareIgnoreCase.contains(columnName)) {

                Set<Object> equalSet = new HashSet<>();
                oldValues.forEach(o -> {

                    String oldLc = (o == null) ? "null" : o.toString();
                    oldLc = StringUtils.lowerCase(oldLc);

                    AtomicReference<Boolean> equal = new AtomicReference<>(false);
                    // Has to be effectively final
                    String lcValueOld = oldLc;
                    newValues.forEach(no -> {

                        String nLc = (no == null) ? "null" : no.toString();
                        nLc = StringUtils.lowerCase(nLc);

                        if (lcValueOld != null || nLc != null) {

                            if (!lcValueOld.equals(nLc)) {

                                equalSet.add(no);
                            } else {

                                equal.set(true);
                                changedVals.add(new ColumnValue(no, ValueState.EQUAL));
                            }
                        }

                    });

                    if (!equal.get()) {
                        changedVals.add(new ColumnValue(o, ValueState.REMOVED));
                    }
                });

                Collection col = CollectionUtils.subtract(equalSet, newValues);
                col.forEach(o -> changedVals.add(new ColumnValue(o, ValueState.ADDED)));
            } else {

                Collection col = CollectionUtils.intersection(oldValues, newValues);
                col.forEach(o -> changedVals.add(new ColumnValue(o, ValueState.EQUAL)));

                col = CollectionUtils.subtract(oldValues, newValues);
                col.forEach(o -> changedVals.add(new ColumnValue(o, ValueState.REMOVED)));

                col = CollectionUtils.subtract(newValues, oldValues);
                col.forEach(o -> changedVals.add(new ColumnValue(o, ValueState.ADDED)));

            }
            changes.put(column, changedVals);
        }


        for (Map.Entry<Column, Set<Object>> entry : newEntry.entrySet()) {
            Column column = entry.getKey();
            if (attributesToIgnore.contains(column.getName())) {
                continue;
            }

            if (comparedAttributes.contains(column.getName())) {
                continue;
            }

            // all new values for this attribute were added
            List<ColumnValue> changedVals = new ArrayList<>();
            entry.getValue().forEach(o -> changedVals.add(new ColumnValue(o, ValueState.ADDED)));
            changes.put(column, changedVals);
        }

        return changes;
    }

/*    public Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        return compareData(oldEntry, newEntry, getAttributesToIgnore());
    }*/

    public abstract SearchRequest buildSearchRequest() throws LdapException;

    public CompareLdapOptions getOptions() {
        return options;
    }
}
