package com.evolveum.midpoint.midcredible.example;
import com.evolveum.midpoint.midcredible.framework.LdapButler;
import com.evolveum.midpoint.midcredible.framework.TableButler;
import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import com.evolveum.midpoint.midcredible.framework.util.JdbcUtil;
import com.evolveum.midpoint.midcredible.framework.util.TableComparator;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.sql.DataSource;
import java.sql.SQLException;

@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ObjectComparisionTest extends ButlerBaseTest {
    LdapButler targetResource = null;
    LdapButler comparisonSource= null;

    TableButler targetResourceTable = null;
    TableButler comparisonSourceTable= null;
    DataSource oldDb;
    DataSource newDb;
    private static final String COMPARATOR = "./src/test/resources/SimpleComparator.groovy";
    private static final String OUT = "./src/test/resources/out.csv";

    @BeforeClass
    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();
//        targetResource = new LdapButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");
//        comparisonSource = new LdapButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");
//
//        targetResourceTable = new TableButler("foo", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6",COMPARATOR);
//        comparisonSourceTable = new TableButler("bar", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff43abc6");


        JdbcUtil util = new JdbcUtil();
        oldDb = util.setupDataSource("jdbc:oracle:thin:@:1521:xe","","","oracle.jdbc.OracleDriver");
        newDb = util.setupDataSource("jdbc:oracle:thin:@:1521:xe","","","oracle.jdbc.OracleDriver");
       // DataSource dataSource = util.setupDataSource()
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

      @Test
      public void testCompareAllResourceJdbc() throws SQLException {
          TableComparator tableCompare = new TableComparator(newDb, oldDb,COMPARATOR);
          tableCompare.compare(OUT,"ID",false);

    }
}
