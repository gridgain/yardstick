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

import com.beust.jcommander.*;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.entity.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.*;
import org.jfree.chart.title.*;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import org.yardstickframework.probes.*;
import org.yardstickframework.writers.*;

import java.awt.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.List;

import static java.awt.Color.*;
import static org.yardstickframework.BenchmarkUtils.*;
import static org.yardstickframework.report.jfreechart.JFreeChartGenerationMode.*;
import static org.yardstickframework.writers.BenchmarkProbePointCsvWriter.*;

/**
 * JFreeChart graph plotter.
 */
public class JFreeChartGraphPlotter {
    /** */
    private static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd");

    /** */
    private static final String INPUT_FILE_EXTENSION = ".csv";

    /** */
    private static final String MULTIPLE_DRIVERS_MARKER_FILE = ".multiple-drivers";

    /** */
    private static final Color[] PLOT_COLORS = {new Color(39, 174, 96), new Color(41, 128, 185),
        new Color(192, 57, 43), new Color(142, 68, 173), new Color(44, 62, 80), new Color(243, 156, 18),
        new Color(211, 84, 0), new Color(127, 140, 141), new Color(22, 160, 133)};

    /** */
    static final Comparator<File> FILE_NAME_COMP = new Comparator<File>() {
        @Override public int compare(File f1, File f2) {
            return f1.getName().compareTo(f2.getName());
        }
    };

    /**
     * @param cmdArgs Arguments.
     */
    public static void main(String[] cmdArgs) {
        try {
            JFreeChartGraphPlotterArguments args = new JFreeChartGraphPlotterArguments();

            JCommander jCommander = jcommander(cmdArgs, args, "<graph-plotter>");

            if (args.help()) {
                jCommander.usage();

                return;
            }

            if (args.inputFolders().isEmpty()) {
                errorHelp("Input folders are not defined.");

                return;
            }

            List<String> inFoldersAsString = args.inputFolders();

            List<File> inFolders = new ArrayList<>(inFoldersAsString.size());

            for (String folderAsString : inFoldersAsString)
                inFolders.add(new File(folderAsString).getAbsoluteFile());

            for (File inFolder : inFolders) {
                if (!inFolder.exists()) {
                    errorHelp("Folder does not exist: " + inFolder.getAbsolutePath());

                    return;
                }
            }

            List<List<List<File>>> benchFolders = new ArrayList<>();

            for (File inFolder : inFolders) {
                File[] dirs0 = inFolder.listFiles();

                if (dirs0 == null || dirs0.length == 0)
                    continue;

                List<File> dirs = new ArrayList<>(Arrays.asList(dirs0));

                Collections.sort(dirs, FILE_NAME_COMP);

                boolean multipleDrivers = false;

                for (File f : dirs) {
                    if (f.isFile() && MULTIPLE_DRIVERS_MARKER_FILE.equals(f.getName())) {
                        multipleDrivers = true;

                        break;
                    }
                }

                List<List<File>> mulDrvFiles = new ArrayList<>();

                if (multipleDrivers) {
                    for (File f : dirs) {
                        List<File> files = getFiles(f);

                        if (files != null)
                            mulDrvFiles.add(files);
                    }
                }
                else {
                    List<File> files = getFiles(inFolder);

                    if (files != null)
                        mulDrvFiles.add(files);
                }

                benchFolders.add(mergeMultipleDriverLists(mulDrvFiles));
            }

            if (benchFolders.isEmpty()) {
                errorHelp("Input folders are empty or have invalid structure: " + inFoldersAsString);

                return;
            }

            String outputFolder = outputFolder(inFolders);

            JFreeChartGenerationMode mode = args.generationMode();

            if (mode == COMPOUND)
                processCompoundMode(outputFolder, benchFolders, args);
            else if (mode == COMPARISON)
                processComparisonMode(outputFolder, benchFolders, args);
            else if (mode == STANDARD)
                processStandardMode(benchFolders, args);
            else
                errorHelp("Unknown generation mode: " + args.generationMode());
        }
        catch (ParameterException e) {
            errorHelp("Invalid parameter.", e);
        }
        catch (Exception e) {
            errorHelp("Failed to execute graph generator.", e);
        }
    }

