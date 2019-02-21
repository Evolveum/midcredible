package com.evolveum.midpoint.midcredible.example;
import com.evolveum.midpoint.midcredible.framework.LdapButler;
import com.evolveum.midpoint.midcredible.framework.objects.Resource;
import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;

@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ObjectComparisionTest extends ButlerBaseTest {
    LdapButler targetResource = null;
    LdapButler comparisonSource= null;

    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();
        targetResource = new LdapButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");
        comparisonSource = new LdapButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");

    }

      public void testCompareAllResource() throws Exception {

         targetResource
                 .compare()
                    .all(comparisonSource)
                        .statistics()
                                .toCsv("")
                                    .execute();

    }
}
