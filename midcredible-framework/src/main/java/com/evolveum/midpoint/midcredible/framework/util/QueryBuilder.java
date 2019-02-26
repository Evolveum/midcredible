package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.FilterTree;
import com.evolveum.midpoint.midcredible.framework.util.structural.Statistics;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

import java.util.List;
import java.util.Map;

public class QueryBuilder {


    private final ComparatorImpl comparatorImpl;
    private final FilterTree filterTree;
    private final String nativeFilter;
    private String query;
    private Map<String, Object> operationalAttributes;

    public QueryBuilder(ComparatorImpl comparatorImpl) {

        this(null, comparatorImpl);

    }

    public QueryBuilder(ComparatorImpl comparatorImpl, String nativeFilter) {

        this.filterTree = null;
        this.comparatorImpl = comparatorImpl;
        this.nativeFilter = nativeFilter;
    }

    public QueryBuilder(FilterTree filterTree, ComparatorImpl comparatorImpl) {
        this.filterTree = filterTree;
        this.comparatorImpl = comparatorImpl;
        this.nativeFilter = null;
    }


    protected String getQuery() {

        return this.query;
    }

    protected void produceQuery(ResourceButler butler, List<ShadowType> shadows) {
        //TODO
        this.query = null;
    }

    protected Map<String, Object> getOperationalAttributes() {

        return this.operationalAttributes;
    }

    public Statistics statistics() {

        //TODO
        if (nativeFilter != null) {

            return new Statistics(comparatorImpl, nativeFilter);
        } else if (filterTree != null) {

            return new Statistics(filterTree, comparatorImpl);
        } else {

            return new Statistics(comparatorImpl);
        }

    }

}
