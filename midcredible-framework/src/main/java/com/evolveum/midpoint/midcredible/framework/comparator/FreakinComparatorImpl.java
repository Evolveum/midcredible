package com.evolveum.midpoint.midcredible.framework.comparator;

import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.filter.ExprNode;
import org.apache.directory.api.ldap.model.filter.ObjectClassNode;
import org.apache.directory.api.ldap.model.ldif.LdifEntry;
import org.apache.directory.api.ldap.model.ldif.LdifUtils;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        req.setBase(new Dn("dc=example,dc=com"));
        req.setFilter("(objectClass=*)");

        return req;
    }

    @Override
    public RowState compareIdentity(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        Entry entry = null;
        LdifEntry ldifEntry = new LdifEntry(entry);

//        return oldEntry.get("dn").equals(newEntry.get("dn"));
        return null;    // todo compare dn
    }

    @Override
    public Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        return null;    // todo implement
    }
}
