package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.ResourceButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.FilterTree;
import com.evolveum.midpoint.midcredible.framework.util.structural.Statistics;
import com.evolveum.midpoint.xml.ns._public.common.common_3.ShadowType;

import java.util.List;
import java.util.Map;

public class QueryBuilder {


    private final Comparator comparator;
    private final FilterTree filterTree;
    private final String nativeFilter;
    private String query;
    private Map<String, Object> operationalAttributes;

    public QueryBuilder(Comparator comparator) {

        this(null, comparator);

    }

    public QueryBuilder(Comparator comparator, String nativeFilter) {

        this.filterTree = null;
        this.comparator = comparator;
        this.nativeFilter = nativeFilter;
    }

    public QueryBuilder(FilterTree filterTree, Comparator comparator) {
        this.filterTree = filterTree;
        this.comparator = comparator;
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

            return new Statistics(comparator, nativeFilter);
        } else if (filterTree != null) {

            return new Statistics(filterTree, comparator);
        } else {

            return new Statistics(comparator);
        }

    }

}
