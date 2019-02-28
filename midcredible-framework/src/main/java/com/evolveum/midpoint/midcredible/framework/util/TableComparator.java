package com.evolveum.midpoint.midcredible.framework.util;

import com.evolveum.midpoint.midcredible.framework.TableButler;
import com.evolveum.midpoint.midcredible.framework.util.structural.Attribute;
import com.evolveum.midpoint.midcredible.framework.util.structural.CsvReportPrinter;
import com.evolveum.midpoint.midcredible.framework.util.structural.Identity;
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
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class TableComparator {

    private final DataSource newResource;
    private final DataSource oldResource;
    private final String comparatorPath;
    private static final Logger LOG = LoggerFactory.getLogger(TableComparator.class);

    public TableComparator(TableButler newResource, DataSource OldResource, String comparatorPath) {
        this(newResource.getClient().getDataSource(), OldResource, comparatorPath);
    }

    public TableComparator(DataSource newResource, DataSource oldResource, String comparatorPath) {
        this.newResource = newResource;
        this.oldResource = oldResource;
        this.comparatorPath = comparatorPath;
    }


    public void compare(String outputFilePath, String identificator, Boolean compareAttributes) throws SQLException {
        try {
            executeComparison(setupComparator(), outputFilePath, identificator, compareAttributes);
        } catch (IOException e) {

            e.printStackTrace();
        } catch (ScriptException e) {
            LOG.error("Exception white iterating trough result set " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            LOG.error("Exception white iterating trough result set " + e.getLocalizedMessage());
            e.printStackTrace();
        } catch (InstantiationException e) {
            LOG.error("Exception white iterating trough result set " + e.getLocalizedMessage());
            e.printStackTrace();
        } finally {

            //TODO clean up

        }
    }

    protected void executeComparison(Comparator comparator, String outputFilePath, String identificator, Boolean compareAttributes) throws SQLException, IOException {

        //TODO path from property file
        CsvReportPrinter reportPrinter = new CsvReportPrinter(outputFilePath);

        ResultSet oldRs = null;
        ResultSet newRs = null;
        ResultSetMetaData md;
        List attributeList = new ArrayList();

        try {
            oldRs = createResultSet(comparator.query(), oldResource);
            newRs = createResultSet(comparator.query(), newResource);
            md = oldRs.getMetaData();

            int columns = md.getColumnCount();

            LOG.info("Number of columns fetched: "+ columns);

            for (int i = 1; i < columns; i++) {

                attributeList.add(md.getColumnName(i));
            }

        } catch (SQLException e) {
            LOG.error("An error has occurred: " +e.getLocalizedMessage());
            //TODO
           throw e;
        }

        Identity oldRow;
        Identity newRow;

        try {
            while (oldRs.next()) {
                oldRow = createIdentityFromRow(oldRs, identificator);

                if (!newRs.next()) {
                    oldRow.setChanged(State.OLD_AFTER_NEW);
                    reportPrinter.printCsvRow(attributeList, oldRow);
                    //break;
                }

                newRow = createIdentityFromRow(newRs, identificator);
                State state = comparator.compareIdentity(oldRow, newRow);
                switch (state) {
                    case EQUAL:
                        if (!compareAttributes) {
                            continue;
                        } else {
                            Identity difference = comparator.compareData(oldRow, newRow);
                            reportPrinter.printCsvRow(attributeList, difference);
                        }
                    case OLD_BEFORE_NEW:
                        // new table contains row that shouldn't be there, mark new as "+"
                        newRow.setChanged(State.OLD_BEFORE_NEW);
                        reportPrinter.printCsvRow(attributeList, newRow);
                        //printCsvRow(printer, "+", newRs);
                        break;
                    case OLD_AFTER_NEW:

                        oldRow.setChanged(State.OLD_AFTER_NEW);
                        reportPrinter.printCsvRow(attributeList, oldRow);
                        // new table misses some rows obviously, therefore old row should be marked as "-"
                        //printCsvRow(printer, "-", oldRs);
                        break;
                }
            }

            while (newRs.next()) {
                newRow = createIdentityFromRow(newRs, identificator);
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

    private Identity createIdentityFromRow(ResultSet rs, String identifier) throws SQLException {
        String id = rs.getObject(identifier).toString();

        //TODO change to trace
        LOG.info("Creating identity with the id: "+id);

        Identity identity = new Identity(id, new HashMap<>());
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();

        for (int i = 1; i < columns; i++) {

            String colName = md.getColumnName(i);
            //TODO change to trace
            LOG.info("Setting up attribute: "+ colName);

            Attribute attr = new Attribute(colName);
            Object object= rs.getObject(i);
            attr.setInitialSingleValue(object);
            //TODO change to trace
            LOG.info("Pushing object value to attribute: "+ object);

            Map map = identity.getAttrs();
            map.put(colName, attr);
            identity.setAttrs(map);
        }

//       identity.getAttrs().forEach((string,attribute)->{
//           attribute.getValues().forEach((diff,object)->{
//
//              LOG.info("The attribute "+ string +" has the values "+ object.toString());
//
//           });
//
//       });
        return identity;
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

}
