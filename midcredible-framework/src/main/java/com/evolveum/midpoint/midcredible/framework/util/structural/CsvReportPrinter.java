package com.evolveum.midpoint.midcredible.framework.util.structural;

import com.evolveum.midpoint.midcredible.framework.util.State;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class CsvReportPrinter {

    private static final char DEFAULT_MV_SEPARATOR = ';';

    public CsvReportPrinter(){}


    public CSVPrinter setupCsvPrinter(String path) throws IOException {
        Writer writer;
        if (path != null && !path.isEmpty()) {
            File file = new File(path);
            writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
        } else {
            writer = new OutputStreamWriter(System.out, StandardCharsets.UTF_8);
        }

        CSVFormat csvFormat = setupCsvFormat();

        return csvFormat.print(writer);
    }

    private CSVFormat setupCsvFormat() {
        return CSVFormat.DEFAULT;
    }

    private void printCsvHeader(CSVPrinter printer, List <String> columnNames) throws SQLException, IOException {
        columnNames.sort(String::compareTo);
        int columns= columnNames.size();
        String[] header = new String[columns + 1];

        header[0] = "state";

        for (int i = 0; i < columns; i++) {
            header[i + 1] = columnNames.get(i);
        }

        printer.printRecord(header);
    }

    public void printCsvRow(CSVPrinter printer, List <String> attrNames, Identity identity) throws IOException {
        if (identity.getChange() == null || identity.getChange() == State.EQUAL) {
            return;
        }
        String[] row = new String[attrNames.size() + 1];
        attrNames.sort(String::compareTo);

        row[0] =  identity.getChange().getCharacter();
        attrNames.forEach(name->{
          int i =0;
           Attribute attr= identity.getAttrs().get(name);
           StringBuilder valueString=null;
            attr.getValues().forEach((diff, objects) -> {
                int count=1;
                objects.forEach(object->{

                    valueString.append( object != null ? object.toString() : "[null]");

                if(count<objects.size()){
                        valueString.append(DEFAULT_MV_SEPARATOR);
                    }
                });
            });

            row[i + 1] =valueString !=null ? valueString.toString(): "[null]";
        });

    }

//    private void printCsvRow(CSVPrinter printer, String state, ResultSet rs) throws SQLException, IOException {
//        ResultSetMetaData md = rs.getMetaData();
//        int columns = md.getColumnCount();
//
//        String[] row = new String[columns + 1];
//
//        row[0] = state;
//        for (int i = 0; i < columns; i++) {
//            Object obj = rs.getObject(i);
//            row[i + 1] = obj != null ? obj.toString() : "[null]";
//        }
//
//        printer.printRecord(row);
//    }

}
