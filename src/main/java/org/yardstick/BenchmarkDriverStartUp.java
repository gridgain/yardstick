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

package org.yardstick;

import org.yardstick.impl.*;

import java.util.*;

import static org.yardstick.BenchmarkUtils.*;

/**
 * Benchmark driver startup class.
 */
public class BenchmarkDriverStartUp {
    /**
     * @param cmdArgs Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] cmdArgs) throws Exception {
        final BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        cfg.commandLineArguments(cmdArgs);

        BenchmarkUtils.jcommander(cmdArgs, cfg, "<benchmark-runner>");

        BenchmarkLoader ldr = new BenchmarkLoader();

        ldr.initialize(cfg);

        String name = cfg.driverName();

        if (name != null)
            name = name.trim();

        if (name == null || name.isEmpty()) {
            errorHelp(cfg, "Driver class name is not specified.");

            return;
        }

        BenchmarkDriver drv;

        if ((drv = ldr.loadBenchmarkClass(BenchmarkDriver.class, name)) != null) {
            if (cfg.help()) {
                println(cfg, drv.usage());

                return;
            }

            drv.setUp(cfg);

            Collection<BenchmarkProbe> probes = drv.probes();

            if (probes == null || probes.isEmpty()) {
                errorHelp(cfg, "No probes provided by benchmark driver (stopping benchmark): " + name);

                return;
            }

            final BenchmarkRunner runner = new BenchmarkRunner(cfg, drv, new BenchmarkProbeSet(drv, cfg, probes, ldr));

            if (cfg.shutdownHook()) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override public void run() {
                        try {
                            runner.cancel();
                        }
                        catch (Exception e) {
                            errorHelp(cfg, "Exception is raised during runner cancellation.", e);
                        }
                    }
                });
            }

            // Runner will shutdown driver.
            runner.runBenchmark();
        }
        else {
            errorHelp(cfg, "Could not find runner class name in classpath: " + name +
                ".\nMake sure class name is specified correctly and corresponding package is added " +
                "to -p argument list.");
        }
    }
}
