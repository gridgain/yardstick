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

import java.util.Collection;
import java.util.Collections;
import org.yardstickframework.impl.BenchmarkLoader;
import org.yardstickframework.impl.BenchmarkProbeSet;
import org.yardstickframework.impl.BenchmarkServerProbeSet;

import static org.yardstickframework.BenchmarkUtils.errorHelp;
import static org.yardstickframework.BenchmarkUtils.jcommander;
import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Benchmark server startup class.
 */
public class BenchmarkServerStartUp {
    /**
     * @param cmdArgs Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] cmdArgs) throws Exception {
        final BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        cfg.commandLineArguments(cmdArgs);

        jcommander(cmdArgs, cfg, "<benchmark-runner>");

        BenchmarkLoader ldr = new BenchmarkLoader();

        ldr.initialize(cfg);

        String name = cfg.serverName();

        if (name != null)
            name = name.trim();

        if (name == null || name.isEmpty()) {
            errorHelp(cfg, "Server class name is not specified.");

            return;
        }

        BenchmarkServer srv;
        BenchmarkServerProbeSet probeSet = null;

        if ((srv = ldr.loadClass(BenchmarkServer.class, name)) != null) {
            if (cfg.help()) {
                println(cfg, srv.usage());

                return;
            }

            try {
                srv.start(cfg);

                try {
                    Collection<BenchmarkServerProbe> probes = ldr.loadServerProbes();

                    if (probes == null)
                        probes = Collections.emptyList();

                    probeSet = new BenchmarkServerProbeSet(srv, cfg, probes, ldr);

                    probeSet.start();
                }
                catch (Exception e){
                    errorHelp(cfg, "Failed to start server probes. Probes will be disabled.", e);
                }

                final BenchmarkServer srv0 = srv;
                final BenchmarkServerProbeSet probeSet0 = probeSet;

                if (cfg.shutdownHook()) {
                    Runtime.getRuntime().addShutdownHook(new Thread() {
                        @Override public void run() {
                            try {
                                if (probeSet0 != null)
                                    probeSet0.stop();

                                srv0.stop();
                            }
                            catch (Exception e) {
                                errorHelp(cfg, "Exception is raised during server stop.", e);
                            }
                        }
                    });
                }
            }
            catch (Exception e) {
                BenchmarkUtils.error("Failed to start benchmark server (will stop and exit).", e);

                srv.stop();
            }
        }
        else {
            errorHelp(cfg, "Could not find benchmark server class name in classpath: " + name +
                ".\nMake sure class name is specified correctly and corresponding package is added " +
                "to -p argument list.");
        }
    }
}