    /**
     * @param inFolder Input folder.
     * @return List of folders.
     */
    private static List<File> getFiles(File inFolder) {
        File[] dirs = inFolder.listFiles();

        if (dirs == null || dirs.length == 0)
            return null;

        List<File> files = new ArrayList<>(Arrays.asList(dirs));

        for (int i = 0; i < files.size();) {
            if (!files.get(i).isDirectory())
                files.remove(i);
            else
                i++;
        }

        Collections.sort(files, FILE_NAME_COMP);

        return files;
    }

    /**
     * @param list List of folders.
     * @return Merged folders.
     */
    private static List<List<File>> mergeMultipleDriverLists(List<List<File>> list) {
        if (list == null || list.isEmpty())
            return Collections.emptyList();

        List<List<File>> res = new ArrayList<>();

        int idx = -1;

        while (true) {
            idx++;

            List<File> l = new ArrayList<>();

            for (List<File> files : list) {
                if (files.size() <= idx)
                    continue;

                l.add(files.get(idx));
            }

            if (l.isEmpty())
                break;

            res.add(l);
        }

        return res;
    }

    /**
     * @param outputFolder Output folders.
     * @param benchFolders Input folders.
     * @param args Arguments.
     * @throws Exception If failed.
     */
    private static void processCompoundMode(String outputFolder, List<List<List<File>>>benchFolders,
        JFreeChartGraphPlotterArguments args) throws Exception {
        Map<String, List<List<List<File>>>> res = new HashMap<>();

        for (List<List<File>> f0 : benchFolders) {
            for (List<File> f1 : f0) {
                Map<String, List<List<File>>> res0 = new HashMap<>();

                for (File f2 : f1) {
                    if (f2.isDirectory()) {
                        Map<String, List<File>> map = files(f2);

                        if (!map.isEmpty())
                            mergeMaps(res0, map);
                    }
                }

                if (!res0.isEmpty())
                    mergeMaps(res, res0);
            }
        }

        if (res.isEmpty())
            return;

        String parentFolderName = "results-" + COMPOUND.name().toLowerCase() + '-' + FORMAT.format(new Date());

        parentFolderName = fixFolderName(parentFolderName);

        File folderToWrite = new File(outputFolder, parentFolderName);

        if (!folderToWrite.exists()) {
            if (!folderToWrite.mkdir())
                throwException("Can not create folder: " + folderToWrite.getAbsolutePath());
        }

        processFilesPerProbe(res, folderToWrite, args, COMPOUND);
    }

    /**
     * @param outputFolder Output folder.
     * @param benchFolders Input folders.
     * @param args Arguments.
     * @throws Exception If failed.
     */
    private static void processComparisonMode(String outputFolder, List<List<List<File>>> benchFolders,
        JFreeChartGraphPlotterArguments args) throws Exception {
        String parentFolderName = "results-" + COMPARISON.name().toLowerCase() + '-' + FORMAT.format(new Date());

        parentFolderName = fixFolderName(parentFolderName);

        File parentFolderToWrite = new File(outputFolder, parentFolderName);

        int idx = -1;

        while (true) {
            idx++;

            boolean filesExist = false;

            Map<String, List<List<List<File>>>> res = new HashMap<>();

            for (List<List<File>> files : benchFolders) {
                if (files.size() <= idx)
                    continue;

                filesExist = true;

                List<File> f = files.get(idx);

                Map<String, List<List<File>>> res0 = new HashMap<>();

                for (File f0 : f) {
                    if (f0.isDirectory()) {
                        Map<String, List<File>> map = files(f0);

                        if (!map.isEmpty())
                            mergeMaps(res0, map);
                    }
                }

                if (!res0.isEmpty())
                    mergeMaps(res, res0);
            }

            if (!filesExist)
                break;

            List<List<List<File>>> fList = res.isEmpty() ? null : res.values().iterator().next();

            File parFile = fList == null || fList.isEmpty() ? null : fList.get(0).get(0).get(0).getParentFile();

            String suffix = parFile == null ? "" : parFile.getName();

            String time = parseTime(suffix);

            if (time == null && parFile != null) {
                parFile = parFile.getParentFile();

                suffix = parFile == null ? "" : parFile.getName();

                time = parseTime(suffix);
            }

            suffix = time == null ? "" : suffix.substring(time.length());

            String idxPrefix = idx < 9 ? "00" : idx < 99 ? "0" : "";

            String folName = idxPrefix + (idx + 1) + suffix;

            folName = fixFolderName(folName);

            File folderToWrite = new File(parentFolderToWrite, folName);

            if (!folderToWrite.exists()) {
                if (!folderToWrite.mkdirs())
                    throwException("Can not create folder: " + folderToWrite.getAbsolutePath());
            }

            processFilesPerProbe(res, folderToWrite, args, COMPARISON);
        }
    }

