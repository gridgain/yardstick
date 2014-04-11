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
    public static final String META_INFO_SEPARATOR = ";";

    /** */
    public static final String META_INFO_PREFIX = "*MI*";

    /** Print writer. */
    private PrintWriter writer;

    /** {@inheritDoc} */
    @Override public void writePoints(BenchmarkProbe probe, Collection<BenchmarkProbePoint> points) throws Exception {
        if (writer == null) {
            writer = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(probe.getClass().getSimpleName() + "_" + System.currentTimeMillis() + ".csv")));

            writer.println("--Probe dump file for probe: " + probe + " (" + probe.getClass() + ")");
            writer.println("--Created " + new Date());

            if (probe.metaInfo() != null && probe.metaInfo().size() > 0) {
                int i = 0;

                writer.print(META_INFO_PREFIX);

                for (String metaInfo : probe.metaInfo())
                    writer.print(metaInfo + (++i == probe.metaInfo().size() ? "" : META_INFO_SEPARATOR));

                if (i != 0)
                    writer.println();
            }
        }

        for (BenchmarkProbePoint pt : points) {
            writer.write(String.valueOf(pt.time()));
            writer.write(",");

            float[] vals = pt.values();

            for (int i = 0; i < vals.length; i++) {
                writer.write(String.format("%.2f", vals[i]));

                if (i != vals.length - 1)
                    writer.write(",");
            }

            writer.println();
        }

        writer.flush();
    }

    /** {@inheritDoc} */
    @Override public void close() throws Exception {
        if (writer != null)
            writer.close();
    }
}
