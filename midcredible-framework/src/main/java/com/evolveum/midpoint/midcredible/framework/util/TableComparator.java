package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.TableButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute;
import com.evolveum.midpoint.midcredible.framework.util.structural.Entity;
import com.evolveum.midpoint.midcredible.framework.util.structural.Label;
import groovy.lang.GroovyClassLoader;
import org.apache.commons.io.FileUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TableComparator implements DatabaseComparable {

    private DataSource newResource;
    private DataSource oldResource;
    private String comparatorPath;
    private String ourCsvFilePath;

    private static final Logger LOG = LoggerFactory.getLogger(TableComparator.class);

    public TableComparator(String properties) throws IOException {

        this(null, properties);
    }


    public TableComparator(TableButler newResource, String properties) throws IOException {

        if (newResource != null) {

            this.newResource = newResource.getClient().getDataSource();
        } else {

            fetchDataFromProperties(properties);
        }

    }

    private void fetchDataFromProperties(String propertiesPath) throws IOException {

        FileInputStream input = new FileInputStream(propertiesPath);
        Properties properties = new Properties();
        properties.load(input);

        JdbcUtil util = new JdbcUtil();
        oldResource = util.setupDataSource(properties.getProperty(JDBC_URL_OLD_RESOURCE), properties.getProperty(DATABASE_USERNAME_OLD_RESOURCE)
                , properties.getProperty(DATABASE_PASSWORD_OLD_RESOURCE), properties.getProperty(JDBC_DRIVER));

        newResource = util.setupDataSource(properties.getProperty(JDBC_URL_NEW_RESOURCE), properties.getProperty(DATABASE_USERNAME_NEW_RESOURCE)
                , properties.getProperty(DATABASE_PASSWORD_NEW_RESOURCE), properties.getProperty(JDBC_DRIVER));

        comparatorPath = properties.getProperty(COMPARATOR_LOCATION);
        ourCsvFilePath = properties.getProperty(OUT_CSV_FILE_LOCATION);
    }


    public void compare(Boolean compareAttributes) throws SQLException {
        try {
            executeComparison(setupComparator(), compareAttributes);
        } catch (IOException e) {

            e.printStackTrace();
        } catch (ScriptException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InstantiationException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            e.printStackTrace();
        } finally {

            //TODO clean up

        }
    }

    protected void executeComparison(Comparator comparator, Boolean compareAttributes) throws SQLException, IOException {
        CsvReportPrinter reportPrinter = new CsvReportPrinter(ourCsvFilePath);

        ResultSet oldRs;
        ResultSet newRs;
        ResultSetMetaData md;
        List attributeList = new ArrayList();

        try {
            oldRs = createResultSet(comparator.query(), oldResource);
            newRs = createResultSet(comparator.query(), newResource);
            md = oldRs.getMetaData();

            int columns = md.getColumnCount();

            LOG.info("Number of columns fetched: " + columns);

            for (int i = 1; i < columns; i++) {

                attributeList.add(md.getColumnName(i));
            }

        } catch (SQLException e) {
            LOG.error("An error has occurred: " + e.getLocalizedMessage());
            //TODO
            throw e;
        }


        Entity oldRow = null;
        Entity newRow;
        boolean iterateNew = true;
        boolean iterateOld = true;

        try {
            while (true) {

                if (iterateOld) {
                    if (oldRs.next()) {
                        oldRow = createIdentityFromRow(oldRs, comparator.buildIdentifier(createMapFromRow(oldRs)));
                    } else {
                        break;
                    }
                }

                if (iterateNew) {
                    if (!newRs.next()) {
                        oldRow.setChanged(State.OLD_AFTER_NEW);
                        reportPrinter.printCsvRow(attributeList, oldRow);
                        iterateOld = true;
                        //break;
                    }
                }

                newRow = createIdentityFromRow(newRs, comparator.buildIdentifier(createMapFromRow(newRs)));

                State state = comparator.compareEntity(oldRow, newRow);
                switch (state) {
                    case EQUAL:
                        if (!compareAttributes) {

                            iterateNew = true;
                            iterateOld = true;
                            continue;
                        } else {

                            Entity difference = comparator.compareData(oldRow, newRow);
                            reportPrinter.printCsvRow(attributeList, difference);

                            iterateNew = true;
                            iterateOld = true;
                        }

                        break;
                    case OLD_BEFORE_NEW:

                        // new table misses some rows obviously, therefore old row should be marked as "-"
                        oldRow.setChanged(State.OLD_BEFORE_NEW);
                        reportPrinter.printCsvRow(attributeList, oldRow);
                        //printCsvRow(printer, "-", newRs);
                        iterateNew = false;
                        iterateOld = true;
                        break;
                    case OLD_AFTER_NEW:

                        newRow.setChanged(State.OLD_AFTER_NEW);
                        reportPrinter.printCsvRow(attributeList, newRow);
                        // new table contains row that shouldn't be there, mark new as "+"
                        //printCsvRow(printer, +", oldRs);
                        iterateNew = true;
                        iterateOld = false;
                        break;
                }
            }

            while (newRs.next()) {
                newRow = createIdentityFromRow(newRs, comparator.buildIdentifier(createMapFromRow(newRs)));
                newRow.setChanged(State.OLD_BEFORE_NEW);
                reportPrinter.printCsvRow(attributeList, newRow);
            }
        } catch (SQLException e) {
            // TODO
            LOG.error("Sql exception white iterating trough result set " + e.getLocalizedMessage());
            throw e;
        } catch (IOException e) {
            //TODO
            LOG.error("IO exception white iterating trough result set " + e.getLocalizedMessage());
            throw e;
        }
    }

    private Entity createIdentityFromRow(ResultSet rs, String identifier) throws SQLException {

        //TODO change to trace
        LOG.info("Creating entity with the id: " + identifier);

        Entity entity = new Entity(identifier, new HashMap<>());
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();

        for (int i = 1; i < columns; i++) {

            String colName = md.getColumnName(i);

            Attribute attr = new Attribute(colName);
            Object object = rs.getObject(i);
            attr.setInitialSingleValue(object);
            //TODO change to trace

            LOG.info("Setting up attribute: " + colName + " with value: " + object.toString());
            Map map = entity.getAttrs();
            map.put(colName, attr);
            entity.setAttrs(map);
        }

//       entity.getAttrs().forEach((string,attribute)->{
//           attribute.getValues().forEach((diff,object)->{
//
//              LOG.info("The attribute "+ string +" has the values "+ object.toString());
//
//           });
//
//       });
        return entity;
    }

    private Comparator setupComparator() throws IOException, ScriptException, IllegalAccessException, InstantiationException {
        File file = new File(comparatorPath);
        String script = FileUtils.readFileToString(file, "utf-8");

        ScriptEngineManager engineManager = new ScriptEngineManager();
        ScriptEngine engine = engineManager.getEngineByName("groovy");

        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) engine;
        gse.compile(script);

        Class<? extends Comparator> type = null;

        GroovyClassLoader gcl = gse.getClassLoader();
        for (Class c : gcl.getLoadedClasses()) {
            if (Comparator.class.isAssignableFrom(c)) {
                type = c;
                break;
            }
        }

        if (type == null) {
            throw new IllegalStateException("Couldn't find comparatorPath class that is assignable from Comparator "
                    + ", available classes: " + Arrays.toString(gcl.getLoadedClasses()));
        }

        return type.newInstance();
    }

    private ResultSet createResultSet(String query, DataSource ds) throws SQLException {
        Connection con = ds.getConnection();

        PreparedStatement pstmt = con.prepareStatement(query);
        return pstmt.executeQuery();
    }

    private Map<Label, Object> createMapFromRow(ResultSet rs) throws SQLException {
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();

        Map<Label, Object> map = new HashMap<>();
        for (int i = 1; i < columns; i++) {
            map.put(new Label(md.getColumnName(i), i), rs.getObject(i));
        }

        return map;
    }

}
