package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.comparator.common.Attribute;
import com.evolveum.midpoint.midcredible.comparator.common.State;

import java.util.List;

public class Outcome {

    private State generalState;
    private List<Attribute> attributes;

    public Outcome(State state, List<Attribute> attributes) {

        this.generalState = state;
        this.attributes = attributes;

    }

    public State getGeneralState() {

        return this.generalState;
    }

//    public List<Attribute> getDifferences() {
//
//        if (generalState == State.EQUAL) {
//
//            return null;
//        } else {
//            List<Attribute> differences = new ArrayList<>();
//
//            for (Attribute attribute : attributes) {
//                if (attribute.isChanged()) {
//
//                    differences.add(attribute);
//                }
//            }
//            return differences;
//        }
//    }
}
