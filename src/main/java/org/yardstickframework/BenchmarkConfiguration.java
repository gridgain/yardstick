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

import com.beust.jcommander.Parameter;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Input arguments for benchmarks.
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal"})
public class BenchmarkConfiguration implements Serializable {
    /** */
    private static final long serialVersionUID = 0L;

    /** */
    @Parameter(names = {"-cfg", "--config"}, description = "Framework configuration file path")
    private String propsFileName = "config/benchmark.properties";

    /** For internal use. Should not be used in configs. */
    @Parameter(names = {"--logsFolder"}, description = "Logs directory")
    private String logsFolder;

    /** For internal use. Should not be used in configs. */
    @Parameter(names = {"--currentFolder"}, description = "Current folder")
    private String curFolder;

    /** For internal use. Should not be used in configs. */
    @Parameter(names = {"--scriptsFolder"}, description = "Script folder")
    private String scriptsFolder;

    /** */
    @Parameter(names = {"-dn", "--driverNames"}, variableArity = true,
        description = "Space-separated list of Benchmark driver names (required)")
    private List<String> driverNames;

    /** */
    @Parameter(names = {"-sn", "--serverName"},
        description = "Space-separated list of benchmark server name (required), maximum size is 4")
    private List<String> serverName;

    /** */
    @Parameter(names = {"-nn", "--serverNameNumber"}, description = "Number of the benchmark server name (default 0, max 3)")
    private int serverNameNumber;

    /** */
    @Parameter(names = {"-id", "--memberId"}, description = "Memebr ID")
    private int memberId = -1;

    /** */
    @Parameter(names = {"-p", "--packages"}, description = "Comma separated list of packages for benchmarks")
    private List<String> packages = Collections.emptyList();

    /** */
    @Parameter(names = {"-pr", "--probes"}, description = "Comma separated list of probes for benchmarks")
    private List<String> dfltProbeClsNames = Collections.emptyList();

    /** Probe writer class name. */
    @Parameter(names = {"wr", "--writer"}, description = "Probe point writer class name")
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
    @Parameter(names = {"-ops", "--operations"}, description = "Operations (0 is infinite, default)")
    private int opsCnt;

    /** */
    @Parameter(names = {"-sh", "--shutdown"}, description = "Invoke shutdown hook")
    private boolean shutdownHook = true;

    /** */
    @Parameter(names = {"-of", "--outputFolder"}, description = "Output folder for benchmark results")
    private String outputFolder;

    /** */
    @Parameter(names = {"-ds", "--descriptions"}, variableArity = true,
        description = "Space-separated list of benchmark run descriptions")
    private List<String> descs;

    /** */
    @Parameter(names = {"-hn", "--hostName"}, description = "Host name where a benchmark driver is run")
    private String hostName = "";

    /** */
    @Parameter(names = {"-remoteuser", "--remoteuser"}, description = "Remote user name")
    private String remoteUser = "";

    /** */
    @Parameter(names = {"-rhn", "--remoteHostName"}, description = "Remote host name")
    private String remoteHostName = "";

    /** */
    @Parameter(names = {"-lfreq", "--logFreq"}, description = "Iterations log frequency")
    private int logIterFreq = 25000;

    /** */
    @Parameter(names = { "-h", "--help" }, description = "Print help message", help = true, hidden = true)
    private boolean help;

    /** Non-parsed command line arguments. */
    private String[] cmdArgs;

    /** Custom properties. */
    private Map<String, String> customProps;

    /** Output writer. */
    private transient PrintStream outputWriter;

    /** Error writer. */
    private transient PrintStream errorWriter;

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
        return serverName.size() > serverNameNumber ? serverName.get(serverNameNumber) : null;
    }

    /**
     * @return Benchmark driver names.
     */
    public List<String> driverNames() {
        return driverNames;
    }

    /**
     * @return Member ID unique to server or driver.
     */
    public int memberId() {
        return memberId;
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
     * @return Operations count.
     */
    public int operationsCount() {
        return opsCnt;
    }

    /**
     * @param opsCnt Operations count.
     */
    public void operationsCount(int opsCnt) {
        this.opsCnt = opsCnt;
    }

    /**
     * @return Log frequency.
     */
    public int logIterationsFrequency() {
        return logIterFreq;
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
     * @return Script folder.
     */
    public String scriptsFolder() {
        return scriptsFolder;
    }

    /**
     * @return Current folder.
     */
    public String currentFolder() {
        return curFolder;
    }

    /**
     * @return Logs folder.
     */
    public String logsFolder() {
        return logsFolder;
    }

    /**
     * @return Output folder.
     */
    public String outputFolder() {
        return outputFolder;
    }

    /**
     * @return Descriptions.
     */
    public List<String> descriptions() {
        return descs;
    }

    /**
     * @return Host name.
     */
    public String hostName() {
        return hostName;
    }

    /**
     * @return Remote user name.
     */
    public String remoteUser() {
        return remoteUser;
    }

    /**
     * @param remoteUser Remote user name.
     */
    public void remoteUser(String remoteUser) {
        this.remoteUser = remoteUser;
    }

    /**
     * @return Remote host name.
     */
    public String remoteHostName() {
        return remoteHostName;
    }

    /**
     * @param remoteHostName Remote host name.
     */
    public void remoteHostName(String remoteHostName) {
        this.remoteHostName = remoteHostName;
    }

    /**
     * @return Default description.
     */
    public String defaultDescription() {
        return "-t=" + threads + "-d=" + duration + "-w=" + warmup;
    }

    /** {@inheritDoc} */
    @Override public String toString() {
        return getClass().getSimpleName() + " [" +
            "memberId='" + memberId + '\'' +
            ", driverNames='" + driverNames + '\'' +
            ", serverName='" + serverName + '\'' +
            ", serverNameNumber='" + serverNameNumber + '\'' +
            ", threads=" + threads +
            ", duration=" + duration +
            ", warmup=" + warmup +
            ", dfltProbeClsNames=" + dfltProbeClsNames +
            ", propsFileName='" + propsFileName + '\'' +
            ", packages=" + packages +
            ", cmdArgs=" + Arrays.toString(cmdArgs) +
            ", probeWriter='" + probeWriter + '\'' +
            ", customProps=" + customProps +
            ", shutdownHook=" + shutdownHook +
            ", currentFolder=" + curFolder +
            ", scriptsFolder=" + scriptsFolder +
            ", logsFolder=" + logsFolder +
            ", outputFolder=" + outputFolder +
            ", outputWriter=" + outputWriter +
            ", errorWriter=" + errorWriter +
            ", descriptions=" + descs +
            ", hostName=" + hostName +
            ']';
    }
}
