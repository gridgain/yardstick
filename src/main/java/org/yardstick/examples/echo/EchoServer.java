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

import java.io.*;
import java.net.*;

import static org.yardstick.BenchmarkUtils.*;

/**
 * Echo server.
 */
public class EchoServer implements BenchmarkServer {
    /** Echo server benchmark arguments. */
    private final EchoBenchmarkArguments args = new EchoBenchmarkArguments();

    /** Echo thread. */
    private Thread th;

    /** {@inheritDoc} */
    @Override public void start(final BenchmarkConfiguration cfg) throws Exception {
        BenchmarkUtils.jcommander(cfg.commandLineArguments(), args, "<echo-server>");

        th = new Thread(new Runnable() {
            @Override public void run() {
                try (ServerSocket srvrSock = newServerSocket(args)) {
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
                                    errorHelp(cfg, "Exception is raised.", e);
                                }
                                finally {
                                    try {
                                        sock.close();
                                    }
                                    catch (IOException e) {
                                        errorHelp(cfg, "Exception is raised.", e);
                                    }
                                }
                            }
                        });

                        t.setDaemon(true);

                        t.start();
                    }
                }
                catch (IOException e) {
                    errorHelp(cfg, "Exception is raised.", e);
                }
            }
        });

        th.start();
    }

    /**
     * Creates new server socket within port range.
     *
     * @param args Benchmark arguments.
     * @return New server socket.
     * @throws IOException If failed.
     */
    private ServerSocket newServerSocket(EchoBenchmarkArguments args) throws IOException {
        IOException ex = null;

        for (int port = args.port(); port < args.port() + 10; port++) {
            try {
                return new ServerSocket(args.port(), 50, InetAddress.getByName(args.localBind()));
            }
            catch (UnknownHostException e) {
                throw e;
            }
            catch (IOException e) {
                ex = e;
            }
        }

        throw new IOException("Failed to bind server socket to any port [localBind=" + args.localBind() +
            ", portRange=" + args.port() + ".." + (args.port() + 10) + ']', ex);
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
