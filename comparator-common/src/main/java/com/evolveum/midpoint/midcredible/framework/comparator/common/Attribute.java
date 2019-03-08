package com.evolveum.midpoint.midcredible.framework.comparator.common;

import java.util.*;

public class Attribute {

    private String name;

    private Map<Diff, Collection<Object>> values = new HashMap<>();

    public Attribute(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Diff, Collection<Object>> getValues() {
        return values;
    }

    public void setInitialSingleValue(Object value) {
        this.values.put(Diff.NONE, Arrays.asList(value));
    }

    public void setInitialMultiValue(List<Object> values) {
        this.values.put(Diff.NONE, values);
    }

    public void setValues(Map<Diff, Collection<Object>> values) {
        this.values = values;
    }

    public void addValue(Diff diff, Object value) {
        Collection values = this.values.get(diff);
        if (values == null) {
            values = new ArrayList();
            this.values.put(diff, values);
        }

        values.add(value);
    }
}
