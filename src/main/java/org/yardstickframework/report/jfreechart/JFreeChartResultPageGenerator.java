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

package org.yardstickframework.report.jfreechart;

import org.yardstickframework.writers.*;

import java.io.*;
import java.text.*;
import java.util.*;

import static org.yardstickframework.report.jfreechart.JFreeChartGenerationMode.*;
import static org.yardstickframework.report.jfreechart.JFreeChartGraphPlotter.*;

/**
 * Generates html pages with resulted graphs built by JFreeChart framework.
 */
public class JFreeChartResultPageGenerator {
    /** */
    public static final NumberFormat NUMBER_INSTANCE = NumberFormat.getNumberInstance();

    /** */
    static {
        NUMBER_INSTANCE.setMaximumFractionDigits(2);
        NUMBER_INSTANCE.setMinimumFractionDigits(2);
    }

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

            int i = folder.getName().lastIndexOf('-');

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
                errorHelp("Incorrect file name: " + file.getAbsolutePath());

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
     * @param fileMap Files.
     * @param outFolder Output folder.
     * @param args Arguments.
     * @param infoMap Map with additional plot info.
     */
    private static void generateHtml(Date testTime, Map<String, List<File>> fileMap, File outFolder,
        JFreeChartGraphPlotterArguments args, Map<String, List<JFreeChartPlotInfo>> infoMap) {
        File outFile = new File(outFolder, "Results.html");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)))) {
            writeLine(bw, "<!DOCTYPE html>");
            writeLine(bw, "<html lang=\"en\">");
            writeLine(bw, "<head>");
            writeLine(bw, "<meta charset=\"utf-8\">");
            writeLine(bw, "<meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">");
            writeLine(bw, "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">");
            writeLine(bw, "<link rel=\"stylesheet\" " +
                "href=\"http://netdna.bootstrapcdn.com/bootstrap/3.1.1/css/bootstrap.min.css\">");
            writeLine(bw, "<link rel=\"stylesheet\" " +
                "href=\"http://netdna.bootstrapcdn.com/font-awesome/4.1.0/css/font-awesome.min.css\">");

            writeLine(bw, "<script src=\"http://code.jquery.com/jquery-1.11.0.min.js\"></script>");
            writeLine(bw, "<script src=\"http://netdna.bootstrapcdn.com/bootstrap/3.1.1/js/bootstrap.min.js\"></script>");
            writeLine(bw, "</head>");
            writeLine(bw, "<body>");

            writeLine(bw, "<div class=\"container-fluid\">");
            writeLine(bw, "<h1>Yardstick</h1>");

            JFreeChartGenerationMode mode = generationMode(fileMap, infoMap);

            String modeAsString = mode == null || mode == STANDARD ? "" :
                mode.name().charAt(0) + mode.name().substring(1, mode.name().length()).toLowerCase() + ' ';

            String timeAsString = testTime == null ? "" : "<small> on " + testTime + "</small>";

            writeLine(bw, "<h2>Benchmark " + modeAsString + "Results" + timeAsString + "</h2>");

            Iterator<List<File>> iter = fileMap.values().iterator();

            if (iter.hasNext()) {
                File f = iter.next().get(0);

                List<JFreeChartPlotInfo> list = infoMap.get(f.getAbsolutePath());

                if (list != null) {
                    writeLine(bw, "<table class=\"table\" style=\"width:auto;\">");
                    writeLine(bw, "<thead><tr><th>Color</th><th>Benchmark</th><th>Config</th></tr></thead>");
                    writeLine(bw, "<tbody>");

                    for (JFreeChartPlotInfo info : list) {
                        String[] arr = info.name().split("-");

                        String b = arr.length >= 2 ? arr[2] : "";

                        writeLine(bw, "<tr>");
                        writeLine(bw, "<td><i style=\"color:#" + info.color() + ";\" class=\"fa fa-square\"></i></td>");
                        writeLine(bw, "<td>" + b + "</td>");
                        writeLine(bw, "<td>" + info.name() + "</td>");
                        writeLine(bw, "</tr>");
                    }

                    writeLine(bw, "</tbody>");
                    writeLine(bw, "</table>");
                }
            }

            int id = 0;

            for (Map.Entry<String, List<File>> entry : fileMap.entrySet()) {
                List<File> files = entry.getValue();

                int columnCount = args.chartColumns();

                writeLine(bw, "<div class=\"panel panel-default\">");
                writeLine(bw, "<div class=\"panel-heading\"><h2 class=\"panel-title\">" + entry.getKey() + "</h2></div>");
                writeLine(bw, "<div class=\"panel-body\">");

                for (int start = 0; start < files.size(); start += columnCount) {
                    int end = Math.min(start + columnCount, files.size());

                    List<File> sublist = files.subList(start, end);

                    writeLine(bw, "<div class=\"row\">");

                    for (File file : sublist) {
                        writeLine(bw, "<div class=\"col-md-3\">");
                        writeLine(bw, "<a data-toggle=\"modal\" data-target=\"#" + id + "\" href=\"#\"><img src=\"" +
                            file.getName() + "\" class=\"img-thumbnail\"/></a>");

                        writeLine(bw, "<div class=\"modal\" id=\"" + id +
                            "\" tabindex=\"-1\" role=\"dialog\" aria-hidden=\"true\">");
                        writeLine(bw, "<div class=\"modal-dialog modal-lg\">");
                        writeLine(bw, "<div class=\"modal-content\">");
                        writeLine(bw, "<div class=\"modal-body text-center\">");
                        writeLine(bw, "<img src=\"" + file.getName() + "\" class=\"img-thumbnail\"/>");
                        writeLine(bw, "<p>&nbsp;</p>");
                        buildGraphDetailTable(infoMap, bw, file);
                        writeLine(bw, "</div>");
                        writeLine(bw, "<div class=\"modal-footer\">");
                        writeLine(bw, "<button type=\"button\" class=\"btn btn-primary\" " +
                            "data-dismiss=\"modal\">Close</button>");
                        writeLine(bw, "</div>");
                        writeLine(bw, "</div>");
                        writeLine(bw, "</div>");
                        writeLine(bw, "</div>");

                        buildGraphDetailTable(infoMap, bw, file);

                        writeLine(bw, "</div>");

                        id++;
                    }

                    writeLine(bw, "</div>");
                }

                writeLine(bw, "</div>");
                writeLine(bw, "</div>");
            }

            writeLine(bw, "</div>");

            writeLine(bw, "</body>");
            writeLine(bw, "</html>");

            println("Html file is generated: " + outFile.getAbsolutePath());
            println("");
        } catch (Exception e) {
            errorHelp("Exception is raised during file processing: " + outFile.getAbsolutePath(), e);
        }
    }

    /**
     * @param infoMap Info map.
     * @param bw Buffered writer.
     * @param file File.
     * @throws IOException If failed.
     */
    private static void buildGraphDetailTable(Map<String, List<JFreeChartPlotInfo>> infoMap,
        BufferedWriter bw, File file) throws IOException {
        writeLine(bw, "<table class=\"table table-condensed\">");
        writeLine(bw, "<thead>");
        writeLine(bw, "<tr>");
        writeLine(bw, "<th></th>");
        writeLine(bw, "<th class=\"text-left\">Avg</th>");
        writeLine(bw, "<th class=\"text-left\">Min</th>");
        writeLine(bw, "<th class=\"text-left\">Max</th>");
        writeLine(bw, "<th class=\"text-left\">SD</th>");
        writeLine(bw, "</tr>");
        writeLine(bw, "</thead>");
        writeLine(bw, "<tbody>");

        List<JFreeChartPlotInfo> list = infoMap.get(file.getAbsolutePath());

        if (list != null) {
            for (JFreeChartPlotInfo info : list) {
                writeLine(bw, "<tr>");

                writeLine(bw, "<td><i style=\"color:#" + info.color() + ";\" class=\"fa fa-square\"></i></td>");
                writeValueToTable(bw, info.average());
                writeValueToTable(bw, info.minimum());
                writeValueToTable(bw, info.maximum());
                writeValueToTable(bw, info.standardDeviation());

                writeLine(bw, "</tr>");
            }
        }

        writeLine(bw, "</tbody>");
        writeLine(bw, "</table>");
    }

    /**
     * @param fileMap File map.
     * @param infoMap Info map.
     * @return Generation mode.
     */
    private static JFreeChartGenerationMode generationMode(Map<String, List<File>> fileMap, Map<String,
        List<JFreeChartPlotInfo>> infoMap) {
        Iterator<List<File>> iter = fileMap.values().iterator();

        if (iter.hasNext()) {
            File f = iter.next().get(0);

            List<JFreeChartPlotInfo> list = infoMap.get(f.getAbsolutePath());

            if (list != null && !list.isEmpty())
                return list.get(0).mode();
        }

        return null;
    }

    /**
     * @param bw Buffered writer.
     * @param val Value.
     * @throws IOException If failed.
     */
    private static void writeValueToTable(BufferedWriter bw, double val) throws IOException {
        writeLine(bw, "<td class=\"text-left\">" + NUMBER_INSTANCE.format(val) + "</td>");
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
