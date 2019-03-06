package com.evolveum.midpoint.midcredible.framework.comparator;

/**
 * Created by Viliam Repan (lazyman).
 */
public class Column {

    private String name;
    private int index = -1;

    public Column(String name) {
        this.name = name;
    }

    public Column(String name, int index) {
        this.name = name;
        this.index = index;
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Column column = (Column) o;

        return name != null ? name.equals(column.name) : column.name == null;
    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name + '(' + index + ')';
    }
}
