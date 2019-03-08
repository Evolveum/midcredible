package com.evolveum.midpoint.midcredible.framework.comparator.table;

import com.evolveum.midpoint.midcredible.framework.comparator.common.*;
import com.evolveum.midpoint.midcredible.framework.comparator.common.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class TableComparator implements DatabaseComparison {

    private DataSource newResource;
    private DataSource oldResource;
    private String comparatorPath;
    private String outCsvFilePath;

    private Boolean printEqualRows;

    private static final Logger LOG = LoggerFactory.getLogger(TableComparator.class);

    private CompareTableOptions options;

    public TableComparator(CompareTableOptions options) {
        this.options = options;
    }

    //    public TableComparator() {
//
//        this(null, null);
//    }
//
//    public TableComparator(String properties) {
//
//        this(null, properties);
//    }
//
//
//    public TableComparator(TableButler newResource, String properties) {
//
//        if (newResource != null) {
//
//            this.newResource = newResource.getClient().getDataSource();
//        } else {
//
//            if (properties != null && !properties.isEmpty()) {
//                fetchDataFromProperties(properties);
//            } else {
//                fetchDataFromProperties();
//            }
//
//        }
//
//    }

    private void fetchDataFromProperties() {
        fetchDataFromProperties(null, false);
    }

    private void fetchDataFromProperties(String propertiesPath) {
        fetchDataFromProperties(propertiesPath, true);
    }

    private void fetchDataFromProperties(String propertiesPath, Boolean isFile) {
        JdbcUtil util = new JdbcUtil();
        if (isFile) {
            FileInputStream input;
            Properties properties = null;
            try {
                input = new FileInputStream(propertiesPath);

                new Properties();
                properties.load(input);
            } catch (IOException e) {
                LOG.error("Unexpected Io Exception: " + e);
            }
            oldResource = util.setupDataSource(properties.getProperty(JDBC_URL_OLD_RESOURCE), properties.getProperty(DATABASE_USERNAME_OLD_RESOURCE)
                    , properties.getProperty(DATABASE_PASSWORD_OLD_RESOURCE), properties.getProperty(JDBC_DRIVER));

            newResource = util.setupDataSource(properties.getProperty(JDBC_URL_NEW_RESOURCE), properties.getProperty(DATABASE_USERNAME_NEW_RESOURCE)
                    , properties.getProperty(DATABASE_PASSWORD_NEW_RESOURCE), properties.getProperty(JDBC_DRIVER));

            comparatorPath = properties.getProperty(ComparisonParent.COMPARATOR_LOCATION);
            outCsvFilePath = properties.getProperty(ComparisonParent.OUT_CSV_FILE_LOCATION);

            printEqualRows = Boolean.getBoolean(System.getProperty(ComparisonParent.COMPARATOR_PRINT_EQUAL_ENTITIES));
        } else {
            String propertyPath = System.getProperty(ComparisonParent.PROPERTIES_FILE_LOCATION);

            if (propertyPath != null && !propertyPath.isEmpty()) {
                fetchDataFromProperties(propertyPath, true);
            } else {

                oldResource = util.setupDataSource(System.getProperty(JDBC_URL_OLD_RESOURCE), System.getProperty(DATABASE_USERNAME_OLD_RESOURCE)
                        , System.getProperty(DATABASE_PASSWORD_OLD_RESOURCE), System.getProperty(JDBC_DRIVER));

                newResource = util.setupDataSource(System.getProperty(JDBC_URL_NEW_RESOURCE), System.getProperty(DATABASE_USERNAME_NEW_RESOURCE)
                        , System.getProperty(DATABASE_PASSWORD_NEW_RESOURCE), System.getProperty(JDBC_DRIVER));

                comparatorPath = System.getProperty(ComparisonParent.COMPARATOR_LOCATION);
                outCsvFilePath = System.getProperty(ComparisonParent.OUT_CSV_FILE_LOCATION);

                printEqualRows = Boolean.getBoolean(System.getProperty(ComparisonParent.COMPARATOR_PRINT_EQUAL_ENTITIES));
            }
        }

    }

    @Override
    public void compare(Boolean compareAttributes) throws SQLException, IOException, ScriptException, IllegalAccessException, InstantiationException {
        try {
            executeComparison(setupComparator(), compareAttributes);
        } catch (IOException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            throw e;
        } catch (ScriptException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            throw e;
        } catch (IllegalAccessException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            throw e;
        } catch (InstantiationException e) {
            LOG.error("Exception white iterating trough result set: " + e.getLocalizedMessage());
            throw e;
        } finally {

            LOG.info("Closing connection to both data sources.");

            newResource.getConnection().close();
            oldResource.getConnection().close();
        }
    }

    protected void executeComparison(Comparator comparator, Boolean compareAttributes) throws SQLException, IOException {
        CsvReportPrinter reportPrinter = new CsvReportPrinter();
        reportPrinter.setOutPath(outCsvFilePath);

        if (printEqualRows != null) {
            reportPrinter.setPrintEqual(printEqualRows);
        }

        reportPrinter.init();

        ResultSet oldRs;
        ResultSet newRs;
        ResultSetMetaData md;
        List attributeList = new ArrayList();

        try {
            oldRs = createResultSet(comparator.query(1), oldResource);
            newRs = createResultSet(comparator.query(2), newResource);
            md = oldRs.getMetaData();

            int columns = md.getColumnCount();

            LOG.info("Number of columns fetched: " + columns);

            for (int i = 1; i < columns; i++) {

                attributeList.add(md.getColumnName(i));
            }

        } catch (SQLException e) {
            LOG.error("An error has occurred: " + e.getLocalizedMessage());
            throw e;
        }


        Entity oldRow = null;
        Entity newRow;
        boolean iterateNew = true;
        boolean iterateOld = true;
        Integer noOfRows = 0;
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
                    }
                }

                newRow = createIdentityFromRow(newRs, comparator.buildIdentifier(createMapFromRow(newRs)));

                State state = comparator.compareEntity(oldRow.getId(), newRow.getId());
                switch (state) {
                    case EQUAL:
                        if (!compareAttributes) {

                            iterateNew = true;
                            iterateOld = true;
                            break;
                        } else {

                            Entity difference = comparator.compareData(oldRow, newRow);

                            if (State.MODIFIED == difference.getChange()) {

                                reportPrinter.printCsvRow(attributeList, difference);
                            }

                            iterateNew = true;
                            iterateOld = true;
                        }

                        break;
                    case OLD_BEFORE_NEW:

                        oldRow.setChanged(State.OLD_BEFORE_NEW);
                        reportPrinter.printCsvRow(attributeList, oldRow);

                        iterateNew = false;
                        iterateOld = true;
                        break;
                    case OLD_AFTER_NEW:

                        newRow.setChanged(State.OLD_AFTER_NEW);
                        reportPrinter.printCsvRow(attributeList, newRow);

                        iterateNew = true;
                        iterateOld = false;
                        break;
                }
                noOfRows++;
                System.out.println("Processed number of rows: " + noOfRows);
            }

            while (newRs.next()) {
                newRow = createIdentityFromRow(newRs, comparator.buildIdentifier(createMapFromRow(newRs)));
                newRow.setChanged(State.NEW_AFTER_OLD);
                reportPrinter.printCsvRow(attributeList, newRow);
                noOfRows++;
                System.out.println("Processed number of rows: " + noOfRows);
            }
        } catch (SQLException e) {
            LOG.error("Sql exception white iterating trough result set " + e.getLocalizedMessage());
            throw e;
        } catch (IOException e) {
            LOG.error("IO exception white iterating trough result set " + e.getMessage());
            throw e;
        } finally {
            LOG.info("Closing the output printer.");
            reportPrinter.close();
        }
    }

    private Entity createIdentityFromRow(ResultSet rs, String identifier) throws SQLException {

        LOG.trace("Creating entity with the id: " + identifier);

        Entity entity = new Entity(identifier, new HashMap<>());
        ResultSetMetaData md = rs.getMetaData();
        int columns = md.getColumnCount();

        for (int i = 1; i < columns; i++) {

            String colName = md.getColumnName(i);

            Attribute attr = new Attribute(colName);
            Object object = rs.getObject(i);
            attr.setInitialSingleValue(object);

            LOG.trace("Setting up attribute: " + colName + " with value: " + object.toString());
            Map map = entity.getAttrs();
            map.put(colName, attr);
            entity.setAttrs(map);
        }

        return entity;
    }

    private Comparator setupComparator() throws IOException, ScriptException, IllegalAccessException, InstantiationException {
        return GroovyUtils.createTypeInstance(Comparator.class, comparatorPath);
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