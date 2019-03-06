package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Viliam Repan (lazyman).
 */
public class FreakinComparatorImpl implements FreakinComparator {

    @Override
    public List<String> getReportedAttributes() {
        return Arrays.asList("dn", "givenName", "familyName");
    }

    @Override
    public SearchRequest buildSearchRequest() throws LdapException {
        SearchRequest req = new SearchRequestImpl();
        req.setScope(SearchScope.SUBTREE);
        req.addAttributes("*");
        req.setTimeLimit(5);
        req.setBase(new Dn("ou=system"));
        req.setFilter("*");

        return req;
    }

    @Override
    public RowState compareIdentity(Map<Column, List<Object>> old, Map<Column, List<Object>> entry) {
        return null;    // todo compare dn
    }

    @Override
    public Map<Column, List<ColumnValue>> compareData(Map<Column, List<Object>> old, Map<Column, List<Object>> entry) {
        return null;    // todo implement
    }
}
