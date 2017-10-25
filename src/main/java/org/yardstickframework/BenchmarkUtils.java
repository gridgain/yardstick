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

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParametersDelegate;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Benchmark utility methods.
 */
public class BenchmarkUtils {
    /** Date format. */
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("<HH:mm:ss>");

    /** Time formatter. */
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HHmmss");

    /** Weight delimiter. */
    public static final String WEIGHT_DELIMITER = ":";

    /** Indicates whether current OS is Windows. */
    private static boolean isWin;

    /**
     * Initializes statics.
     */
    static {
        isWin = System.getProperty("os.name").toLowerCase().contains("win");
    }

    /**
     * Initializes instance of {@code JCommander}.
     *
     * @param a Arguments.
     * @param args Custom arguments that should be filled with parsed arguments.
     * @param programName Program name.
     * @return Parses input method parameters and returns {@link JCommander} instance.
     */
    public static JCommander jcommander(String[] a, Object args, String programName) {
        JCommander jCommander = new JCommander();

        jCommander.setAcceptUnknownOptions(true);
        jCommander.setProgramName(programName);
        jCommander.addObject(args);

        jCommander.parse(a);

        return jCommander;
    }

    /**
     * Prints usage string to output.
     *
     * @param args JCommander arguments.
     * @return Usage string.
     */
    public static String usage(Object args) {
        CompositeParameters cp = new CompositeParameters();

        cp.benchmarkArgs = args == null ? new Object() : args;

        JCommander jCommander = new JCommander();

        jCommander.setAcceptUnknownOptions(true);
        jCommander.addObject(cp);

        StringBuilder sb = new StringBuilder();

        jCommander.usage(sb);

        return sb.toString();
    }

    /**
     * Prints message.
     *
     * @param msg Message.
     */
    public static void println(String msg) {
        System.out.println(DATE_FMT.format(new Date()) + '<' + Thread.currentThread().getName() + '>' + "<yardstick> "
            + msg);
    }

    /**
     * Prints error message.
     *
     * @param msg Error message.
     * @param err Exception to print.
     */
    public static void error(String msg, Throwable err) {
        System.err.println(DATE_FMT.format(new Date()) + '<' + Thread.currentThread().getName() + '>' + "<yardstick> "
            + msg);

        if (err != null)
            err.printStackTrace(System.err);
    }

    /**
     * Prints message.
     *
     * @param msg Message.
     * @param f File.
     */
    public static void println(String msg, File f) {
        String name = f.getParent() == null ? "" : f.getParentFile().getName() + File.separator + f.getName();

        System.out.println(DATE_FMT.format(new Date()) + '<' + Thread.currentThread().getName() + '>' + "<yardstick> "
            + msg + name);
    }

    /**
     * Prints message.
     *
     * @param cfg Benchmark configuration.
     * @param msg Message.
     */
    public static void println(BenchmarkConfiguration cfg, String msg) {
        cfg.output().println(DATE_FMT.format(new Date()) + '<' + Thread.currentThread().getName() + '>'
            + "<yardstick> " + msg);
    }

    /**
     * Prints error and help.
     *
     * @param cfg Benchmark configuration.
     * @param msg Error message.
     */
    public static void errorHelp(BenchmarkConfiguration cfg, String msg) {
        cfg.error().println("ERROR: " + msg);
        cfg.error().println("Type '--help' for usage.");
    }

    /**
     * Prints error and help.
     *
     * @param cfg Benchmark configuration.
     * @param msg Error message.
     * @param t Throwable, possibly {@code null}.
     */
    public static void errorHelp(BenchmarkConfiguration cfg, String msg, Throwable t) {
        errorHelp(cfg, msg);

        if (t != null)
            t.printStackTrace(cfg.error());
    }

    /**
     * Fixes folder name if needed.
     *
     * @param fName Folder name.
     * @return Fixed folder name.
     */
    public static String fixFolderName(String fName) {
        return fName.length() > 200 ? fName.substring(0, 200) : fName;
    }

