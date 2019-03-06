package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface FreakinComparator {

    List<String> getReportedAttributes();

    SearchRequest buildSearchRequest() throws LdapException;

    RowState compareIdentity(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry);

    Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry);
}
