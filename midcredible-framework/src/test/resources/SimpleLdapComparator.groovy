package com.evolveum.midpoint.midcredible.framework.comparator


import org.apache.directory.api.ldap.model.exception.LdapException
import org.apache.directory.api.ldap.model.message.SearchRequest
import org.apache.directory.api.ldap.model.message.SearchRequestImpl
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.api.ldap.model.name.Dn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Created by Viliam Repan (lazyman).
 */
class SimpleLdapComparator implements LdapComparator {

    private static final Logger LOG = LoggerFactory.getLogger(SimpleLdapComparator.class)

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
        String oldDn = oldEntry.get(new Column("dn"))?.first()
        String newDn = newEntry.get(new Column("dn"))?.first()

        int result = String.CASE_INSENSITIVE_ORDER.compare(oldDn, newDn)
        RowState rs
        if (result == 0) {
            rs = RowState.EQUAL
        } else if (result < 0) {
            rs = RowState.OLD_BEFORE_NEW
        } else {
            rs = RowState.OLD_AFTER_NEW
        }

        LOG.trace("Compare entity: {}\n{}\n{}", rs, oldEntry, newEntry)

        return rs
    }

    @Override
    Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        return null    // todo implement
    }
}
