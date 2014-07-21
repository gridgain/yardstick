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

package org.yardstickframework;

import org.yardstickframework.impl.*;

import java.util.*;

import static org.yardstickframework.BenchmarkUtils.*;

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

        List<String> names = cfg.driverNames();

        if (names == null || names.isEmpty()) {
            errorHelp(cfg, "Driver class names are not specified.");

            return;
        }

        List<BenchmarkDriver> drivers = new ArrayList<>();

        List<Integer> weights = new ArrayList<>();

        Set<String> driverNames = new HashSet<>();

        for (String nameWithWeight : names) {
            nameWithWeight = nameWithWeight.trim();

            if (nameWithWeight.isEmpty())
                continue;

            String[] tokens = nameWithWeight.split(":");

            String name = tokens[0].trim();

            String weight = tokens.length == 1 ? "1" : tokens[1].trim();

            try {
                weights.add(Integer.parseInt(weight));
            }
            catch (NumberFormatException ignored) {
                errorHelp(cfg, "Can not parse driver run weight [driver=" + name + ", weight=" + weight + "]");

                return;
            }

            BenchmarkDriver drv = ldr.loadClass(BenchmarkDriver.class, name);

            if (drv == null) {
                errorHelp(cfg, "Could not find benchmark driver class name in classpath: " + name +
                    ".\nMake sure class name is specified correctly and corresponding package is added " +
                    "to -p argument list.");

                return;
            }

            drivers.add(drv);

            driverNames.add(name);
        }

        if (drivers.isEmpty()) {
            errorHelp(cfg, "Drivers are not found.");

            return;
        }

        if (driverNames.size() < drivers.size()) {
            errorHelp(cfg, "Multiple drivers with the same name are found.");

            return;
        }

        if (cfg.help()) {
            println(cfg, drivers.get(0).usage());

            return;
        }

        BenchmarkProbeSet[] probeSets = new BenchmarkProbeSet[drivers.size()];

        try {
            for (int i = 0; i < drivers.size(); i++) {
                Collection<BenchmarkProbe> probes = ldr.loadProbes();

                if (probes == null || probes.isEmpty()) {
                    errorHelp(cfg, "No probes provided by benchmark driver (stopping benchmark): " + names);

                    return;
                }

                BenchmarkDriver drv = drivers.get(i);

                probeSets[i] = new BenchmarkProbeSet(drv, cfg, probes, ldr);

                drv.setUp(cfg);
            }
        }
        catch (Throwable e) {
            BenchmarkUtils.error("Failed to set up benchmark drivers (will shutdown and exit).", e);

            for (BenchmarkDriver drv : drivers) {
                try {
                    drv.tearDown();
                }
                catch (Exception ex) {
                    BenchmarkUtils.println("Failed to tear down benchmark driver (will ignore) " +
                        "[drv=" + drv.description() + ", msg=" + ex.getMessage() + ']');
                }
            }

            return;
        }

        int[] weights0 = new int[weights.size()];

        for (int i = 0; i < weights.size(); i++)
            weights0[i] = weights.get(i);

        final BenchmarkRunner runner = new BenchmarkRunner(cfg, drivers.toArray(new BenchmarkDriver[drivers.size()]),
            probeSets, weights0);

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
}
