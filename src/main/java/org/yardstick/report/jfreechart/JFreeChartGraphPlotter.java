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

import com.beust.jcommander.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.yardstick.util.*;
import org.yardstick.writers.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static java.awt.Color.*;
import static org.yardstick.writers.BenchmarkProbePointCsvWriter.*;

/**
 * JFreeChart graph plotter.
 */
public class JFreeChartGraphPlotter {
    /** */
    private static final String INPUT_FILE_EXTENSION = ".csv";

    /** */
    private static final Color[] PLOT_COLORS = {GREEN, BLUE, RED, ORANGE, CYAN, MAGENTA,
        new Color(255, 0, 137), new Color(163, 143, 255), new Color(76, 255, 153)};

    /**
     * @param cmdArgs Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] cmdArgs) throws Exception {
        JFreeChartGraphPlotterArguments args = new JFreeChartGraphPlotterArguments();

        JCommander jCommander = BenchmarkUtils.jcommander(cmdArgs, args, "<graph-plotter>");

        if (args.help()) {
            jCommander.usage();

            return;
        }

        if (args.inputFolders() == null) {
            System.out.println("Input folder is not defined.");

            return;
        }

        String[] inFoldersAsString = args.inputFolders().split(",");

        File[] inFolders = new File[inFoldersAsString.length];

        for (int i = 0; i < inFoldersAsString.length; i++)
            inFolders[i] = new File(inFoldersAsString[i]);

        for (File inFolder : inFolders) {
            if (!inFolder.exists()) {
                System.out.println("Folder '" + inFolder.getAbsolutePath() + "' does not exist.");

                return;
            }
        }

        if (args.compoundChart()) {
            String date = BenchmarkProbePointCsvWriter.FORMAT.format(System.currentTimeMillis());

            File folderToWrite = new File(inFolders[0].getParent() + File.separator + date + "_compound_results");

            if (!folderToWrite.exists()) {
                if (!folderToWrite.mkdir()) {
                    System.out.println("Can not create folder '" + folderToWrite.getAbsolutePath() + "'.");

                    return;
                }
            }

            Map<String, List<File>> res = new HashMap<>();

            for (File inFolder : inFolders) {
                Map<String, List<File>> map = files(inFolder);

                for (Map.Entry<String, List<File>> entry : map.entrySet()) {
                    List<File> list = res.get(entry.getKey());

                    if (list == null) {
                        list = new ArrayList<>();

                        res.put(entry.getKey(), list);
                    }

                    list.addAll(entry.getValue());
                }
            }

            for (Map.Entry<String, List<File>> entry : res.entrySet()) {
                Collection<List<PlotData>> plots = new ArrayList<>(entry.getValue().size());

                for (File file : entry.getValue()) {
                    System.out.println("Processing file '" + file + "'.");

                    try {
                        plots.add(readData(file));
                    } catch (Exception e) {
                        System.out.println("Exception is raised during file '" + file + "' processing.");

                        e.printStackTrace();
                    }
                }

                processPlots(folderToWrite, plots);
            }

            JFreeChartResultPageGenerator.generate(folderToWrite, args);
        }
        else {
            for (File inFolder : inFolders) {
                for (List<File> files : files(inFolder).values()) {
                    for (File file : files) {
                        System.out.println("Processing file '" + file + "'.");

                        try {
                            List<PlotData> plotData = readData(file);

                            processPlots(file.getParentFile(), Collections.singleton(plotData));
                        } catch (Exception e) {
                            System.out.println("Exception is raised during file '" + file + "' processing.");

                            e.printStackTrace();
                        }
                    }
                }

                JFreeChartResultPageGenerator.generate(inFolder, args);
            }
        }
    }

    /**
     * @param folder Folder to scan for files.
     * @return Collection of files.
     */
    private static Map<String, List<File>> files(File folder) {
        File[] dirs = folder.listFiles();

        if (dirs == null || dirs.length == 0)
            return Collections.emptyMap();

        Map<String, List<File>> res = new HashMap<>();

        for (File dir : dirs) {
            if (dir.isDirectory()) {
                File[] files = dir.listFiles();

                if (files == null || files.length == 0)
                    continue;

                for (File file : files)
                    addFile(file, res);
            }
            else
                addFile(dir, res);
        }

        return res;
    }

    /**
     * @param file File to add.
     * @param res Resulted collection.
     */
    private static void addFile(File file, Map<String, List<File>> res) {
        if (file.isDirectory())
            return;

        if (!file.canRead()) {
            System.out.println("File '" + file + "' can not be read.");

            return;
        }

        if (file.getName().endsWith(INPUT_FILE_EXTENSION)) {
            List<File> list = res.get(file.getName());

            if (list == null) {
                list = new ArrayList<>();

                res.put(file.getName(), list);
            }

            list.add(file);
        }
    }

