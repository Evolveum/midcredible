package com.evolveum.midpoint.midcredible.comparator.common;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * Created by Viliam Repan (lazyman).
 */
public class GroovyUtils {

    public static <T> T createTypeInstance(Class<T> clazz, String filePath, Object... constructorArguments)
            throws IOException, ScriptException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        File file = new File(filePath);
        String script = FileUtils.readFileToString(file, StandardCharsets.UTF_8);

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("groovy");

        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) engine;
        gse.compile(script);

        Class<T> type = null;

        GroovyClassLoader gcl = gse.getClassLoader();
        for (Class c : gcl.getLoadedClasses()) {
            if (clazz.isAssignableFrom(c)) {
                type = c;
                break;
            }
        }

        if (type == null) {
            throw new IllegalStateException("Couldn't find comparator class that is assignable from " + clazz.getName()
                    + ", available classes: " + Arrays.toString(gcl.getLoadedClasses()));
        }

        if (constructorArguments.length == 0) {
            return type.newInstance();
        }

        Class[] types = new Class[constructorArguments.length];
        for (int i = 0; i < constructorArguments.length; i++) {
            types[i] = constructorArguments[i] != null ? constructorArguments[i].getClass() : null;
        }

        Constructor<T> m = type.getConstructor(types);
        return m.newInstance(constructorArguments);
    }
}
