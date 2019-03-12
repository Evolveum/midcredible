package com.evolveum.midpoint.midcredible.example;

import com.evolveum.midpoint.midcredible.cmd.CompareTableAction;
import com.evolveum.midpoint.midcredible.comparator.table.CompareTableOptions;
import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ObjectComparisionTest extends ButlerBaseTest {

    @BeforeClass
    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();
    }

    @Test
    public void testCompareAllResourceJdbc() throws Exception {

        CompareTableAction compare = new CompareTableAction();
        compare.init(buildOptions());

    }

    public  CompareTableOptions buildOptions(){
        CompareTableOptions options = new CompareTableOptions();

        return options;
    }
}
