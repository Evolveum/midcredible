package com.evolveum.midpoint.midcredible.framework;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import com.evolveum.midpoint.midcredible.framework.cmd.Action;
import com.evolveum.midpoint.midcredible.framework.cmd.BaseOptions;
import com.evolveum.midpoint.midcredible.framework.cmd.Command;
import org.apache.commons.io.FileUtils;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {
        new Main().run(args);

        // Only the first argument is Valid

//        String comparator = args[0];
//
//        ComparatorFactory cf = new ComparatorFactory();
//        ComparisonParent comparison= cf.produceComparator(args[0]);
//        comparison.compare(false);
    }

    protected void run(String[] args) {
        JCommander jc = setupCommandLineParser();

        System.err.println("vilko");

        try {
            jc.parse(args);
        } catch (ParameterException ex) {
            System.err.println(ex.getMessage());
            return;
        }

        String parsedCommand = jc.getParsedCommand();

        BaseOptions base = getOptions(jc, BaseOptions.class);

        if (base.isVersion()) {
            try {
                Path path = Paths.get(Main.class.getResource("/version").toURI());
                System.out.println("asdf" + path);
                String version = FileUtils.readFileToString(path.toFile(), StandardCharsets.UTF_8);
                System.out.println(version);
            } catch (Exception ex) {
            }
            return;
        }

        if (base.isHelp() || parsedCommand == null) {
            printHelp(jc, parsedCommand);
            return;
        }

        try {
            Action action = Command.createAction(parsedCommand);

            if (action == null) {
                System.err.println("Action for command '" + parsedCommand + "' not found");
                return;
            }

            Object options = jc.getCommands().get(parsedCommand).getObjects().get(0);
            action.init(options);
            action.execute();
        } catch (Exception ex) {
            handleException(base, ex);
        }
    }

    private void handleException(BaseOptions opts, Exception ex) {
        if (!opts.isSilent()) {
            System.err.println("Unexpected exception occurred (" + ex.getClass() + "), reason: " + ex.getMessage());
        }

        if (opts.isVerbose()) {
            String stack = printStackToString(ex);

            System.err.print("Exception stack trace:\n" + stack);
        }
    }

    private String printStackToString(Exception ex) {
        if (ex == null) {
            return null;
        }

        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));

        return writer.toString();
    }

    private <T> T getOptions(JCommander jc, Class<T> type) {
        List<Object> objects = jc.getObjects();
        for (Object object : objects) {
            if (type.equals(object.getClass())) {
                return (T) object;
            }
        }

        return null;
    }

    private void printHelp(JCommander jc, String parsedCommand) {
        if (parsedCommand == null) {
            jc.usage();
        } else {
            jc.usage(parsedCommand);
        }
    }

    private JCommander setupCommandLineParser() {
        BaseOptions base = new BaseOptions();

        JCommander.Builder builder = JCommander.newBuilder()
                .expandAtSign(false)
                .addObject(base);

        for (Command cmd : Command.values()) {
            builder.addCommand(cmd.getCommandName(), cmd.createOptions());
        }

        JCommander jc = builder.build();
        jc.setProgramName("java [-Dloader.path=<jdbc_driver_jar_path>] -jar midcredible.jar");
        jc.setColumnSize(150);

        return jc;
    }
}