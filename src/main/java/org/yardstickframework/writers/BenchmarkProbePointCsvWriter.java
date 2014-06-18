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

package org.yardstickframework.writers;

import org.yardstickframework.*;

import java.io.*;
import java.text.*;
import java.util.*;

/**
 * CSV probe point writer.
 */
public class BenchmarkProbePointCsvWriter implements BenchmarkProbePointWriter {
    /** */
    private static final String DUPLICATE_TO_OUTPUT = "BENCHMARK_WRITER_DUPLICATE_TO_OUTPUT";

    /** */
    private static final boolean DEFAULT_DUPLICATE_TO_OUTPUT = false;

    /** */
    public static final String META_INFO_SEPARATOR = ",";

    /** */
    public static final String META_INFO_PREFIX = "**";

    /** */
    public static final SimpleDateFormat FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");

    /** */
    private PrintWriter writer;

    /** */
    private BenchmarkConfiguration cfg;

    /** */
    private BenchmarkDriver drv;

    /** */
    private long startTime;

    /** */
    private boolean dupToOutput;

    /** */
    private File outPath;

    /** {@inheritDoc} */
    @Override public void start(BenchmarkDriver drv, BenchmarkConfiguration cfg, long startTime) {
        this.cfg = cfg;
        this.drv = drv;
        this.startTime = startTime;
        this.dupToOutput = duplicateToOutput(cfg);

        String path = cfg.outputFolder();

        File folder = null;

        if (path != null) {
            folder = new File(path);

            if (!folder.exists()) {
                if (!folder.mkdirs())
                    throw new IllegalStateException("Can not create folder: " + folder.getAbsolutePath() + "");
            }
        }

        String desc = drv.description() == null ? "" : drv.description();

        desc = desc.replaceAll("-+", "-").replaceAll(",|\\\\|/|\\||%|:|<|>|\\*|\\?|\"|\\s", "-");

        desc = desc.charAt(0) == '-' ? desc : '-' + desc;

        String subFolderName = FORMAT.format(new Date(startTime)) + desc;

        subFolderName = BenchmarkUtils.fixFolderName(subFolderName);

        outPath = folder == null ? new File(subFolderName) : new File(folder, subFolderName);

        if (!outPath.exists()) {
            if (!outPath.mkdir())
                throw new IllegalStateException("Can not create folder: " + outPath.getAbsolutePath() + "");
        }
    }

    /** {@inheritDoc} */
    @Override public void writePoints(BenchmarkProbe probe, Collection<BenchmarkProbePoint> points) throws Exception {
        if (writer == null) {
            String fileName = probe.getClass().getSimpleName() + ".csv";

            File f = outPath == null ? new File(fileName) : new File(outPath, fileName);

            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));

            String parent = outPath.getParent() == null ? outPath.getPath() : outPath.getParent();

            BenchmarkUtils.println(cfg,
                probe.getClass().getSimpleName() + " results will be saved to: " + parent);

            println("--Probe dump file for probe: " + probe + " (" + probe.getClass() + ")");
            println("--Created " + new Date(startTime));
            println("--Benchmark config: " + removeUnwantedChars(cfg.toString()));
            println("--Description: " + removeUnwantedChars(drv.description() == null ? "" : drv.description()));

            if (probe.metaInfo() != null && probe.metaInfo().size() > 0) {
                int i = 0;

                print(META_INFO_PREFIX);

                for (String metaInfo : probe.metaInfo())
                    print("\"" + metaInfo + "\"" + (++i == probe.metaInfo().size() ? "" : META_INFO_SEPARATOR));

                if (i != 0)
                    println("");
            }
        }

        for (BenchmarkProbePoint pt : points) {
            print(String.valueOf(pt.time()));
            print(",");

            double[] vals = pt.values();

            for (int i = 0; i < vals.length; i++) {
                print(String.format(Locale.US, "%.2f", vals[i]));

                if (i != vals.length - 1)
                    print(",");
            }

            println("");
        }

        writer.flush();
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        if (writer != null)
            writer.close();
    }

    /**
     * @param s String to write.
     */
    private void print(String s) {
        writer.print(s);

        if (dupToOutput)
            cfg.output().print(s);
    }

    /**
     * @param s String to write.
     */
    private void println(String s) {
        writer.println(s);

        if (dupToOutput)
            BenchmarkUtils.println(cfg, s);
    }

    /**
     * @param val String.
     * @return String with removed chars.
     */
    private String removeUnwantedChars(String val) {
        return val.replaceAll("\n|\t|\r|\f", "");
    }

    /**
     * @param cfg Config.
     * @return Flat indicating whether to duplicate to output or not.
     */
    private boolean duplicateToOutput(BenchmarkConfiguration cfg) {
        try {
            return Boolean.parseBoolean(cfg.customProperties().get(DUPLICATE_TO_OUTPUT));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_DUPLICATE_TO_OUTPUT;
        }
    }
}
