package com.evolveum.midpoint.midcredible.framework.util;

public enum State {

    /**
     * Used when rows are equal by any means
     */
    EQUAL("="),

    /**
     * Used when rows are not equal and old row is "before" new, when comparing
     * columns that were used for ordering result sets
     */
    OLD_BEFORE_NEW("-"),

    /**
     * Used when rows are not equal and old row is "after" new, when comparing
     * columns that were used for ordering result sets
     */
    OLD_AFTER_NEW("+"),

    /**
     * Used when rows are not equal and new row is "after" old after iterating through the last old row, when comparing
     * columns that were used for ordering result sets
     */
    NEW_AFTER_OLD("+"),

    /**
     * User if two rows are equal (identifiers are the same) and a difference in attribute
     * values was detected
     */
    MODIFIED("!");

    private String character;

    State(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }


}
