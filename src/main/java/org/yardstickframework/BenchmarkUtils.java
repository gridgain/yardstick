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
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Benchmark utility methods.
 */
public class BenchmarkUtils {
    /** Date format. */
    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("<HH:mm:ss>");

    /** Weight delimiter. */
    public static final String WEIGHT_DELIMITER = ":";

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
        System.out.println(DATE_FMT.format(new Date()) + "<yardstick> " + msg);
    }

    /**
     * Prints error message.
     *
     * @param msg Error message.
     * @param err Exception to print.
     */
    public static void error(String msg, Throwable err) {
        System.err.println(DATE_FMT.format(new Date()) + "<yardstick> " + msg);

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

        System.out.println(DATE_FMT.format(new Date()) + "<yardstick> " + msg + name);
    }

    /**
     * Prints message.
     *
     * @param cfg Benchmark configuration.
     * @param msg Message.
     */
    public static void println(BenchmarkConfiguration cfg, String msg) {
        cfg.output().println(DATE_FMT.format(new Date()) + "<yardstick> " + msg);
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
}
