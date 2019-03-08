package com.evolveum.midpoint.midcredible.framework.cmd;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import java.io.File;

/**
 * Created by Viliam Repan (lazyman).
 */
@Parameters(resourceBundle = "messages", commandDescriptionKey = "csvprinter")
public class CsvPrinterOptions {

    public static final String P_PATH = "-p";
    public static final String P_PATH_LONG = "--path";



    @Parameter(names = {P_PATH,P_PATH_LONG}, descriptionKey = "csvprinter.path")
    private File path;

    private boolean printEqual;

    public File getPath() {
        return path;
    }

    public void setPath(File path) {
        this.path = path;
    }

    public boolean isPrintEqual() {
        return printEqual;
    }

    public void setPrintEqual(boolean printEqual) {
        this.printEqual = printEqual;
    }
}
