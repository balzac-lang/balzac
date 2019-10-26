/*
 * Copyright 2019 Nicola Atzei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package xyz.balzaclang.lib.utils;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;

public class TablePrinter {

    private final int nColumns;
    private String title;
    private String[] header;
    private List<String[]> valuesPerLine = new ArrayList<>();
    private Map<Integer,Integer> maxColLength = new TreeMap<>();
    private String rowPrefix = " ";
    private int rowPrefixSize = 1;
    private String rowSuffix = " ";
    private int rowSuffixSize = 1;
    private String valueSeparator = " ";
    private int valueSeparatorSize = 6;
    private String noValueRow;


    public TablePrinter(String title, int nColumns) {
        this(nColumns, title, new String[]{}, "no values");
    }

    public TablePrinter(String title, String[] header) {
        this(header.length, title, header, "no values");
    }

    public TablePrinter(String[] header, String noValueRow) {
        this(header.length, "", header, noValueRow);
    }

    public TablePrinter(String title, String[] header, String noValueRow) {
        this(header.length, title, header, noValueRow);
    }

    private TablePrinter(int nColumns, String title, String[] header, String noValueRow) {
        this.nColumns = nColumns;
        this.title = title;
        this.noValueRow = noValueRow;
        this.header = header;
        for (int i=0; i<nColumns; i++) {
            this.maxColLength.putIfAbsent(i, header.length==nColumns? header[i].length():0);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getRowPrefix() {
        return rowPrefix;
    }

    public void setRowPrefix(String rowPrefix) {
        this.rowPrefix = rowPrefix;
    }

    public int getRowPrefixSize() {
        return rowPrefixSize;
    }

    public void setRowPrefixSize(int rowPrefixSize) {
        this.rowPrefixSize = rowPrefixSize;
    }

    public String getRowSuffix() {
        return rowSuffix;
    }

    public void setRowSuffix(String rowSuffix) {
        this.rowSuffix = rowSuffix;
    }

    public int getRowSuffixSize() {
        return rowSuffixSize;
    }

    public void setRowSuffixSize(int rowSuffixSize) {
        this.rowSuffixSize = rowSuffixSize;
    }

    public String getValueSeparator() {
        return valueSeparator;
    }

    public void setValueSeparator(String valueSeparator) {
        this.valueSeparator = valueSeparator;
    }

    public int getValueSeparatorSize() {
        return valueSeparatorSize;
    }

    public void setValueSeparatorSize(int valueSeparatorSize) {
        this.valueSeparatorSize = valueSeparatorSize;
    }

    public String getNoValueRow() {
        return noValueRow;
    }

    public void setNoValueRow(String noValueRow) {
        this.noValueRow = noValueRow;
    }

    public String[] getHeader() {
        return header;
    }

    public void addRow(Object... values) {
        addRow(Arrays.stream(values).map(Object::toString).toArray(String[]::new));
    }

    public void addRow(String... values) {
        checkArgument(values.length<=nColumns);
        for (int i=0; i<values.length; i++) {
            String v = values[i];
            if (maxColLength.get(i) < v.length())
                this.maxColLength.put(i, v.length());
        }
        this.valuesPerLine.add(values);
    }

    private void printTitle(StringBuilder sb) {
        if (!title.isEmpty()) {
            sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
            sb.append(title);
            sb.append("\n");
        }
    }

    private void printNoValues(StringBuilder sb) {
        sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
        sb.append(noValueRow);
        sb.append("\n");
    }

    private void printLine(StringBuilder sb, char ch) {
        int size = maxColLength.values().stream().reduce(0, Integer::sum)
                + rowPrefixSize
                + rowSuffixSize
                + valueSeparatorSize*(nColumns-1);
        sb.append(StringUtils.repeat(ch, size));
        sb.append("\n");
    }

    private void printRow(StringBuilder sb, String[] values) {
        sb.append(StringUtils.repeat(rowPrefix, rowPrefixSize));
        for (int col=0; col<values.length; col++) {
            sb.append(StringUtils.rightPad(values[col], maxColLength.get(col)));
            if (col!=values.length-1)
                sb.append(StringUtils.repeat(valueSeparator, valueSeparatorSize));
        }
        sb.append(StringUtils.repeat(rowSuffix, rowSuffixSize));
        sb.append("\n");
    }

    private void printHeader(StringBuilder sb) {
        if (header.length>0 && !valuesPerLine.isEmpty()) {
            printRow(sb, header);
            printLine(sb, '-');
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        printTitle(sb);
        printLine(sb, '=');
        printHeader(sb);

        if (valuesPerLine.isEmpty()) {
            printNoValues(sb);
        }
        else {
            for (int row=0; row<valuesPerLine.size(); row++) {
                String[] values = this.valuesPerLine.get(row);
                printRow(sb, values);
            }
        }

        printLine(sb, '=');
        return sb.toString();
    }
}