    /**
     * @param folderToWrite Folder to write the resulted charts.
     * @throws Exception If failed.
     */
    private static void processPlots(File folderToWrite, Collection<List<PlotData>> plots) throws Exception {
        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        int idx = -1;

        while (true) {
            idx++;

            boolean dataExists = false;

            DefaultXYDataset dataSet = new DefaultXYDataset();

            String xAxisLabel = "";
            String yAxisLabel = "";
            String plotName = "";

            int seriesNum = 0;

            for (List<PlotData> plotData0 : plots) {
                if (plotData0.size() <= idx)
                    continue;
                else
                    dataExists = true;

                PlotData plotData = plotData0.get(idx);

                for (PlotSeries s : plotData.series())
                    dataSet.addSeries(s.seriesName, s.data);

                xAxisLabel = plotData.xAxisLabel;
                yAxisLabel = plotData.yAxisLabel;
                plotName = plotData.plotName();

                seriesNum++;
            }

            if (!dataExists)
                break;

            JFreeChart chart = ChartFactory.createXYLineChart(
                    "",
                    xAxisLabel,
                    yAxisLabel,
                    dataSet,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);

            AxisSpace as = new AxisSpace();

            as.add(150, RectangleEdge.LEFT);

            XYPlot plot = (XYPlot)chart.getPlot();

            BasicStroke stroke = new BasicStroke(1);

            plot.setRenderer(renderer);
            plot.setBackgroundPaint(WHITE);
            plot.setRangeGridlinePaint(GRAY);
            plot.setDomainGridlinePaint(GRAY);
            plot.setFixedRangeAxisSpace(as);
            plot.setOutlineStroke(stroke);

            for (int i = 0; i < seriesNum; i++) {
                renderer.setSeriesPaint(i, PLOT_COLORS[i % PLOT_COLORS.length]);
                renderer.setSeriesStroke(i, new BasicStroke(3)); // Line thickness.
            }

            ValueAxis axis = plot.getRangeAxis();

            Font font = new Font(axis.getTickLabelFont().getName(), Font.BOLD, axis.getTickLabelFont().getSize() + 3);

            axis.setTickLabelFont(font);
            axis.setLabelFont(font);
            plot.getDomainAxis().setTickLabelFont(font);
            plot.getDomainAxis().setLabelFont(font);

            chart.setTitle(new TextTitle(yAxisLabel, new Font(font.getName(), font.getStyle(), 30)));

            File res = new File(folderToWrite, plotName + ".png");

            ChartUtilities.saveChartAsPNG(res, chart, 800, 400, info);

            System.out.println("Resulted chart is saved to file '" + res.getAbsolutePath() + "'.");
        }

        System.out.println();
    }

    /**
     * @param file File.
     * @return Collection of plot data.
     * @throws Exception If failed.
     */
    private static List<PlotData> readData(File file) throws Exception {
        List<PlotData> data = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            long initTime = 0;

            String[] metaInfo = null;

            for (String line; (line = br.readLine()) != null;) {
                if (line.startsWith("--"))
                    continue;

                if (line.startsWith(META_INFO_PREFIX)) {
                    metaInfo = line.substring(META_INFO_PREFIX.length()).split("\"" + META_INFO_SEPARATOR + "\"");

                    continue;
                }

                String[] split = line.split(",");

                if (data.isEmpty()) {
                    initTime = Long.parseLong(split[0]);

                    int plotNum = split.length - 1;

                    if (plotNum < 1)
                        throw new Exception("Invalid data file.");

                    String xAxisLabel = metaInfo == null || metaInfo.length == 0 ? "" : metaInfo[0].replace("\"", "");

                    for (int i = 0; i < plotNum; i++) {
                        List<PlotSeries> single = Collections.singletonList(new PlotSeries(file.getParentFile().getName()));

                        String yAxisLabel = metaInfo == null || i + 1 >= metaInfo.length ? "" :
                            metaInfo[i + 1].replace("\"", "");

                        String plotName = file.getName().replace(INPUT_FILE_EXTENSION, "");

                        String cnt = Integer.toString(i + 1);

                        cnt = cnt.length() == 1 ? "0" + cnt : cnt;

                        data.add(new PlotData("Plot_" + plotName + "_" + cnt, single, xAxisLabel, yAxisLabel));
                    }
                }

                double[] tup = new double[split.length];

                for (int i = 0; i < tup.length; i++) {
                    double d = i == 0 ? (Long.parseLong(split[0]) - initTime) : Double.parseDouble(split[i]);

                    tup[i] = d;
                }

                for (int i = 0; i < split.length - 1; i++)
                    data.get(i).series().get(0).rawData.add(new double[] {tup[0], tup[i + 1]});
            }

            for (PlotData plotData : data) {
                for (PlotSeries series : plotData.series())
                    series.finish();
            }

            return data;
        }
    }

    /**
     *
     */
    private static class PlotData {
        /** */
        private final List<PlotSeries> series;

        /** */
        private final String plotName;

        /** */
        private final String xAxisLabel;

        /** */
        private final String yAxisLabel;

        /**
         * @param plotName Plot name.
         * @param series Series.
         * @param xAxisLabel X axis label.
         * @param yAxisLabel Y axis label.
         */
        PlotData(String plotName, List<PlotSeries> series, String xAxisLabel, String yAxisLabel) {
            this.plotName = plotName;
            this.series = series;
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
        }

        /**
         * @return Series.
         */
        public List<PlotSeries> series() {
            return series;
        }

        /**
         * @return Plot name.
         */
        public String plotName() {
            return plotName;
        }
    }

    /**
     *
     */
    private static class PlotSeries {
        /** */
        private final String seriesName;

        /** */
        private List<double[]> rawData = new ArrayList<>();

        /** */
        private double[][] data;

        /**
         * @param seriesName Series name.
         */
        PlotSeries(String seriesName) {
            this.seriesName = seriesName;
        }

        /**
         *
         */
        public void finish() {
            data = new double[2][];

            data[0] = new double[rawData.size()];
            data[1] = new double[rawData.size()];

            for (int i = 0; i < rawData.size(); i++) {
                double[] tup = rawData.get(i);

                data[0][i] = tup[0];
                data[1][i] = tup[1];
            }

            // No need raw data anymore.
            rawData = null;
        }
    }
}
