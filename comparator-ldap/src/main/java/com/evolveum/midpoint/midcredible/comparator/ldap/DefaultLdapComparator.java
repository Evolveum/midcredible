package com.evolveum.midpoint.midcredible.comparator.ldap;

import org.apache.commons.lang.StringUtils;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Viliam Repan (lazyman).
 */
public class DefaultLdapComparator extends LdapComparator {

    public DefaultLdapComparator(CompareLdapOptions options) {
        super(options);
    }

    @Override
    public SearchRequest buildSearchRequest() throws LdapException {
        CompareLdapOptions options = getOptions();

        SearchScope scope = SearchScope.OBJECT.getScope(options.getScope());

        SearchRequest req = new SearchRequestImpl();
        req.setScope(scope);
        req.addAttributes(options.getAttributes());
        req.setBase(new Dn(options.getBaseDn()));
        req.setFilter(options.getFilter());

        return req;
    }
}
