/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstick.report.jfreechart;

import org.yardstick.writers.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * Generates html pages with resulted graphs built by JFreeChart framework.
 */
public class JFreeChartResultPageGenerator {
    /**
     * Generates a page containing all charts that belong to one test run.
     *
     * @param inFolder Input folder.
     * @param args Arguments.
     * @param infoMap Map with additional plot info.
     */
    public static void generate(File inFolder, JFreeChartGraphPlotterArguments args,
        Map<String, List<JFreeChartPlotInfo>> infoMap) {
        for (File folder : folders(inFolder)) {
            Map<String, List<File>> files = files(folder.listFiles());

            if (files.isEmpty())
                continue;

            int i = folder.getName().lastIndexOf('_');

            Date testTime = null;

            if (i != -1) {
                try {
                    testTime = BenchmarkProbePointCsvWriter.FORMAT.parse(folder.getName().substring(0, i));
                }
                catch (ParseException ignored) {
                    // No-op.
                }
            }

            generateHtml(testTime, files, folder, args, infoMap);
        }
    }

    /**
     * @param folder Folder to scan for folders.
     * @return Collection of folder.
     */
    private static Collection<File> folders(File folder) {
        File[] dirs = folder.listFiles();

        if (dirs == null || dirs.length == 0)
            return Collections.emptyList();

        Collection<File> res = new ArrayList<>();

        res.add(folder);

        for (File dir : dirs) {
            if (dir.isDirectory())
                res.add(dir);
        }

        return res;
    }

    /**
     * @param files Files.
     * @return Map of files.
     */
    private static Map<String, List<File>> files(File[] files) {
        Map<String, List<File>> res = new HashMap<>();

        for (File file : files) {
            if (!file.getName().endsWith(".png"))
                continue;

            String[] tokens = file.getName().split("_");

            if (tokens.length < 3) {
                System.out.println("Incorrect file name: '" + file.getName() + "'.");

                continue;
            }

            List<File> list = res.get(tokens[1]);

            if (list == null) {
                list = new ArrayList<>();

                res.put(tokens[1], list);
            }

            list.add(file);
        }

        Comparator<File> comp = new Comparator<File>() {
            @Override public int compare(File o1, File o2) {
                return o1.getName().compareTo(o2.getName());
            }
        };

        // Sort files to have them always in the same order.
        for (List<File> list : res.values())
            Collections.sort(list, comp);

        return res;
    }

    /**
     * @param testTime Test time.
     * @param files Files.
     * @param outFolder Output folder.
     * @param args Arguments.
     * @param infoMap Map with additional plot info.
     */
    private static void generateHtml(Date testTime, Map<String, List<File>> files, File outFolder,
        JFreeChartGraphPlotterArguments args, Map<String, List<JFreeChartPlotInfo>> infoMap) {
        File outFile = new File(outFolder, "Results.html");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)))) {
            writeLine(bw, "<html>");
            writeLine(bw, "<head>");
            writeLine(bw, "</head>");
            writeLine(bw, "<body>");

            writeLine(bw, "<h1>Results" + (testTime == null ? "" : " on " + testTime) + "</h1>");
            writeLine(bw, "<br>");

            int cnt = 0;

            for (Map.Entry<String, List<File>> entry : files.entrySet()) {
                writeLine(bw, "<h2>" + entry.getKey() + "</h2>");
                writeLine(bw, "<table>");
                writeLine(bw, "<tr>");

                int imgInRow = 0;

                for (File file : entry.getValue()) {
                    if (imgInRow != 0 && imgInRow % args.chartColumns() == 0) {
                        writeLine(bw, "</tr>");
                        writeLine(bw, "</table>");
                        writeLine(bw, "<table>");
                        writeLine(bw, "<br>");
                        writeLine(bw, "<tr>");
                    }

                    writeLine(bw, "<td>");
                    writeLine(bw, "<table>");
                    writeLine(bw, "<tr>");
                    writeLine(bw, "<td>");
                    writeLine(bw, "<a href=\"javascript:showCloseLevel('id" + (cnt + 1) + "', 'id" +
                        cnt + "')\"><img src=\"" + file.getName() + "\" id=\"id" +
                        cnt + "\" width=\"400\" height=\"200\"/></a>");
                    writeLine(bw, "<a href=\"javascript:showCloseLevel('id" + cnt + "', 'id" +
                        (cnt + 1) + "')\"><img src=\"" + file.getName() + "\" id=\"id" +
                        (cnt + 1) + "\" style=\"display:none\"/></a>");
                    writeLine(bw, "</td>");
                    writeLine(bw, "</tr>");
                    writeLine(bw, "<tr>");
                    writeLine(bw, "<td>");

                    writeLine(bw, "<table border=\"1\" style=\"border:1 solid;border-collapse:collapse\">");
                    writeLine(bw, "<tr>");
                    writeLine(bw, "<th>Avg</th>");
                    writeLine(bw, "<th>Min</th>");
                    writeLine(bw, "<th>Max</th>");
                    writeLine(bw, "<th>Std Deviation</th>");
                    writeLine(bw, "</tr>");

                    for (JFreeChartPlotInfo info : infoMap.get(file.getAbsolutePath())) {
                        writeLine(bw, "<tr>");
//                        writeValueToTable(bw, "Plot", info.name());
                        writeValueToTable(bw, info.average());
                        writeValueToTable(bw, info.minimum());
                        writeValueToTable(bw, info.maximum());
                        writeValueToTable(bw, info.standardDeviation());

                        writeLine(bw, "</tr>");
                    }

                    writeLine(bw, "</tr>");
                    writeLine(bw, "</table>");

                    writeLine(bw, "</td>");
                    writeLine(bw, "</tr>");
                    writeLine(bw, "</table>");
                    writeLine(bw, "</td>");

                    cnt += 2;

                    imgInRow++;
                }

                writeLine(bw, "</tr>");
                writeLine(bw, "</table>");
            }

            writeLine(bw, "</body>");
            writeLine(bw, "</html>");
            writeLine(bw, "<script type=\"text/javascript\">");
            writeLine(bw, "function showCloseLevel(idOpen, idClose) {");
            writeLine(bw, "    document.getElementById(idOpen).style.display = \"\";");
            writeLine(bw, "    document.getElementById(idClose).style.display = \"none\";");
            writeLine(bw, "}");
            writeLine(bw, "</script>");

            System.out.println("Html file is generated: " + outFile.getAbsolutePath());
        }
        catch (Exception e) {
            System.out.println("Exception is raised during file '" + outFile + "' processing.");

            e.printStackTrace();
        }
    }

    /**
     * @param bw Buffered writer.
     * @param val Value.
     * @throws IOException If failed.
     */
    private static void writeValueToTable(BufferedWriter bw, double val) throws IOException {
        writeLine(bw, "<td>");

        writeLine(bw, String.format(Locale.US, "%.2f", val));

        writeLine(bw, "</td>");
    }

    /**
     * @param bw Buffered writer.
     * @param rowName Row name.
     * @param val Value.
     * @throws IOException If failed.
     */
    private static void writeValueToTable(BufferedWriter bw, String rowName, String val) throws IOException {
        writeLine(bw, "<tr>");
        writeLine(bw, "<td>");

        writeLine(bw, rowName + ": " + val);

        writeLine(bw, "</td>");
        writeLine(bw, "</tr>");
    }

    /**
     * @param bw Buffered writer.
     * @param line Line.
     * @throws IOException If failed.
     */
    private static void writeLine(BufferedWriter bw, String line) throws IOException {
        bw.write(line);
        bw.newLine();
    }
}
