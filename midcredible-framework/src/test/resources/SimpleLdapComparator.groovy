package com.evolveum.midpoint.midcredible.framework.comparator

import org.apache.directory.api.ldap.model.entry.Entry
import org.apache.directory.api.ldap.model.exception.LdapException
import org.apache.directory.api.ldap.model.ldif.LdifEntry
import org.apache.directory.api.ldap.model.message.SearchRequest
import org.apache.directory.api.ldap.model.message.SearchRequestImpl
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.api.ldap.model.name.Dn

/**
 * Created by Viliam Repan (lazyman).
 */
class SimpleLdapComparator implements LdapComparator {

    @Override
    List<String> getReportedAttributes() {
        return Arrays.asList("dn", "givenName", "familyName")
    }

    @Override
    SearchRequest buildSearchRequest() throws LdapException {
        SearchRequest req = new SearchRequestImpl()
        req.setScope(SearchScope.SUBTREE)
        req.addAttributes("*")
        req.setBase(new Dn("dc=example,dc=com"))
        req.setFilter("(objectClass=*)")

        return req
    }

    @Override
    RowState compareIdentity(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        Entry entry = null
        LdifEntry ldifEntry = new LdifEntry(entry)

//        return oldEntry.get("dn").equals(newEntry.get("dn"))
        return null    // todo compare dn
    }

    @Override
    Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        return null    // todo implement
    }
}
