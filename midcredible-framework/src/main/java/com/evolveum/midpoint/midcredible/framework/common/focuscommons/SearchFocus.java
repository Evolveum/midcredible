package com.evolveum.midpoint.midcredible.framework.common.focuscommons;

import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.common.Search;

public class SearchFocus implements Search {

    private final Context context;

    public SearchFocus(Context ctx) {

        this.context = ctx;
    }

    @Override
    public ByFocusParameters by() {
        return new ByFocusParameters(null);
    }

    public SearchShadow shadows(ResourceButler resourceButler) {

        return new SearchShadow(resourceButler, context);
    }
}

