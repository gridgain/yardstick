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

package org.yardstickframework.examples.echo;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.BenchmarkDriverAdapter;
import org.yardstickframework.BenchmarkUtils;

import static org.yardstickframework.BenchmarkUtils.println;

/**
 * Echo server benchmark. This benchmark has client and server counterparts.
 */
public class EchoBenchmark extends BenchmarkDriverAdapter {
    /** Counter. */
    private final AtomicInteger cntr = new AtomicInteger();

    /** Thread to socket map. */
    private final ConcurrentMap<Thread, Socket> sockMap = new ConcurrentHashMap<>();

    /** Arguments. */
    private final EchoBenchmarkArguments args = new EchoBenchmarkArguments();

    /** {@inheritDoc} */
    @Override public void setUp(BenchmarkConfiguration cfg) throws Exception {
        super.setUp(cfg);

        println("Started benchmark with id" + cfg.memberId());

        BenchmarkUtils.jcommander(cfg.commandLineArguments(), args, "<echo-driver>");

        // Check if EchoServer is up.
        createSocket(args);
    }

    /** {@inheritDoc} */
    @Override public void tearDown() throws Exception {
        for (Socket sock : sockMap.values())
            sock.close();
    }

    /** {@inheritDoc} */
    @Override public boolean test(Map<Object, Object> ctx) throws Exception {
        Socket sock = socket(args);

        String req = "ping-" + cntr.incrementAndGet();

        byte[] reqBytes = req.getBytes();

        sock.getOutputStream().write(reqBytes);

        byte[] resBytes = new byte[reqBytes.length];

        InputStream in = sock.getInputStream();

        int read = 0;

        while (read < resBytes.length) {
            int b = in.read(resBytes, read, resBytes.length - read);

            if (b < 0)
                break;

            read += b;
        }

        String res = new String(resBytes);

        if (!req.equals(res))
            throw new Exception("Invalid echo response [req=" + req + ", res=" + res + ']');

        return true;
    }

    /** {@inheritDoc} */
    @Override public String description() {
        String desc = BenchmarkUtils.description(cfg, this);

        return desc.isEmpty() ?
            getClass().getSimpleName() + args.description() + cfg.defaultDescription() : desc;
    }

    /** {@inheritDoc} */
    @Override public String usage() {
        return BenchmarkUtils.usage(args);
    }

    /**
     * Initialize socket per thread.
     *
     * @param args Echo server arguments.
     * @return Socket for this thread.
     * @throws Exception If failed.
     */
    private Socket socket(EchoBenchmarkArguments args) throws Exception {
        Socket sock = sockMap.get(Thread.currentThread());

        if (sock == null) {
            sock = createSocket(args);

            Socket old = sockMap.putIfAbsent(Thread.currentThread(), sock);

            if (old != null)
                sock = old;
        }

        return sock;
    }

    /**
     * Creates socket.
     *
     * @param args Arguments.
     * @return Created socket.
     * @throws Exception If failed.
     */
    private static Socket createSocket(EchoBenchmarkArguments args) throws Exception {
        try {
            return new Socket(args.localBind(), args.port());
        }
        catch (IOException e) {
            throw new Exception("Can not connect to EchoServer, is server running?", e);
        }
    }
}