    /**
     * @param inFolders Input folders.
     * @param args Arguments.
     * @throws Exception If failed.
     */
    private static void processStandardMode(List<List<List<File>>> inFolders,
        JFreeChartGraphPlotterArguments args) throws Exception {
        for (List<List<File>> f0 : inFolders) {
            for (List<File> f1 : f0) {
                for (File f2 : f1) {
                    Map<String, List<JFreeChartPlotInfo>> infoMap = new HashMap<>();

                    for (List<File> files : files(f2).values()) {
                        for (File file : files) {
                            List<PlotData> plotData = readData(file);

                            if (!plotData.isEmpty())
                                processPlots(file.getParentFile(), Collections.singleton(plotData), infoMap, STANDARD);
                        }
                    }

                    JFreeChartResultPageGenerator.generate(f2, args, infoMap);
                }
            }
        }
    }

    /**
     * @param res Resulted map.
     * @param folderToWrite Folder to write results to.
     * @param args Arguments.
     * @param mode Generation mode.
     * @throws Exception If failed.
     */
    private static void processFilesPerProbe(Map<String, List<List<List<File>>>> res, File folderToWrite,
        JFreeChartGraphPlotterArguments args, JFreeChartGenerationMode mode) throws Exception {
        Map<String, List<JFreeChartPlotInfo>> infoMap = new HashMap<>();

        for (Map.Entry<String, List<List<List<File>>>> entry : res.entrySet()) {
            Collection<List<PlotData>> plots = new ArrayList<>(entry.getValue().size());

            for (List<List<File>> files : entry.getValue()) {
                if ((files.size() > 1 || files.get(0).size() > 1) &&
                    args.summaryMode() == JFreeChartSummaryMode.SUM_ONLY) {
                    if (isProbeResultFile(entry, ThroughputLatencyProbe.class) ||
                        isProbeResultFile(entry, PercentileProbe.class)) {
                        Collection<List<PlotData>> plots0 = new ArrayList<>(entry.getValue().size());

                        for (List<File> files0 : files) {
                            for (File file : files0) {
                                List<PlotData> pd = readData(file);

                                if (!pd.isEmpty())
                                    plots0.add(pd);
                            }
                        }

                        List<PlotData> sumPlot = addSummaryPlot(plots0);

                        if (!sumPlot.isEmpty())
                            plots.add(sumPlot);
                    }
                    else {
                        List<PlotData> pd = readData(files.get(0).get(0));

                        if (!pd.isEmpty())
                            plots.add(pd);
                    }
                }
                else {
                    for (List<File> f0 : files) {
                        List<List<PlotData>> l = new ArrayList<>(files.size());

                        for (File f1 : f0) {
                            List<PlotData> pd = readData(f1);

                            if (!pd.isEmpty()) {
                                plots.add(pd);

                                l.add(pd);
                            }
                        }

                        if (l.size() > 1 && args.summaryMode() == JFreeChartSummaryMode.INDIVIDUAL_AND_SUM &&
                            (isProbeResultFile(entry, ThroughputLatencyProbe.class) ||
                                isProbeResultFile(entry, PercentileProbe.class))) {
                            List<PlotData> sumPlot = addSummaryPlot(l);

                            if (!sumPlot.isEmpty())
                                plots.add(sumPlot);
                        }
                    }
                }
            }

            processPlots(folderToWrite, plots, infoMap, mode);
        }

        if (!infoMap.isEmpty())
            JFreeChartResultPageGenerator.generate(folderToWrite, args, infoMap);
    }

