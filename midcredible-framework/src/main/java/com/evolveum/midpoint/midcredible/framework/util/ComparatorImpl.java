package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.FilterTree;
import com.evolveum.midpoint.midcredible.framework.util.structural.Identity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Outcome;

import java.util.Map;

public class ComparatorImpl implements Comparator {
    @Override
    public String query() {
        return null;
    }

    @Override
    public State compareIdentity(Identity oldIdentity, Identity newIdentity) {
        return null;
    }

    @Override
    public Identity compareData(Identity oldIdentity, Identity newIdentity) {
        return null;
    }
//
//    private final ResourceButler producedResource;
//    private ResourceButler analyzedResource;
//
//    public ComparatorImpl(ResourceButler produced) {
//
//        producedResource = produced;
//    }
//
//    public QueryBuilder all(ResourceButler analyzed) {
//
//        analyzedResource = analyzed;
//
//        return new QueryBuilder(this);
//    }
//
//    public QueryBuilder filter(ResourceButler analyzed, String nativeFilter) {
//
//        analyzedResource = analyzed;
//
//        return new QueryBuilder(this, nativeFilter);
//    }
//
//    public QueryBuilder filter(ResourceButler analyzed, FilterTree filterTree) {
//
//        analyzedResource = analyzed;
//
//        return new QueryBuilder(filterTree, this);
//    }
//
//    protected Outcome compare(Identity produced, Identity compared) {
//
//        return null;
//    }
//
//    public ResourceButler getProducedResource() {
//
//        return producedResource;
//    }
//
//    public ResourceButler getAnalyzedResource() {
//
//        return analyzedResource;
//    }
//
//    @Override
//    public String query() {
//        return null;
//    }
//
//    @Override
//    public Outcome compare(Map<?, Object> oldRow, Map<?, Object> newRow) {
//
//      return compare(oldRow, newRow );
//    }
}
