package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.FilterTree;
import com.evolveum.midpoint.midcredible.framework.util.structural.Identity;
import com.evolveum.midpoint.midcredible.framework.util.structural.ResultSet;

public class Comparator {

    private final ResourceButler producedResource;
    private ResourceButler analyzedResource;

    public Comparator(ResourceButler produced) {

        producedResource = produced;
    }

    public QueryBuilder all(ResourceButler analyzed) {

        analyzedResource = analyzed;

        return new QueryBuilder(this);
    }

    public QueryBuilder filter(ResourceButler analyzed, String nativeFilter) {

        analyzedResource = analyzed;

        return new QueryBuilder(this, nativeFilter);
    }

    public QueryBuilder filter(ResourceButler analyzed, FilterTree filterTree) {

        analyzedResource = analyzed;

        return new QueryBuilder(filterTree, this);
    }

    protected ResultSet compare(Identity produced, Identity compared) {

        return null;
    }

    public ResourceButler getProducedResource() {

        return producedResource;
    }

    public ResourceButler getAnalyzedResource() {

        return analyzedResource;
    }
}