    /**
     * Returns the description corresponding to the given benchmark driver.
     *
     * @param cfg Config.
     * @param drv Driver.
     * @return The description corresponding to the given benchmark driver.
     */
    public static String description(BenchmarkConfiguration cfg, BenchmarkDriver drv) {
        if (cfg.driverNames() == null || cfg.driverNames().isEmpty() ||
            cfg.descriptions() == null || cfg.descriptions().isEmpty())
            return "";

        String simpleName = drv.getClass().getSimpleName();

        for (int i = 0; i < cfg.driverNames().size(); i++) {
            if (simpleName.equals(cfg.driverNames().get(i).split(WEIGHT_DELIMITER)[0].trim())) {
                if (i <= cfg.descriptions().size() - 1)
                    return cfg.descriptions().get(i);

                break;
            }
        }

        return "";
    }

    /**
     * Kills 'Dyardstick.server${ID}' process with -9 option on remote host.
     *
     * @param cfg Benchmark configuration.
     * @param isDebug Is debug flag. If <code>true</code> then result will contain 'out' and 'err' streams output.
     * But it will affect a performance.
     * @return Result of execution.
     */
    public static ProcessExecutionResult kill9Server(BenchmarkConfiguration cfg, boolean isDebug) {
        return executeRemotely(cfg.remoteUser(), cfg.remoteHostName(), isDebug,
            Collections.singletonList("pkill -9 -f 'Dyardstick.server" + cfg.memberId() + "'"));
    }

    /**
     * Runs process with ssh connection and execute commands under ssh.
     *
     * @param remoteUser Remote user.
     * @param hostName Host name.
     * @param isDebug Is debug flag.
     * @param cmds Commands.
     * @return Result.
     */
    private static ProcessExecutionResult executeRemotely(String remoteUser, String hostName,
        boolean isDebug, Iterable<String> cmds) {
        if (isWin)
            throw new UnsupportedOperationException("Unsupported operation for windows.");

        Tuple<Thread, StringBuffer> t1 = null;
        Tuple<Thread, StringBuffer> t2 = null;

        try {
            StringBuilder log = new StringBuilder("RemoteUser=" + remoteUser + ", hostName=" + hostName).append('\n');

            Process p = Runtime.getRuntime().exec("ssh -o PasswordAuthentication=no " + remoteUser + "@" + hostName);

            try(PrintStream out = new PrintStream(p.getOutputStream(), true)) {
                if (isDebug) {
                    t1 = monitorInputStream(p.getInputStream(), "OUT");
                    t2 = monitorInputStream(p.getErrorStream(), "ERR");
                }

                for (String cmd : cmds) {
                    log.append("Executing cmd=").append(cmd).append('\n');

                    out.println(cmd);
                }

                out.println("exit");

                p.waitFor();
            }

            return new ProcessExecutionResult(p.exitValue(), log.toString(),  t1 == null ? "" : t1.val2.toString(),
                t2 == null ? "" : t2.val2.toString());
        }
        catch (Exception err) {
            return new ProcessExecutionResult(err);
        }
        finally {
            if (isDebug) {
                if (t1 != null && t1.val1 != null)
                    t1.val1.interrupt();

                if (t2 != null && t2.val1 != null)
                    t2.val1.interrupt();
            }
        }
    }

    /**
     * Monitors input stream at separate thread.
     *
     * @param in Input stream.
     * @param name Name.
     * @return Tuple of started thread and output string.
     */
    private static Tuple<Thread, StringBuffer> monitorInputStream(final InputStream in, final String name) {
        final StringBuffer sb = new StringBuffer();

        Thread thread = new Thread(new Runnable() {
            @Override public void run() {
                try (BufferedReader input = new BufferedReader(new InputStreamReader(in))) {
                    String l;

                    while (!Thread.currentThread().isInterrupted() && ((l = input.readLine()) != null))
                        sb.append(l).append('\n');
                }
                catch (IOException e) {
                    sb.append(e).append('\n');
                }
            }
        }, name);

        thread.setDaemon(true);

        thread.start();

        return new Tuple<>(thread, sb);
    }

