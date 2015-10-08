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

package org.yardstickframework.impl.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Process launcher.
 */
public class BenchmarkProcessLauncher {
    /** */
    private Process proc;

    /** */
    private StreamGrabber inGrabber;

    /** */
    private StreamGrabber errGrabber;

    /**
     * @param cmdParams Command parameters.
     * @param envVars Environment variables.
     * @param printC Print closure.
     * @throws Exception If failed.
     */
    public void exec(Collection<String> cmdParams, Map<String, String> envVars,
        @Nullable BenchmarkClosure<String> printC) throws Exception {
        List<String> procCommands = new ArrayList<>();

        procCommands.addAll(cmdParams);

        ProcessBuilder builder = new ProcessBuilder();

        for (Map.Entry<String, String> entry : envVars.entrySet())
            builder.environment().put(entry.getKey(), entry.getValue());

        builder.command(procCommands);

        proc = builder.start();

        inGrabber = new StreamGrabber(proc.getInputStream(), printC);

        errGrabber = new StreamGrabber(proc.getErrorStream(), printC);

        inGrabber.start();
        errGrabber.start();
    }

    /**
     * @return Process.
     */
    public Process process() {
        return proc;
    }

    /**
     * @param wait {@code True} to wait for the process to finish, {@code false} otherwise.
     * @throws Exception If failed.
     */
    public void shutdown(boolean wait) throws Exception {
        if (proc != null) {
            if (wait)
                proc.waitFor();
            else
                proc.destroy();

            inGrabber.stop();
            errGrabber.stop();
        }
    }

    /**
     *
     */
    private static class StreamGrabber {
        /** */
        private static final BenchmarkClosure<String> LOG_CLOSURE = new BenchmarkClosure<String>() {
            @Override public void apply(String s) {
                System.out.println(s);
            }
        };

        /** */
        private final Thread thread;

        /** */
        private final InputStream is;

        /**
         * @param is Stream.
         * @param printC Print closure.
         */
        StreamGrabber(final InputStream is, @Nullable final BenchmarkClosure<String> printC) {
            thread = new Thread(new Runnable() {
                @Override public void run() {
                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(is));

                        for (String line; (line = br.readLine()) != null && !thread.isInterrupted(); )
                            (printC == null ? LOG_CLOSURE : printC).apply(line);
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

            this.is = is;
        }

        /**
         * Starts thread.
         */
        public void start() {
            thread.start();
        }

        /**
         * Stops thread.
         * @throws Exception If failed.
         */
        public void stop() throws Exception {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException ignored) {
                    // No-op.
                }
            }

            thread.interrupt();
            thread.join();
        }
    }
}
