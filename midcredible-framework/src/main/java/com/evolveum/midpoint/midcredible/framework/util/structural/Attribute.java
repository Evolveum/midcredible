package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.Diff;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Attribute {

    // TODO

    private String name;
    private Map<Diff, Collection<Object>> values;

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
        Map<Diff, Collection> valueMap = new HashMap<>();
        Collection values = new ArrayList();
        values.add(value);

        valueMap.put(Diff.NONE, values);
    }

    public void setInitialMutliValue(Object values) {
        //TODO
    }


    public void setValues(Map<Diff, Collection<Object>> values) {
        this.values = values;
    }
}
