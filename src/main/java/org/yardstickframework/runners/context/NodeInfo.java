package org.yardstickframework.runners.context;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import org.yardstickframework.BenchmarkConfiguration;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Node info.
 */
public class NodeInfo implements WorkResult {
    /** */
    private NodeType nodeType;

    /** */
    private String host;

    /** */
    private String port;

    /** */
    private String id;

    /** */
    private String paramStr;

    /** */
    private String logPath;

    /** */
    private RunMode runMode;

    /** */
    private DockerInfo dockerInfo;

    /** */
    private CommandExecutionResult cmdExRes;

    /** */
    private List<String> errMsg = new ArrayList<>();

    /** */
    private NodeStatus nodeStatus;

    /** */
    private String nodeStartTime;

    /** */
    private String descr;

    /** */
    private BenchmarkConfiguration cfg;

    /**
     * Constructor.
     *
     * @param nodeType Node type.
     * @param host Host.
     * @param port Port.
     * @param id Id.
     * @param runMode Run Mode.
     */
    public NodeInfo(NodeType nodeType, String host, @Nullable String port, String id, RunMode runMode) {
        this.nodeType = nodeType;
        this.host = host;
        this.port = port;
        this.id = id;
        this.runMode = runMode;
    }

    /**
     * @return Node status.
     */
    public NodeStatus nodeStatus() {
        return nodeStatus;
    }

    /**
     * @param nodeStatus New node status.
     */
    public void nodeStatus(NodeStatus nodeStatus) {
        this.nodeStatus = nodeStatus;
    }

    /**
     * @return Run mode.
     */
    public RunMode runMode() {
        return runMode;
    }

    /**
     * @return Node start time.
     */
    public String nodeStartTime() {
        return nodeStartTime;
    }

    /**
     * @param nodeStartTime New node start time.
     */
    public void nodeStartTime(String nodeStartTime) {
        this.nodeStartTime = nodeStartTime;
    }

    /**
     * @return Description.
     */
    public String description() {
        return descr;
    }

    /**
     * @param descr New description.
     */
    public void description(String descr) {
        this.descr = descr;
    }

    /**
     * @return Docker info.
     */
    public DockerInfo dockerInfo() {
        return dockerInfo;
    }

    /**
     * @param dockerInfo New docker info.
     */
    public void dockerInfo(DockerInfo dockerInfo) {
        this.dockerInfo = dockerInfo;
    }

    /**
     * @return Parameter string.
     */
    public String parameterString() {
        return paramStr;
    }

    /**
     * @param paramStr New parameter string.
     */
    public void parameterString(String paramStr) {
        this.paramStr = paramStr;
    }

    /**
     * @return Log path.
     */
    public String logPath() {
        return logPath;
    }

    /**
     * @param logPath New log path.
     */
    public void logPath(String logPath) {
        this.logPath = logPath;
    }

    /**
     * @return Command execution result.
     */
    public CommandExecutionResult commandExecutionResult() {
        return cmdExRes;
    }

    /**
     * @param cmdExRes Command execution result.
     */
    public void commandExecutionResult(CommandExecutionResult cmdExRes) {
        this.cmdExRes = cmdExRes;
    }

    /**
     * @return Id.
     */
    public String id() {
        return id;
    }

    /**
     * @param id New id.
     */
    public void id(String id) {
        this.id = id;
    }

    /**
     * @return Node type.
     */
    public NodeType nodeType() {
        return nodeType;
    }

    /**
     * @param nodeType New node type.
     */
    public void nodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    /**
     * @return Host.
     */
    public String host() {
        return host;
    }

    /**
     * @param host New host.
     */
    public void host(String host) {
        this.host = host;
    }

    /**
     * @return Port.
     */
    public String port() {
        return port;
    }

    /**
     * @param port New port.
     */
    public void port(String port) {
        this.port = port;
    }

    /**
     * @return Error messages.
     */
    public List<String> errorMessages() {
        return new ArrayList<>(errMsg);
    }

    /**
     * @param errMsg New error messages.
     */
    public void errorMessages(List<String> errMsg) {
        this.errMsg = new ArrayList<>(errMsg);
    }

    /**
     * @return {@code String} Node type in lower case..
     */
    public String typeLow() {
        return nodeType.toString().toLowerCase();
    }

    /**
     * @return {@code String} Short string e.g. server0.
     */
    public String toShortStr() {
        return String.format("%s%s", typeLow(), id);
    }

    /**
     * @return Config.
     */
    public BenchmarkConfiguration config() {
        return cfg;
    }

    /**
     * @param cfg New config.
     */
    public void config(BenchmarkConfiguration cfg) {
        this.cfg = cfg;
    }
}
