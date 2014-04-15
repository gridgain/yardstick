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

package org.yardstick.writers;

import org.yardstick.*;

import java.io.*;
import java.util.*;

/**
 * CSV probe point writer.
 */
public class BenchmarkProbePointCsvWriter implements BenchmarkProbePointWriter {
    /** */
    public static final String PRINT_TO_OUTPUT = "benchmark.writer.print.to.output";

    /** */
    public static final String OUTPUT_PATH = "benchmark.writer.output.path";

    /** */
    public static final boolean DEFAULT_PRINT_TO_OUTPUT = false;

    /** */
    public static final String META_INFO_SEPARATOR = ",";

    /** */
    public static final String META_INFO_PREFIX = "**";

    /** */
    private PrintWriter writer;

    /** */
    private BenchmarkConfiguration cfg;

    /** */
    private long startTime;

    /** */
    private boolean printToOutput;

    /** */
    private File outPath;

    /** {@inheritDoc} */
    @Override public void start(BenchmarkConfiguration cfg, long startTime) {
        this.cfg = cfg;
        this.startTime = startTime;
        this.printToOutput = printToOutput(cfg);

        String path = cfg.customProperties().get(OUTPUT_PATH);

        if (path != null) {
            outPath = new File(path);

            if (!outPath.exists())
                throw new IllegalStateException("Output path does not exist: '" + path + "'.");
        }
    }

    /** {@inheritDoc} */
    @Override public void writePoints(BenchmarkProbe probe, Collection<BenchmarkProbePoint> points) throws Exception {
        if (writer == null) {
            String fileName = probe.getClass().getSimpleName() + "_" + startTime + ".csv";

            File f = outPath == null ? new File(fileName) : new File(outPath, fileName);

            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(f)));

            println("--Probe dump file for probe: " + probe + " (" + probe.getClass() + ")");
            println("--Created " + new Date(startTime));

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

            float[] vals = pt.values();

            for (int i = 0; i < vals.length; i++) {
                print(String.format("%.2f", vals[i]));

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

        if (printToOutput)
            cfg.output().print(s);
    }

    /**
     * @param s String to write.
     */
    private void println(String s) {
        writer.println(s);

        if (printToOutput)
            cfg.output().println(s);
    }

    /**
     * @param cfg Config.
     * @return Flat indicating whether to print to output or not.
     */
    private static boolean printToOutput(BenchmarkConfiguration cfg) {
        try {
            return Boolean.parseBoolean(cfg.customProperties().get(PRINT_TO_OUTPUT));
        }
        catch (NumberFormatException | NullPointerException ignored) {
            return DEFAULT_PRINT_TO_OUTPUT;
        }
    }
}
