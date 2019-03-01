package com.evolveum.midpoint.midcredible.example;

import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import com.evolveum.midpoint.midcredible.framework.util.TableComparator;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;
import java.sql.SQLException;


@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ObjectComparisionTest extends ButlerBaseTest {

    private static final String PROPERTIES = "./src/test/resources/configuration_test.properties";

    @BeforeClass
    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();
    }

    @Test
    public void testCompareAllResourceJdbc() throws SQLException, IOException {
        TableComparator tableCompare = new TableComparator(PROPERTIES);
        tableCompare.compare(false);

    }
}