    /**
     * @param entry Entry.
     * @param probeCls Probe class.
     * @return check if file is the given probe result file.
     */
    private static <T> boolean isProbeResultFile(Map.Entry<String, T> entry, Class probeCls) {
        return entry.getKey().equals(probeCls.getSimpleName() + INPUT_FILE_EXTENSION);
    }

    /**
     * @param plots Plots.
     * @return Summary Plot.
     */
    private static List<PlotData> addSummaryPlot(Collection<List<PlotData>> plots) {
        int idx = -1;

        List<PlotData> sumPlot = new ArrayList<>();

        while (true) {
            idx++;

            PlotData sumPlotData = null;

            for (List<PlotData> plotData0 : plots) {
                if (plotData0.size() <= idx)
                    continue;

                PlotData plotData = plotData0.get(idx);

                double[][] data = plotData.series().data;

                if (sumPlotData == null) {
                    PlotSeries sumSeries = new PlotSeries(plotData.series().seriesName);

                    sumSeries.data = new double[data.length][];

                    sumPlotData = new PlotData(plotData.plotName(), sumSeries,
                        plotData.xAxisLabel, plotData.yAxisLabel);
                }

                sumPlotData.series().addConfigurations(plotData.series().cfg);

                double[][] sumData = sumPlotData.series().data;

                if (sumData[0] == null) {
                    sumData[0] = copy(data[0]);
                    sumData[1] = copy(data[1]);
                }
                else if (sumData[0].length <= data[0].length) {
                    for (int i = 0; i < sumData[0].length; i++)
                        sumData[1][i] += data[1][i];
                }
                else {
                    double[] tmp = sumData[1];

                    sumData[0] = copy(data[0]);
                    sumData[1] = copy(data[1]);

                    for (int i = 0; i < data[0].length; i++)
                        sumData[1][i] += tmp[i];
                }
            }

            if (sumPlotData == null)
                break;
            else
                sumPlot.add(sumPlotData);
        }

        return sumPlot;
    }

    /**
     * @param src Source array
     * @return Copied array.
     */
    private static double[] copy(double[] src) {
        double[] res = new double[src.length];

        System.arraycopy(src, 0, res, 0, src.length);

        return res;
    }

