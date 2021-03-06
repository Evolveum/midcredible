package com.evolveum.midpoint.midcredible.cmd;

import com.evolveum.midpoint.midcredible.comparator.ldap.CompareLdapOptions;
import com.evolveum.midpoint.midcredible.comparator.ldap.LdapDbComparator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class CompareLdapAction implements Action<CompareLdapOptions> {

    private CompareLdapOptions options;

    @Override
    public void init(CompareLdapOptions opts) {
        options = opts;

    }

    @Override
    public void execute() {
        new LdapDbComparator(options).execute();
    }
}
