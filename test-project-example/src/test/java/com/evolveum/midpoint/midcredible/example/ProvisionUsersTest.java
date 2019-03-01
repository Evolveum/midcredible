package com.evolveum.midpoint.midcredible.example;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.TableButler;
import com.evolveum.midpoint.midcredible.framework.test.ButlerBaseTest;
import org.springframework.test.context.ContextConfiguration;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testng.AssertJUnit;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.namespace.QName;

@ContextConfiguration(classes = com.evolveum.midpoint.midcredible.example.ContextConfiguration.class)
public class ProvisionUsersTest extends ButlerBaseTest {

    private static final String PREFIXATTRS = "attributes/";

    private TableButler tableButler;

    @BeforeClass
    @Override
    public void beforeClass() throws Exception {
        super.beforeClass();

//        tableButler = new TableButler("source_table_test_A", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
//        tableButler.init();
//
//        tableButler = new TableButler("provision_table_test_B", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
//        tableButler.init();
//
//        tableButler = new TableButler("compare_table_test_C", getContext(), "04afeda6-394b-11e6-8cbe-abf7ff430056");
//        tableButler.init();

    }

    @Test
    public void testPullShadowData() throws AuthenticationException, ObjectNotFoundException {

        Context ctx = getContext();
        RestJaxbService service = ctx.getMidpoint();

        ItemPathType path = new ItemPathType();
        path.setValue("attributes/username");
        SearchResult<ShadowType> result = service.shadows().search().queryFor(ShadowType.class)
                //.item(path).eq("administrator").and()
                .item(new QName("kind")).eq(ShadowKindType.ACCOUNT).and()
                .item(new QName("resourceRef")).ref("ef2bc95b-76e0-59e2-86d6-9999cccccccc").get();

        AssertJUnit.assertNotNull(result);
        AssertJUnit.assertFalse(result.isEmpty());
        ShadowType shadow = result.get(0);

        result.forEach(shadowType -> {
            System.out.println("The Shadow ID: " + shadowType.getName().getContent().toString());

        });
    }

    // @Test
//    public void provisionUserInitial() throws Exception{
//        JdbcTemplate tableResource = tableButler.getClient();
//        assertUsersCount(1L);
//        ItemPathType namePath = new ItemPathType(PREFIXATTRS+"name");
//
//        ItemPathType assignmentTargetRef = new ItemPathType("assignment/targetRef");
//
//        Context ctx = getContext();
//        RestJaxbService service = ctx.getMidpoint();
//        String testUserName="doej";
//        SearchResult<RoleType> result = service.roles().search().queryFor(RoleType.class)
//                .item(namePath).eq("basic_account_construction_r001").get();
//
//        AssertJUnit.assertNotNull(result);
//        AssertJUnit.assertFalse(result.isEmpty());
//        RoleType r001 = result.get(0);
//
//        SearchResult<UserType> userSearch = service.users().search().queryFor(UserType.class)
//                .item(namePath).eq(testUserName).get();
//        UserType user = userSearch.get(0);
//
//        AssignmentType assignmentType = new AssignmentType();
//        ObjectReferenceType targetRef = new ObjectReferenceType();
//        targetRef.setOid(r001.getOid());
//        targetRef.setType(RoleType.COMPLEX_TYPE);
//        assignmentType.setTargetRef(targetRef);
//
//        try{
//            service.users().oid(user.getOid())
//                    .modify()
//                    .add("assignment",assignmentType);
//
//        }catch (ObjectNotFoundException e){
//
//
//        }
//
//
//
//        // *** Begin *** Use schrodinger ?
//        //--> importUser (importFromResource_A)
//        //--> importAccountConstructionRole_B
//        //--> assignAccountConstructionRole_B
//        //--> compareWithShadowTable_C
//
//        //-->
//        //-->
//        //-->
//        //-->
//        //-->
//        //-->
//    }

    // @Test
    public void syncUserAfterProvisioning() {
        JdbcTemplate tableResource = tableButler.getClient();
        assertUsersCount(1L);

        // *** Begin *** Use schrodinger ?
        //--> importUser (importFromResource_A)
        //--> importAccountConstructionRole_B
        //--> assignAccountConstructionRole_B
        //--> compareWithShadowTable_C

        //-->
        //-->
        //-->
        //-->
        //-->
        //-->
    }

    // @Test
    public void deprovisionUser() {
        JdbcTemplate tableResource = tableButler.getClient();
        assertUsersCount(1L);

        // *** Begin *** Use schrodinger ?
        //--> importUser (importFromResource_A)
        //--> importAccountConstructionRole_B
        //--> assignAccountConstructionRole_B
        //--> compareWithShadowTable_C

        //-->
        //-->
        //-->
        //-->
        //-->
        //-->
    }

    private void assertUsersCount(long expectedCount) {
        Number count = tableButler.getClient().queryForObject("select count(*) from users", Number.class);
        long lCount = count.longValue();

        AssertJUnit.assertEquals(expectedCount, lCount);
    }

//    private List<Entity> fetchObjectDataFromResource(ResourceButler butler, List<ShadowType> shadows){
//        QueryBuilderImplementation queryBuilderFactory = new QueryBuilderImplementation();
//
//
//    return identityList;
//    }

}