    /**
     * @param res Resulted map.
     * @param map Map to merge.
     */
    private static <T> void mergeMaps(Map<String, List<T>> res, Map<String, T> map) {
        for (Map.Entry<String, T> entry : map.entrySet()) {
            List<T> list = res.get(entry.getKey());

            if (list == null) {
                list = new ArrayList<>();

                res.put(entry.getKey(), list);
            }

            list.add(entry.getValue());
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

        files0(dirs, res);

        return res;
    }

    /**
     * @param files Files.
     * @param res Result.
     */
    private static void files0(File[] files, Map<String, List<File>> res) {
        if (files == null || files.length == 0)
            return;

        List<File> fs = new ArrayList<>(Arrays.asList(files));

        Collections.sort(fs, FILE_NAME_COMP);

        for (File file : fs) {
            if (file.isDirectory())
                files0(file.listFiles(), res);
            else
                addFile(file, res);
        }
    }

    /**
     * @param file File to add.
     * @param res Resulted collection.
     */
    private static void addFile(File file, Map<String, List<File>> res) {
        if (file.isDirectory())
            return;

        if (!file.canRead()) {
            errorHelp("File can not be read: " + file.getAbsolutePath());

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
     * @param plots Collections of plots.
     * @param infoMap Map with additional plot info.
     * @param mode Generation mode.
     * @throws Exception If failed.
     */
    private static void processPlots(File folderToWrite, Collection<List<PlotData>> plots,
        Map<String, List<JFreeChartPlotInfo>> infoMap, JFreeChartGenerationMode mode) throws Exception {
        ChartRenderingInfo info = new ChartRenderingInfo(new StandardEntityCollection());

        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);

        int idx = -1;

        while (true) {
            idx++;

            DefaultXYDataset dataSet = new DefaultXYDataset();

            List<JFreeChartPlotInfo> infoList = new ArrayList<>();

            String xAxisLabel = "";
            String yAxisLabel = "";
            String plotName = "";

            int cnt = 0;

            for (List<PlotData> plotData0 : plots) {
                if (plotData0.size() <= idx)
                    continue;

                PlotData plotData = plotData0.get(idx);

                dataSet.addSeries(plotData.plotName() + "_" + cnt++, plotData.series().data);

                xAxisLabel = plotData.xAxisLabel;
                yAxisLabel = plotData.yAxisLabel;
                plotName = plotData.plotName();

                infoList.add(info(plotData.series(), mode));
            }

            if (infoList.isEmpty())
                break;

            JFreeChart chart = ChartFactory.createXYLineChart(
                "",
                xAxisLabel,
                yAxisLabel,
                dataSet,
                PlotOrientation.VERTICAL,
                false,
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

            for (int i = 0; i < infoList.size(); i++) {
                Color color = PLOT_COLORS[i % PLOT_COLORS.length];

                renderer.setSeriesPaint(i, color);
                renderer.setSeriesStroke(i, new BasicStroke(3)); // Line thickness.

                infoList.get(i).color(Integer.toHexString(color.getRGB()).substring(2));
            }

            ValueAxis axis = plot.getRangeAxis();

            Font font = new Font("Helvetica,Arial,sans-serif", Font.BOLD, axis.getTickLabelFont().getSize() + 5);

            axis.setTickLabelFont(font);
            axis.setLabelFont(font);
            plot.getDomainAxis().setTickLabelFont(font);
            plot.getDomainAxis().setLabelFont(font);

            chart.setTitle(new TextTitle(yAxisLabel, new Font(font.getName(), font.getStyle(), 30)));

            File res = new File(folderToWrite, plotName + ".png");

            ChartUtilities.saveChartAsPNG(res, chart, 1000, 500, info);

            infoMap.put(res.getAbsolutePath(), infoList);

            println("Chart is saved to file: ", res);
        }
    }

    /**
     * @param series Plot series.
     * @param mode Generation mode.
     * @return Graph info.
     */
    private static JFreeChartPlotInfo info(PlotSeries series, JFreeChartGenerationMode mode) {
        double sum = 0;
        double min = Long.MAX_VALUE;
        double max = Long.MIN_VALUE;

        int len = series.data[1].length;

        if (len == 1) {
            double val = series.data[1][0];

            return new JFreeChartPlotInfo(series.seriesName, series.configuration(), val, val, val, 0, mode);
        }

        for (int i = 0; i < len; i++) {
            double val = series.data[1][i];

            min = Math.min(min, val);

            max = Math.max(max, val);

            sum += val;
        }

        double avg = sum / len;

        double s = 0;

        for (int i = 0; i < len; i++) {
            double val = series.data[1][i];

            s += Math.pow((val - avg), 2);
        }

        double stdDiv = Math.sqrt(s / (len - 1));

        return new JFreeChartPlotInfo(series.seriesName, series.configuration(), avg, min, max, stdDiv, mode);
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

            String drvNames = null;
            String[] metaInfo = null;

            for (String line; (line = br.readLine()) != null; ) {
                if (line.startsWith("--"))
                    continue;

                if (line.startsWith(DRV_NAMES_PREFIX)) {
                    drvNames = line.substring(DRV_NAMES_PREFIX.length());

                    List<String> drvNamesList = Arrays.asList(drvNames.split(","));

                    Collections.sort(drvNamesList);

                    drvNames = drvNamesList.toString().replaceAll("\\[", "").replaceAll("]", "");

                    continue;
                }

                if (line.startsWith(META_INFO_PREFIX)) {
                    metaInfo = line.substring(META_INFO_PREFIX.length()).split("\"" + META_INFO_SEPARATOR + "\"");

                    continue;
                }

                String[] split = line.split(",");

                if (data.isEmpty()) {
                    initTime = Long.parseLong(split[0]);

                    int plotNum = split.length - 1;

                    if (plotNum < 1)
                        throwException("Invalid data file: " + file.getAbsolutePath());

                    String xAxisLabel = metaInfo == null || metaInfo.length == 0 ? "" : metaInfo[0].replace("\"", "");

                    for (int i = 0; i < plotNum; i++) {
                        PlotSeries single = new PlotSeries(drvNames);

                        single.addConfiguration(file.getParentFile().getName());

                        String yAxisLabel = metaInfo == null || i + 1 >= metaInfo.length ? "" :
                            metaInfo[i + 1].replace("\"", "");

                        String plotName = file.getName().replace(INPUT_FILE_EXTENSION, "");

                        String cnt = Integer.toString(i + 1);

                        cnt = cnt.length() == 1 ? "0" + cnt : cnt;

                        data.add(new PlotData("Plot_" + plotName + "_" + cnt, single,
                            xAxisLabel, yAxisLabel));
                    }
                }

                double[] tup = new double[split.length];

                for (int i = 0; i < tup.length; i++) {
                    double d = i == 0 ? (Long.parseLong(split[0]) - initTime) : Double.parseDouble(split[i]);

                    tup[i] = d;
                }

                for (int i = 0; i < split.length - 1; i++)
                    data.get(i).series().rawData.add(new double[] {tup[0], tup[i + 1]});
            }

            for (PlotData plotData : data)
                plotData.series().finish();

            return data;
        }
        catch (Exception e) {
            errorHelp("Exception is raised while processing file (will skip): " + file.getAbsolutePath(), e);

            return Collections.emptyList();
        }
    }

    /**
     * @param fName Folder name.
     * @return Substring containing benchmark time.
     */
    static String parseTime(String fName) {
        int i = fName.indexOf('-', fName.indexOf('-') + 1);

        if (i != -1) {
            try {
                String time = fName.substring(0, i);

                BenchmarkProbePointCsvWriter.FORMAT.parse(time);

                return time;
            }
            catch (ParseException ignored) {
                return null;
            }
        }

        return null;
    }

    /**
     * @param inFolders Input folders.
     * @return Output folder name.
     */
    private static String outputFolder(List<File> inFolders) {
        return inFolders.size() != 1 ? null :
            inFolders.get(0).getParent() == null ? inFolders.get(0).getName() : inFolders.get(0).getParent();
    }

    /**
     * Throws exception.
     *
     * @param msg Error message.
     * @throws Exception that describes exceptional situation.
     */
    private static void throwException(String msg) throws Exception {
        throw new Exception("ERROR: " + msg);
    }

    /**
     * Prints error and help.
     *
     * @param msg Error message.
     */
    static void errorHelp(String msg) {
        System.err.println("ERROR: " + msg);
        System.err.println("Type '--help' for usage.");
    }

    /**
     * Prints error and help.
     *
     * @param msg Error message.
     * @param t Throwable, possibly {@code null}.
     */
    static void errorHelp(String msg, Throwable t) {
        errorHelp(msg);

        if (t != null)
            t.printStackTrace();
    }

    /**
     *
     */
    private static class PlotData {
        /** */
        private final PlotSeries series;

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
        PlotData(String plotName, PlotSeries series, String xAxisLabel, String yAxisLabel) {
            this.plotName = plotName;
            this.series = series;
            this.xAxisLabel = xAxisLabel;
            this.yAxisLabel = yAxisLabel;
        }

        /**
         * @return Series.
         */
        public PlotSeries series() {
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
        private final List<String> cfg = new ArrayList<>();

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
         * @param cfg Configuration string.
         */
        public void addConfiguration(String cfg) {
            this.cfg.add(cfg);
        }

        /**
         * @param cfg Configuration strings.
         */
        public void addConfigurations(List<String> cfg) {
            this.cfg.addAll(cfg);
        }

        /**
         * @return Configuration string.
         */
        public Collection<String> configuration() {
            Collections.sort(cfg);

            return cfg;
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
