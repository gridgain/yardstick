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

/**
 * Benchmark server startup class.
 */
public class BenchmarkServerStartUp {
    /**
     * @param cmdArgs Arguments.
     * @throws Exception If failed.
     */
    public static void main(String[] cmdArgs) throws Exception {
        System.out.println("For help use '--help' or '-h' options");
        System.out.println();

        final BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        cfg.commandLineArguments(cmdArgs);

        BenchmarkUtils.jcommander(cmdArgs, cfg, "<benchmark-runner>");

        BenchmarkLoader ldr = new BenchmarkLoader();

        ldr.initialize(cfg);

        String name = cfg.serverName();

        if (name != null)
            name = name.trim();

        if (name == null || name.isEmpty()) {
            cfg.output().println("Server class name is not specified.");

            return;
        }

        BenchmarkServer srv;

        if ((srv = ldr.loadBenchmarkClass(BenchmarkServer.class, name)) != null) {
            if (cfg.help()) {
                cfg.output().println(srv.usage());

                return;
            }

            srv.start(cfg);

            final BenchmarkServer srv0 = srv;

            if (cfg.shutdownHook()) {
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override public void run() {
                        try {
                            srv0.stop();
                        }
                        catch (Exception e) {
                            e.printStackTrace(cfg.error());
                        }
                    }
                });
            }
        }
        else {
            cfg.output().println("Could not find runner class name in classpath: " + name);
            cfg.output().println("Make sure class name is specified correctly and corresponding package is added " +
                "to -p argument list.");
        }
    }
}
