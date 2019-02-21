package com.evolveum.midpoint.midcredible.framework.common.focuscommons;

import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.common.By;
import com.evolveum.midpoint.midcredible.framework.common.Search;

public class SearchShadow implements Search {
    private final ResourceButler resourceButler;
    private final Context context;

    public SearchShadow(ResourceButler resourceButler, Context context) {
        this.resourceButler = resourceButler;
        this.context = context;
    }

    @Override
    public ByShadowParameters by() {
        return new ByShadowParameters(this, resourceButler, context);
    }


}
