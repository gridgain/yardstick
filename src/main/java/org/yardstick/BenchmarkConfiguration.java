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

import com.beust.jcommander.*;

import java.io.*;
import java.util.*;

/**
 * Input arguments for benchmarks.
 */
public class BenchmarkConfiguration {
    /** */
    @SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
    @Parameter(names = {"-c", "--config"}, description = "Framework configuration file path")
    private String propsFileName = "config/benchmark.properties";

    /** */
    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = {"-dn", "--driverName"}, description = "Benchmark driver name (required)")
    private String driverName;

    /** */
    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = {"-sn", "--serverName"}, description = "Benchmark server name (required)")
    private String serverName;

    /** */
    @Parameter(names = {"-p", "--packages"}, description = "Comma separated list of packages for benchmarks")
    private List<String> packages = Collections.emptyList();

    /** */
    @Parameter(names = {"--probes"}, description = "Comma separated list of probes for benchmarks")
    private List<String> dfltProbeClsNames = Collections.emptyList();

    /** Default probes. */
    private List<BenchmarkProbe> dfltProbes = Collections.emptyList();

    /** Probe writer class name. */
    @Parameter(names = {"--writer"}, description = "Probe point writer class name")
    private String probeWriter;

    /** */
    @Parameter(names = {"-t", "--threads"}, description = "Thread count (set to 'cpus * 2')")
    private int threads = Runtime.getRuntime().availableProcessors() * 2;

    /** */
    @Parameter(names = {"-d", "--duration"}, description = "Duration, in seconds")
    private long duration = 40;

    /** */
    @Parameter(names = {"-w", "--warmup"}, description = "Warmup, in seconds")
    private long warmup = 20;

    /** */
    @Parameter(names = {"-sh", "--shutdown"}, description = "Invoke shutdown hook")
    private boolean shutdownHook = true;

    /** */
    @Parameter(names = {"-of", "--outputFolder"}, description = "Output folder for benchmark results")
    private String outputFolder;

    /** */
    @SuppressWarnings("UnusedDeclaration")
    @Parameter(names = { "-h", "--help" }, description = "Print help message", help = true, hidden = true)
    private boolean help;

    /** Non-parsed command line arguments. */
    private String[] cmdArgs;

    /** Custom properties. */
    private Map<String, String> customProps;

    /** Output writer. */
    private PrintStream outputWriter;

    /** Error writer. */
    private PrintStream errorWriter;

    /** */
    private String description;

    /**
     * @return Properties file name.
     */
    public String propertiesFileName() {
        return propsFileName;
    }

    /**
     * @return If help requested.
     */
    public boolean help() {
        return help;
    }

    /**
     * @return Benchmark server name.
     */
    public String serverName() {
        return serverName;
    }

    /**
     * @return Benchmark driver name.
     */
    public String driverName() {
        return driverName;
    }

    /**
     * @return List of packages to load benchmarks from.
     */
    public List<String> packages() {
        return packages;
    }

    /**
     * @param packages List of packages to load benchmarks from.
     */
    public void packages(List<String> packages) {
        this.packages = packages;
    }

    /**
     * @return Threads.
     */
    public int threads() {
        return threads;
    }

    /**
     * @param threads Number of threads to run benchmark on.
     */
    public void threads(int threads) {
        this.threads = threads;
    }

    /**
     * @return Duration, in seconds.
     */
    public long duration() {
        return duration;
    }

    /**
     * @param duration Duration, in seconds.
     */
    public void duration(long duration) {
        this.duration = duration;
    }

    /**
     * @return Warmup, in seconds.
     */
    public long warmup() {
        return warmup;
    }

    /**
     * @param warmup Warmup time, in seconds.
     */
    public void warmup(long warmup) {
        this.warmup = warmup;
    }

    /**
     * @return {@code True} if shutdown hook should be added ({@code true} by default).
     */
    public boolean shutdownHook() {
        return shutdownHook;
    }

    /**
     * @param shutdownHook {@code True} if shutdown hook should be added.
     */
    public void shutdownHook(boolean shutdownHook) {
        this.shutdownHook = shutdownHook;
    }

    /**
     * @return Custom properties map.
     */
    public Map<String, String> customProperties() {
        return customProps;
    }

    /**
     * @param customProps Custom properties map.
     */
    public void customProperties(Map<String, String> customProps) {
        this.customProps = customProps;
    }

    /**
     * @return Output writer.
     */
    public PrintStream output() {
        return outputWriter;
    }

    /**
     * @param outputWriter Output writer.
     */
    public void output(PrintStream outputWriter) {
        this.outputWriter = outputWriter;
    }

    /**
     * @return Error writer.
     */
    public PrintStream error() {
        return errorWriter;
    }

    /**
     * @param errorWriter Error writer.
     */
    public void error(PrintStream errorWriter) {
        this.errorWriter = errorWriter;
    }

    /**
     * @return Command line arguments list.
     */
    public String[] commandLineArguments() {
        return cmdArgs;
    }

    /**
     * @param cmdArgs Command line arguments.
     */
    public void commandLineArguments(String[] cmdArgs) {
        this.cmdArgs = cmdArgs;
    }

    /**
     * @return Gets list of default probe class names.
     */
    public List<String> defaultProbeClassNames() {
        return dfltProbeClsNames;
    }

    /**
     * @param dfltProbeClsNames List of default probe class names.
     */
    public void defaultProbeClassNames(List<String> dfltProbeClsNames) {
        this.dfltProbeClsNames = dfltProbeClsNames;
    }

    /**
     * @return List of default probes.
     */
    public List<BenchmarkProbe> defaultProbes() {
        return dfltProbes;
    }

    /**
     * @param dfltProbes List of default probes.
     */
    public void defaultProbes(List<BenchmarkProbe> dfltProbes) {
        this.dfltProbes = dfltProbes;
    }

    /**
     * @return Probe writer class name.
     */
    public String probeWriterClassName() {
        return probeWriter;
    }

    /**
     * @param probeWriter Probe writer class name.
     */
    public void probeWriterClassName(String probeWriter) {
        this.probeWriter = probeWriter;
    }

    /**
     * @return Description.
     */
    public String description() {
        return description;
    }

    /**
     * @param description Description.
     */
    public void description(String description) {
        this.description = description;
    }

    /**
     * @return Output folder.
     */
    public String outputFolder() {
        return outputFolder;
    }

    /**
     * @return Description.
     */
    public String parametersToString() {
        return "-t=" + threads + "_-d=" + duration + "_-w=" + warmup;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return "BenchmarkConfiguration [" +
            "serverName='" + serverName + '\'' +
            ", driverName='" + driverName + '\'' +
            ", threads=" + threads +
            ", duration=" + duration +
            ", warmup=" + warmup +
            ", dfltProbeClsNames=" + dfltProbeClsNames +
            ", propsFileName='" + propsFileName + '\'' +
            ", dfltProbes=" + dfltProbes +
            ", packages=" + packages +
            ", cmdArgs=" + Arrays.toString(cmdArgs) +
            ", probeWriter='" + probeWriter + '\'' +
            ", customProps=" + customProps +
            ", shutdownHook=" + shutdownHook +
            ", outputFolder=" + outputFolder +
            ", outputWriter=" + outputWriter +
            ", errorWriter=" + errorWriter +
            ']';
    }
}
