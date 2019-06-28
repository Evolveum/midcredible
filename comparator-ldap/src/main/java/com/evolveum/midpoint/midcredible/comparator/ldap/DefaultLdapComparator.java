package com.evolveum.midpoint.midcredible.comparator.ldap;

import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequest;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;

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

        String attrs = options.getAttributes();
        if (attrs == null) {
            attrs = "*";
        }

        String[] parsed = attrs.split(",");

        req.addAttributes(parsed);
        req.setBase(new Dn(options.getBaseDn()));
        req.setFilter(options.getFilter());

        return req;
    }
}
