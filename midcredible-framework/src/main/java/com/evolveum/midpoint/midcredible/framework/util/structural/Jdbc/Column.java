package com.evolveum.midpoint.midcredible.framework.util.structural.Jdbc;

public class Column {

    private String name;
    private int index;

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

        if (index != column.index) return false;
        return name != null ? name.equals(column.name) : column.name == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + index;
        return result;
    }
}