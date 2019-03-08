package com.evolveum.midpoint.midcredible.framework.comparator.table;

public interface ComparisonParent {

    String PROPERTIES_FILE_LOCATION = "properties.file.location";
    String COMPARATOR_LOCATION = "comparator.location";
    String OUT_CSV_FILE_LOCATION = "out.csv.file.location";
    String COMPARATOR_PRINT_EQUAL_ENTITIES = "comparator.print.equal";

    void compare(Boolean compareAttributes) throws Exception;

}
