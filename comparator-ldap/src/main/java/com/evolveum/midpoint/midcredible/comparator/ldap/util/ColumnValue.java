package com.evolveum.midpoint.midcredible.comparator.ldap.util;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ColumnValue {

    private Object value;

    private ValueState state;

    public ColumnValue(Object value, ValueState state) {
        this.value = value;
        this.state = state;
    }

    public Object getValue() {
        return value;
    }

    public ValueState getState() {
        return state;
    }

    @Override
    public String toString() {
        return value + "(" + state + ")";
    }
}
