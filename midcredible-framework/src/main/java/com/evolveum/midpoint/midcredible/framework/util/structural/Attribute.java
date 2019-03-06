package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.Diff;

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
}
