package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.comparator.db.TableComparator;

import java.security.InvalidParameterException;

public class ComparatorFactory {

    public ComparisonParent produceComparator(String type) {

        switch (type) {

            //TODO add additional possible input

            case "DATABASE":
                return new TableComparator();

            default:
                throw new InvalidParameterException("Invalid parameter 'type' supplied: " + type);
        }
    }
}
