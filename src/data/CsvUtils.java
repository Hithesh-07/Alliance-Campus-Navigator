package data;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Robust utility for reading and writing CSV files with UTF-8 encoding.
 * Supports quoted fields, internal quotes escaped as '""', embedded commas, and whitespace trimming.
 */
public final class CsvUtils {

    private CsvUtils() {}

    /**
     * Reads a CSV file into a list of row values.
     * Skips blank lines.
     */
    public static List<List<String>> readCsv(File file) throws IOException {
        List<List<String>> rows = new ArrayList<>();
        if (!file.exists() || file.length() == 0) {
            return rows;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {
            
            String line;
            StringBuilder multiLineBuffer = new StringBuilder();
            boolean insideQuote = false;

            while ((line = reader.readLine()) != null) {
                // Check if we are accumulating a multi-line quoted field
                if (insideQuote) {
                    multiLineBuffer.append("\n").append(line);
                    if (countQuotes(line) % 2 != 0) {
                        insideQuote = false;
                        rows.add(parseLine(multiLineBuffer.toString()));
                        multiLineBuffer.setLength(0);
                    }
                } else {
                    int quoteCount = countQuotes(line);
                    if (quoteCount % 2 != 0) {
                        insideQuote = true;
                        multiLineBuffer.append(line);
                    } else {
                        if (!line.trim().isEmpty()) {
                            rows.add(parseLine(line));
                        }
                    }
                }
            }
        }
        return rows;
    }

    private static int countQuotes(String text) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == '"') {
                count++;
            }
        }
        return count;
    }

    /**
     * Parses a single CSV record line into column values according to RFC 4180.
     */
    public static List<String> parseLine(String line) {
        List<String> values = new ArrayList<>();
        if (line == null) {
            return values;
        }

        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        int len = line.length();

        for (int i = 0; i < len; i++) {
            char c = line.charAt(i);

            if (c == '"') {
                if (inQuotes && i + 1 < len && line.charAt(i + 1) == '"') {
                    // Escaped quote: "" -> "
                    sb.append('"');
                    i++;
                } else {
                    // Toggle quote mode
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(sb.toString().trim());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        values.add(sb.toString().trim());

        return values;
    }

    /**
     * Writes records to a CSV file safely in UTF-8 format.
     */
    public static void writeCsv(File file, String[] headers, List<String[]> rows) throws IOException {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
            
            if (headers != null && headers.length > 0) {
                writeRow(writer, headers);
            }

            if (rows != null) {
                for (String[] row : rows) {
                    writeRow(writer, row);
                }
            }
        }
    }

    private static void writeRow(BufferedWriter writer, String[] values) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                sb.append(',');
            }
            sb.append(escapeValue(values[i]));
        }
        sb.append(System.lineSeparator());
        writer.write(sb.toString());
    }

    private static String escapeValue(String val) {
        if (val == null) {
            return "";
        }
        boolean mustQuote = val.contains(",") || val.contains("\"") || val.contains("\n") || val.contains("\r");
        if (!mustQuote) {
            return val;
        }
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}
