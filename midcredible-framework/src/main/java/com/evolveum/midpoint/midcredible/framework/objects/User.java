package com.evolveum.midpoint.midcredible.framework.objects;

import com.evolveum.midpoint.midcredible.framework.Context;
import com.evolveum.midpoint.midcredible.framework.common.Searchable;
import com.evolveum.midpoint.midcredible.framework.common.focuscommons.SearchFocus;

public class User implements Searchable {

    @Override
    public SearchFocus search(Context ctx) {
        return new SearchFocus(ctx);
    }
}
