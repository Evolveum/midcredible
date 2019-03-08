package com.evolveum.midpoint.midcredible.framework.comparator

import com.evolveum.midpoint.midcredible.comparator.ldap.LdapComparator
import org.apache.directory.api.ldap.model.exception.LdapException
import org.apache.directory.api.ldap.model.message.SearchRequest
import org.apache.directory.api.ldap.model.message.SearchRequestImpl
import org.apache.directory.api.ldap.model.message.SearchScope
import org.apache.directory.api.ldap.model.name.Dn

/**
 * Created by Viliam Repan (lazyman).
 */
class SimpleLdapComparator extends LdapComparator {

    @Override
    Set<String> getAttributesToIgnore() {
        return new HashSet<String>(Arrays.asList("employeenumber","objectclass", "dc", "userpassword", "", "uid"))
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
}
