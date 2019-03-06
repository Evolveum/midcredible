package com.evolveum.midpoint.midcredible.framework.util;

// This main class is used to execute the code as a repackaged "fat" jar if needed

public class Main {

    public static void main(String[] args) throws Exception {
// Only the first argument is Valid
        String comparator = args[0];

        ComparatorFactory cf = new ComparatorFactory();
        ComparisonParent comparison= cf.produceComparator(args[0]);
        comparison.compare(false);
    }

}
