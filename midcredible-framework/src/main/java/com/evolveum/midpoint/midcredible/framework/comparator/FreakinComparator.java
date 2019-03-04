package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;

import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public interface FreakinComparator {

    SearchRequest buildSearchRequest() throws LdapException;

    RowState compareIdentity(Map<Column, Object> old, Map<Column, Object> entry);

    Map<Column, List<ColumnValue>> compareData(Map<Column, Object> old, Map<Column, Object> entry);
}
