package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.util.structural.Outcome;

import java.util.Map;

public interface Comparator {

    String query();

    Outcome compare(Map<? extends Object, Object> oldRow, Map<? extends Object, Object> newRow);

}
