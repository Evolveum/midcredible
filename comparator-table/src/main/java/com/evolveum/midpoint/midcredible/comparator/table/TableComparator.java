package com.evolveum.midpoint.midcredible.comparator.table;

import com.evolveum.midpoint.midcredible.comparator.common.*;
import com.evolveum.midpoint.midcredible.comparator.common.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import javax.sql.DataSource;
import java.io.IOException;
import java.sql.*;
import java.util.*;


public class TableComparator {

    private DataSource newResource;
    private DataSource oldResource;

    private static final Logger LOG = LoggerFactory.getLogger(TableComparator.class);
    private static final StatusLogger statusLogger = new StatusLogger();

    private CompareTableOptions options;

    public TableComparator(CompareTableOptions options) {
        this.options = options;
    }

    private void setupDataSources() {

        JdbcUtil util = new JdbcUtil();

        oldResource = util.setupDataSource(options.getOldJdbcUrl(), options.getOldUsername()
                , options.getOldPassword(), options.getJdbcDriver());

        newResource = util.setupDataSource(options.getNewJdbcUrl(), options.getNewUsername()
                , options.getNewPassword(), options.getJdbcDriver());

    }

    public void execute() throws SQLException, IOException, ScriptException, IllegalAccessException, InstantiationException {
        try {
            setupDataSources();
            executeComparison(setupComparator(), options.getCompareAttributeValues());
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
        reportPrinter.setOutPath(options.getCsvPrinterOptions().getPath().toString());

        reportPrinter.setPrintEqual(options.getCsvPrinterOptions().isPrintEqual());

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

            for (int i = 1; i <= columns; i++) {

                attributeList.add(md.getColumnName(i));
            }

        } catch (SQLException e) {
            LOG.error("An sql exception has occurred: " + e.getLocalizedMessage());
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

                            oldRow.setChanged(State.EQUAL);
                            reportPrinter.printCsvRow(attributeList, oldRow);
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
                statusLogger.printStatus("Processed number of rows: " ,noOfRows);
            }

            while (newRs.next()) {
                newRow = createIdentityFromRow(newRs, comparator.buildIdentifier(createMapFromRow(newRs)));
                newRow.setChanged(State.NEW_AFTER_OLD);
                reportPrinter.printCsvRow(attributeList, newRow);
                noOfRows++;
                statusLogger.printStatus("Processed number of rows: " ,noOfRows);
            }
        } catch (SQLException e) {
            LOG.error("Sql exception while iterating trough result set " + e.getLocalizedMessage());
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

        for (int i = 1; i <= columns; i++) {

            String colName = md.getColumnName(i);

            Attribute attr = new Attribute(colName);
            Object object = rs.getObject(i);
            attr.setInitialSingleValue(object);


            if (object != null) {

                LOG.trace("Setting up attribute: " + colName + " with value: " + object.toString());
            } else {

                LOG.trace("Setting up attribute: " + colName + " with value: [NULL]");
            }

            Map map = entity.getAttrs();
            map.put(colName, attr);
            entity.setAttrs(map);
        }

        return entity;
    }

    private Comparator setupComparator() throws IOException, ScriptException, IllegalAccessException, InstantiationException {
        return GroovyUtils.createTypeInstance(Comparator.class, options.getCompareScriptPath().getPath());
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
        for (int i = 1; i <= columns; i++) {
            map.put(new Label(md.getColumnName(i), i), rs.getObject(i));
        }

        return map;
    }

}
