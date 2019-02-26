package com.evolveum.midpoint.midcredible.example;
import com.evolveum.midpoint.midcredible.framework.LdapButler;
import com.evolveum.midpoint.midcredible.framework.TableButler;
import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ObjectComparisionTest extends ButlerBaseTest {
    LdapButler targetResource = null;
    LdapButler comparisonSource= null;

    TableButler targetResourceTable = null;
    TableButler comparisonSourceTable= null;

    private static final String COMPARATOR = "./src/test/resources/SimpleComparator.groovy";
    private static final String OUT = "./src/test/resources/out.csv";


    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();
        targetResource = new LdapButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");
        comparisonSource = new LdapButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");

        targetResourceTable = new TableButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6",COMPARATOR);
        comparisonSourceTable = new TableButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");

    }

//      public void testCompareAllResourceLdap() throws Exception {
//
//         targetResource
//                 .compare()
//                    .all(comparisonSource)
//                        .statistics()
//                                .toCsv("")
//                                    .execute();
//    }

//      public void testCompareAllResourceJdbc() throws Exception {
//
//          targetResourceTable
//                  .statistics()
//                    .toCsv(OUT)
//                    .toLog()
//                  .confirm()
//                    .compare(comparisonSourceTable);
//
//    }
}
