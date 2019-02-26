package com.evolveum.midpoint.midcredible.example;

import com.evolveum.midpoint.midcredible.framework.LdapButler;
import com.evolveum.midpoint.midcredible.framework.objects.User;
import org.springframework.test.context.ContextConfiguration;

import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import org.testng.annotations.BeforeClass;


@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class AccountComparisionTest  extends ButlerBaseTest {
    LdapButler targetResource;
    LdapButler comparisonSource;
    private static final String KIND_ACCOUNT = "account";
    private static final String KIND_ENTITLEMENT = "entitlement";
    private static final String KIND_GENERIC = "generic";


    @BeforeClass
    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();

      targetResource = new LdapButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");
      comparisonSource = new LdapButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");


    }

    public void testCompareAccountsOnMidpointDifferent() throws Exception {

      User users = new User();
        users
            .search(getContext())
                .shadows(comparisonSource)
                 .by()
                    .kindAndIntent(KIND_ACCOUNT, null)
                        .confirm() ;// This should provide a list of shadows
//                            .compare()
//                                .withResource(comparisonSource)
//                                    .produce()
//                                        .statistics()
//                                            .full()//for the full subset or selective - > for specific user


    }

    public void testCompareAccountIdentical(){


    }

    public void testCompareAccountsSameAmount(){


    }


}
