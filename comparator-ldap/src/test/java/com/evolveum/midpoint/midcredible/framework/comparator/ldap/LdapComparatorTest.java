package com.evolveum.midpoint.midcredible.framework.comparator.ldap;

import com.evolveum.midpoint.midcredible.comparator.ldap.CompareLdapOptions;
import com.evolveum.midpoint.midcredible.comparator.ldap.LdapDbComparator;
import org.testng.annotations.Test;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
public class LdapComparatorTest {

    @Test
    public void simpleCompare() throws Exception {
        CompareLdapOptions opts = new CompareLdapOptions();

        opts.getCsvPrinterOptions().setPath(new File("./target/out.csv"));
        opts.getCsvPrinterOptions().setPrintEqual(false);

        opts.setOldHost("localhost");
        opts.setOldPort(1389);
        opts.setOldSecured(false);
        opts.setOldUsername("cn=admin,dc=example,dc=com");
        opts.setOldPassword("admin");

        opts.setNewHost("localhost");
        opts.setNewPort(2389);
        opts.setNewSecured(false);
        opts.setNewUsername("cn=admin,dc=example,dc=com");
        opts.setNewPassword("admin");

        opts.setCompareScriptPath(new File("./src/test/resources/SimpleLdapComparator.groovy"));
        opts.setWorkers(2);

        new LdapDbComparator(opts).execute();
    }
}
