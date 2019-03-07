package com.evolveum.midpoint.midcredible.framework.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "compare-common")
public class CompareCommonOptions {

    public static final String P_PROPERTIES = "-p";
    public static final String P_PROPERTIES_LONG = "--properties";

    @Parameter(names = {P_PROPERTIES, P_PROPERTIES_LONG}, descriptionKey = "compare-common.properties")
    private File input;

    // todo csv output options

    public File getInput() {
        return input;
    }
}