    /**
     * Starts server process on remote host.
     *
     * @param cfg Benchmark configuration.
     * @param isDebug Is debug flag. If <code>true</code> then result will contain 'out' and 'err' streams output.
     * But it will affect a performance.
     * @return Result of execution.
     */
    public static ProcessExecutionResult startServer(BenchmarkConfiguration cfg,
        boolean isDebug) {
        List<String> descriptions = cfg.descriptions();

        String descriptrion = descriptions == null || descriptions.isEmpty() ? "" : "_" + descriptions.get(0);
        String now = TIME_FORMATTER.format(new Date());

        String logFile = cfg.logsFolder() +"/"+ now + "_id" + cfg.memberId() + "_" + cfg.remoteHostName()
            + descriptrion + ".log";

        StringBuilder cmdArgs = new StringBuilder();

        for (String arg : cfg.commandLineArguments())
            cmdArgs.append(arg).append(' ');

        String java = cfg.customProperties().get("JAVA");

        Collection<String> cmds = new ArrayList<>();

        String curFold = cfg.currentFolder();

        if (curFold != null && !curFold.isEmpty())
            cmds.add("cd " + curFold);

        cmds.add("nohup "
            + java + " " + cfg.customProperties().get("JVM_OPTS")
            + " -cp " + cfg.customProperties().get("CLASSPATH")
            + " org.yardstickframework.BenchmarkServerStartUp " + cmdArgs + " > " + logFile + " 2>& 1 &");

        return executeRemotely(cfg.remoteUser(), cfg.remoteHostName(), isDebug, cmds);
    }

    /** */
    private static class CompositeParameters {
        @ParametersDelegate
        private BenchmarkConfiguration cfg = new BenchmarkConfiguration();

        @ParametersDelegate
        private Object benchmarkArgs;
    }

    /**
     * Ensure static class.
     */
    private BenchmarkUtils() {
        // No-op.
    }

    /**
     * Result of executed command.
     */
    public static class ProcessExecutionResult {
        /** */
        private final int exitCode;

        /** */
        private final String log;

        /** Contant of output stream of the process. */
        private final String out;

        /** Contant of error output stream of the process. */
        private final String err;

        /** */
        private final Exception e;

        /**
         * @param exitCode Exit code.
         * @param log Log.
         * @param out Out.
         * @param err Err.
         */
        public ProcessExecutionResult(int exitCode, String log, String out, String err) {
            this.exitCode = exitCode;
            this.log = log;
            this.out = out;
            this.err = err;

            e = null;
        }

        /**
         * @param e Exception.
         */
        public ProcessExecutionResult(Exception e) {
            exitCode = -1;
            out = null;
            err = null;
            log = null;

            this.e = e;
        }

        /**
         * @return Exit code.
         */
        public int getExitCode() {
            return exitCode;
        }

        /**
         * @return Execution log.
         */
        public String getLog() {
            return log;
        }

        /**
         * @return Output of ssh process.
         */
        public String getOutput() {
            return out;
        }

        /**
         * @return Error output of ssh process.
         */
        public String getErrorOutput() {
            return err;
        }

        /**
         * @return Exception.
         */
        public Exception getException() {
            return e;
        }

        /** {@inheritDoc} */
        @Override public String toString() {
            return "Result{\n" +
                "exitCode=" + exitCode + '\n' +
                "log=\n" + log + '\n' +
                "out=\n'" + out + '\n' +
                "err=\n'" + err + '\n' +
                "e=" + e +
                '}';
        }
    }

    /**
     * Tuple.
     *
     * @param <T> Type of first element.
     * @param <K> Type of second element.
     */
    private static class Tuple<T, K> {
        /** */
        private T val1;

        /** */
        private K val2;

        /**
         * @param val1 Value 1.
         * @param val2 Value 2.
         */
        Tuple(T val1, K val2) {
            this.val1 = val1;
            this.val2 = val2;
        }
    }
}
