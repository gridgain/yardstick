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

import java.io.*;
import java.util.*;

/**
 * Generates html pages with resulted graphs built by JFreeChart framework.
 */
public class JFreeChartResultPageGenerator {
    /**
     * @param args Arguments.
     */
    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            System.out.println("Folder with graphs is not defined.");

            return;
        }

        File inFolder = new File(args[0]);

        if (!inFolder.exists()) {
            System.out.println("Input folder '" + inFolder + "' does not exist.");

            return;
        }

        Map<String, Map<String, List<File>>> files = files(inFolder.listFiles());

        for (Map.Entry<String, Map<String, List<File>>> entry : files.entrySet())
            generateHtml(entry.getKey(), entry.getValue(), inFolder);
    }

    /**
     * @param files Files.
     * @return Map of files.
     */
    private static Map<String, Map<String, List<File>>> files(File[] files) {
        Map<String, Map<String, List<File>>> res = new HashMap<>();

        for (File file : files) {
            if (!file.getName().endsWith(".png"))
                continue;

            String[] tokens = file.getName().split("_");

            if (tokens.length < 4) {
                System.out.println("Incorrect file name: '" + file.getName() + "'.");

                continue;
            }

            Map<String, List<File>> map = res.get(tokens[2]);

            if (map == null) {
                map = new HashMap<>();

                res.put(tokens[2], map);
            }

            List<File> list = map.get(tokens[1]);

            if (list == null) {
                list = new ArrayList<>();

                map.put(tokens[1], list);
            }

            list.add(file);
        }

        return res;
    }

    /**
     * @param id Id.
     * @param files Files.
     * @param outFolder Output folder.
     */
    private static void generateHtml(String id, Map<String, List<File>> files, File outFolder) {
        File outFile = new File(outFolder, "Results_" + id + ".html");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)))) {
            writeLine(bw, "<html>");
            writeLine(bw, "<head>");
            writeLine(bw, "</head>");
            writeLine(bw, "<body>");

            long time;

            try {
                time = Long.parseLong(id);
            }
            catch (NumberFormatException e) {
                time = 0;
            }

            writeLine(bw, "<h1>Results" + (time == 0 ? "" : " on " + new Date(time)) + "</h1>");
            writeLine(bw, "<br>");

            for (Map.Entry<String, List<File>> entry : files.entrySet()) {
                writeLine(bw, "<h2>" + entry.getKey() + "</h2>");

                for (File file : entry.getValue()) {
                    writeLine(bw, "<h3>" + file.getName() + "</h3>");
                    writeLine(bw, "<img src=\"" + file.getName() + "\">");
                }
            }

            writeLine(bw, "</body>");
            writeLine(bw, "</html>");
        }
        catch (Exception e) {
            System.out.println("Exception is raised during file '" + outFile + "' processing.");

            e.printStackTrace();
        }

        System.out.println("Html file is generated: " + outFile.getAbsolutePath());
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
