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

    private CompareLdapOptions options;

    public DefaultLdapComparator(CompareLdapOptions options) {
        this.options = options;
    }

    @Override
    public Set<String> getAttributesToIgnore() {
        if (StringUtils.isEmpty(options.getAttributesToIgnore())) {
            return super.getAttributesToIgnore();
        }

        String ignored = options.getAttributesToIgnore();
        String[] attributes = ignored.split(",");

        Set<String> result = new HashSet<>();
        for (String attribute : attributes) {
            String a = attribute.trim();
            if (StringUtils.isEmpty(a)) {
                continue;
            }

            result.add(a);
        }

        return result;
    }

    @Override
    public SearchRequest buildSearchRequest() throws LdapException {
        SearchScope scope = SearchScope.OBJECT.getScope(options.getScope());

        SearchRequest req = new SearchRequestImpl();
        req.setScope(scope);
        req.addAttributes(options.getAttributes());
        req.setBase(new Dn(options.getBaseDn()));
        req.setFilter(options.getFilter());

        return req;
    }
}
