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

            generateHtml(testTime, files, folder);
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
     * @param testTime Tsst time.
     * @param files Files.
     * @param outFolder Output folder.
     */
    private static void generateHtml(Date testTime, Map<String, List<File>> files, File outFolder) {
        File outFile = new File(outFolder, "Results.html");

        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outFile)))) {
            writeLine(bw, "<html>");
            writeLine(bw, "<head>");
            writeLine(bw, "</head>");
            writeLine(bw, "<body>");

            writeLine(bw, "<h1>Results" + (testTime == null ? "" : " on " + testTime) + "</h1>");
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
