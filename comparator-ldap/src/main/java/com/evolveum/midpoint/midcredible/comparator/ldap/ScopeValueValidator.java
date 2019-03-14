package com.evolveum.midpoint.midcredible.comparator.ldap;

import com.beust.jcommander.IValueValidator;
import com.beust.jcommander.ParameterException;

import java.util.Arrays;

/**
 * Created by Viliam Repan (lazyman).
 */
public class ScopeValueValidator implements IValueValidator<String> {

    private static final String[] ALLOWED_VALUES = new String[]{"one", "sub", "base"};

    @Override
    public void validate(String name, String value) throws ParameterException {
        if (!Arrays.asList(ALLOWED_VALUES).contains(value)) {
            throw new ParameterException("Only values allowed: " + Arrays.toString(ALLOWED_VALUES));
        }
    }
}
