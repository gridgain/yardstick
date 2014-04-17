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
     */
    public static void main(String[] cmdArgs) {
        JFreeChartGraphPlotterArguments args = new JFreeChartGraphPlotterArguments();

        JCommander jCommander = BenchmarkUtils.jcommander(cmdArgs, args, "<graph-plotter>");

        if (args.help()) {
            jCommander.usage();

            return;
        }

        if (args.inputFolder() == null) {
            System.out.println("Input folder is not defined.");

            return;
        }

        File inFolder = new File(args.inputFolder());

        if (!inFolder.exists()) {
            System.out.println("Folder '" + args.inputFolder() + "' does not exist.");

            return;
        }

        for (File file : files(inFolder)) {
            try {
                processFile(file);
            }
            catch (Exception e) {
                System.out.println("Exception is raised during file '" + file + "' processing.");

                e.printStackTrace();
            }
        }

        JFreeChartResultPageGenerator.generate(inFolder, args);
    }

    /**
     * @param folder Folder to scan for files.
     * @return Collection of files.
     */
    private static Collection<File> files(File folder) {
        File[] dirs = folder.listFiles();

        if (dirs == null || dirs.length == 0)
            return Collections.emptyList();

        Collection<File> res = new ArrayList<>();

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
    private static void addFile(File file, Collection<File> res) {
        if (file.isDirectory())
            return;

        if (!file.canRead()) {
            System.out.println("File '" + file + "' can not be read.");

            return;
        }

        if (file.getName().endsWith(INPUT_FILE_EXTENSION))
            res.add(file);
    }

    /**
     * @param file File to process.
     * @throws Exception If failed.
     */
    private static void processFile(File file) throws Exception {
        System.out.println("Processing file '" + file + "'.");

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        Collection<PlotData> plots = readData(file);

        int i = 0;

        for (PlotData plotData : plots) {
            DefaultXYDataset dataset = new DefaultXYDataset();

            for (PlotSeries s : plotData.series())
                dataset.addSeries(s.seriesName, s.data);

            JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                plotData.xAxisLabel,
                plotData.yAxisLabel,
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false);

            AxisSpace as = new AxisSpace();

            as.add(130, RectangleEdge.LEFT);

            XYPlot plot = (XYPlot)chart.getPlot();

            BasicStroke stroke = new BasicStroke(1);

            plot.setRenderer(renderer);
            plot.setBackgroundPaint(WHITE);
            plot.setRangeGridlinePaint(GRAY);
            plot.setDomainGridlinePaint(GRAY);
            plot.setFixedRangeAxisSpace(as);
            renderer.setSeriesPaint(0, PLOT_COLORS[i++ % PLOT_COLORS.length]);
            renderer.setSeriesStroke(0, new BasicStroke(3)); // Line thickness.
            plot.setOutlineStroke(stroke);

            ValueAxis axis = plot.getRangeAxis();

            Font font = new Font(axis.getTickLabelFont().getName(), Font.BOLD, axis.getTickLabelFont().getSize() + 3);

            axis.setTickLabelFont(font);
            axis.setLabelFont(font);
            plot.getDomainAxis().setTickLabelFont(font);
            plot.getDomainAxis().setLabelFont(font);

            chart.setTitle(new TextTitle(plotData.yAxisLabel, new Font(font.getName(), font.getStyle(), 30)));

            File res = new File(file.getParent(), plotData.plotName() + ".png");

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
    private static Collection<PlotData> readData(File file) throws Exception {
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
                        // Separate series for each plot.
                        List<PlotSeries> single = Collections.singletonList(new PlotSeries("Series-" + i));

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
        private final List<PlotSeries> series;

        private final String plotName;

        private final String xAxisLabel;

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
        private final String seriesName;

        private List<double[]> rawData = new ArrayList<>();

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
