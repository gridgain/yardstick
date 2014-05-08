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

package org.yardstick.examples.echo;

import org.yardstick.*;
import org.yardstick.impl.util.*;

import java.io.*;
import java.net.*;

/**
 * Echo server.
 */
public class EchoServer implements BenchmarkServer {
    /** Echo server benchmark arguments. */
    private final EchoServerBenchmarkArguments args = new EchoServerBenchmarkArguments();

    /** Echo thread. */
    private Thread th;

    /** {@inheritDoc} */
    @Override public void start(final BenchmarkConfiguration cfg) throws Exception {
        BenchmarkUtils.jcommander(cfg.commandLineArguments(), args, "<echo-server>");

        th = new Thread(new Runnable() {
            @Override public void run() {
                try (ServerSocket srvrSock = new ServerSocket(args.port(), 50, InetAddress.getByName(args.host()))) {
                    while (!Thread.interrupted()) {
                        final Socket sock = srvrSock.accept();

                        Thread t = new Thread(new Runnable() {
                            @Override public void run() {
                                try {
                                    InputStream in = sock.getInputStream();
                                    OutputStream out = sock.getOutputStream();

                                    int b;

                                    // Echo input to output.
                                    while ((b = in.read()) > 0)
                                        out.write(b);
                                }
                                catch (IOException e) {
                                    e.printStackTrace(cfg.error());
                                }
                                finally {
                                    try {
                                        sock.close();
                                    }
                                    catch (IOException e) {
                                        e.printStackTrace(cfg.error());
                                    }
                                }
                            }
                        });

                        t.setDaemon(true);

                        t.start();
                    }
                }
                catch (IOException e) {
                    e.printStackTrace(cfg.error());
                }
            }
        });

        th.start();
    }

    /** {@inheritDoc} */
    @Override public void stop() throws Exception {
        th.interrupt();

        th.join();
    }

    /** {@inheritDoc} */
    @Override public String usage() {
        return BenchmarkUtils.usage(args);
    }
}
