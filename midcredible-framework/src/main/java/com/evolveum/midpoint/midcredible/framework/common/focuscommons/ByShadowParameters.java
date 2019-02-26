package com.evolveum.midpoint.midcredible.framework.common.focuscommons;

import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.common.By;
import com.evolveum.midpoint.midcredible.framework.common.ShadowBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ByShadowParameters<T> extends By<T> {

    private String kind;
    private String intent;
    private static final String DEFAULT_INTENT = "default";
    private ResourceButler resourceButler;
    private Context context;
    private static final Logger LOG = LoggerFactory.getLogger(ByShadowParameters.class);

    public ByShadowParameters(T parent) {
        super(parent);
    }

    public ByShadowParameters(T parent, ResourceButler resourceButler, Context ctx) {
        this(parent);
        this.resourceButler = resourceButler;
        this.context = ctx;
    }


    public ByShadowParameters kindAndIntent(String kind, String intent) throws Exception {

        if (kind != null && kind.isEmpty()) {
        } else {
            LOG.error("Parameter \"kind\" empty or null");
            throw new Exception("Parameter \"kind\" empty or null");
        }

        if (intent != null && intent.isEmpty()) {
            LOG.debug("Parameter using intent: " + intent);
            this.intent = intent;
        } else {
            LOG.debug("Parameter \"intent\" empty or null, using default intent: " + DEFAULT_INTENT);
            this.intent = DEFAULT_INTENT;
        }
        this.kind = kind;

        return this;
    }

    @Override
    public ShadowBroker confirm() {

        return new ShadowBroker(context, kind, intent, resourceButler);
    }

}
