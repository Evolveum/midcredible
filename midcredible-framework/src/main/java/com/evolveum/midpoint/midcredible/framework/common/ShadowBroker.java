package com.evolveum.midpoint.midcredible.framework.common;

import com.evolveum.midpoint.client.api.SearchResult;
import com.evolveum.midpoint.client.api.exception.AuthenticationException;
import com.evolveum.midpoint.client.api.exception.ObjectNotFoundException;
import com.evolveum.midpoint.client.impl.restjaxb.RestJaxbService;
import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.util.QueryBuilder;
import com.evolveum.midpoint.prism.polystring.PolyString;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.List;

public class ShadowBroker extends Broker {

    private static final String RESOURCE_REF = "resourceRef";
    private static final Class CLAZZ = ShadowType.class;
    private static final Logger LOG = LoggerFactory.getLogger(ShadowBroker.class);
    private final Context context;
    private final String kind;
    private final String intent;
    private final ResourceButler butler;
    private List<ShadowType> shadows;


    public ShadowBroker(Context context, String kind, String intent, ResourceButler butler) {

        this.context = context;
        this.kind = kind;
        this.intent = intent;
        this.butler = butler;

        fetchShadows();

    }

    private void fetchShadows() {
        String resourceRef = butler.getResouceOid();

        LOG.debug("Pulling shadows based on the provided context from the resource with the following oid: " +
                resourceRef + " the shadows are of the following kind: " + kind + " and intent: " + intent);

        RestJaxbService service = context.getMidpoint();
        SearchResult<ShadowType> result = null;
        try {
            result = service.shadows().search().queryFor(CLAZZ)
                    .item(new QName("kind")).eq(kind).and().item(new QName(RESOURCE_REF)).ref(resourceRef).get();
        } catch (ObjectNotFoundException e) {

            LOG.error(e.getLocalizedMessage());
        } catch (AuthenticationException e) {

            LOG.error(e.getLocalizedMessage());
        }

        if (result != null && !(result.isEmpty())) {
            result.get(0);
        } else {
            LOG.debug("Empty result returned while querying shadows on the resource: " + resourceRef + " for kind: " + kind + " and intent: " + intent);
        }

        result.forEach(shadowType -> LOG.info("Shadow: " + shadowType.getName()));

        this.shadows = result;
    }

    public void compare() {

        if (shadows != null && !(shadows.isEmpty())) {

            shadows.forEach(shadowType -> {

                String name = PolyString.getOrig(shadowType.getName());
                QueryBuilder builder;
                butler.compare();
            });

        } else {
            LOG.debug("Shadows list contains no values");
        }

    }


}
