package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.State;

import java.util.ArrayList;
import java.util.List;

public class ResultSet {

    private State generalState;
    private List<Attribute> attributes;

    public ResultSet(State state, List<Attribute> attributes) {

        this.generalState = state;
        this.attributes = attributes;

    }

    public State getGeneralState() {

        return this.generalState;
    }

    public List<Attribute> getDifferences() {

        if (generalState == State.EQUAL) {

            return null;
        } else {
            List<Attribute> differences = new ArrayList<>();

            for (Attribute attribute : attributes) {
                if (attribute.isChanged()) {

                    differences.add(attribute);
                }
            }
            return differences;
        }
    }
}
