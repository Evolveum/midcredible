package com.evolveum.midpoint.midcredible.framework.comparator

import com.evolveum.midpoint.midcredible.framework.comparator.ldap.Column
import com.evolveum.midpoint.midcredible.framework.comparator.ldap.ColumnValue
import com.evolveum.midpoint.midcredible.framework.comparator.ldap.LdapComparator
import org.apache.commons.collections.CollectionUtils
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
class SimpleLdapComparator extends LdapComparator {

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
    Map<Column, List<ColumnValue>> compareData(Map<Column, Set<Object>> oldEntry, Map<Column, Set<Object>> newEntry) {
        return compareData(oldEntry, newEntry, null);
    }
}
