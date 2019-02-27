package com.evolveum.midpoint.midcredible.framework.util;

public enum Diff {
    // EQUALS - This means that the values of a specific attribute are the same.
    // This will be represented with an equals character in brackets e.g. "(=)"
    EQUALS("(=)"),
    // ADD - This means that the values of a specific attribute have been modified, it represents the addition of a specific value.
    // This will be represented with a plus character in brackets e.g. "(+)"
    ADD("(+)"),
    // REMOVE - This means that the values of a specific attribute have been modified, it represents the removal of a specific value.
    // This will be represented with a minus character in brackets e.g. "(-)"
    REMOVE("(-)"),
    //NONE - This means initial state
    NONE("(!)");

    private String character;

    Diff(String character) {
        this.character = character;
    }

    public String getCharacter() {
        return character;
    }

}
