package com.evolveum.midpoint.midcredible.comparator.common;

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
