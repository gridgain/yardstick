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
import org.jfree.chart.entity.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.yardstick.util.*;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.List;

import static org.yardstick.writers.BenchmarkProbePointCsvWriter.*;

/**
 * JFreeChart graph plotter.
 */
public class JFreeChartGraphPlotter {
    /**
     * @param cmdArgs Arguments.
     */
    public static void main(String[] cmdArgs) {
        JFreeChartGraphPlotterArguments args = parseArgs(cmdArgs);

        if (args == null)
            return;

        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        try {
            File file = new File(args.inputFileName());

            if (!file.exists()) {
                System.out.println("File '" + args.inputFileName() + "' does not exist.");

                return;
            }

            Collection<PlotData> plots = readData(file);

            for (PlotData plotData : plots) {
                DefaultXYDataset dataset = new DefaultXYDataset();

                for (PlotSeries s : plotData.series())
                    dataset.addSeries(s.seriesName, s.data);

                JFreeChart chart = ChartFactory.createXYLineChart(
                    "Performance",
                    plotData.xAxisLabel,
                    plotData.yAxisLabel,
                    dataset,
                    PlotOrientation.VERTICAL,
                    true,
                    false,
                    false);

                XYPlot plot = (XYPlot)chart.getPlot();
                plot.setRenderer(renderer);

                IntervalMarker intervalIncertitude = new IntervalMarker(1.0d, 2.0d);
                intervalIncertitude.setPaint(new Color(222, 222, 255, 128));
                plot.addDomainMarker(intervalIncertitude, Layer.BACKGROUND);

                Marker distanceTiers = new ValueMarker(1.0d);
                distanceTiers.setPaint(Color.BLACK);
                plot.addDomainMarker(distanceTiers);

                renderer.setSeriesPaint(0, Color.BLACK);

                File res = new File(plotData.plotName() + ".png");

                ChartUtilities.saveChartAsPNG(res, chart, 800, 400, info);

                System.out.println("Chart is saved to file '" + res.getAbsolutePath() + "'.");
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
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
                    metaInfo = line.substring(META_INFO_PREFIX.length()).split(META_INFO_SEPARATOR);

                    continue;
                }

                String[] split = line.split(",");

                if (data.isEmpty()) {
                    initTime = Long.parseLong(split[0]);

                    int plotNum = split.length - 1;

                    if (plotNum < 1)
                        throw new Exception("Invalid data file.");

                    String xAxisLabel = metaInfo == null || metaInfo.length == 0 ?  "" : metaInfo[0];

                    for (int i = 0; i < plotNum; i++) {
                        // Separate series for each plot.
                        List<PlotSeries> single = Collections.singletonList(new PlotSeries("Series-" + i));

                        String yAxisLabel = metaInfo == null || i + 1 >= metaInfo.length  ?  "" : metaInfo[i + 1];

                        data.add(new PlotData("Plot-" + file.getName() + "-" + i, single, xAxisLabel, yAxisLabel));
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
     * @param cmdArgs Arguments.
     * @return Graph plotter arguments.
     */
    private static JFreeChartGraphPlotterArguments parseArgs(String[] cmdArgs) {
        JFreeChartGraphPlotterArguments args = new JFreeChartGraphPlotterArguments();

        JCommander jCommander = BenchmarkUtils.jcommander(cmdArgs, args, "<graph-plotter>");

        if (args.help()) {
            jCommander.usage();

            return null;
        }

        if (args.inputFileName() == null) {
            System.out.println("Input file name is not defined.");

            return null;
        }

        return args;
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
