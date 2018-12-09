package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

public class NodeInfo implements WorkResult {

    private NodeType nodeType;

    private String host;

    private String port;

    private String id;

    private String paramStr;

    private String logPath;

    private RunMode runMode;

    private DockerInfo dockerInfo;

    private CommandExecutionResult cmdExRes;

    private RestartInfo restCtx;

    private List<String> errMsgs = new ArrayList<>();

    public List<String> getErrMsgs() {
        return errMsgs;
    }

    public NodeStatus nodeStatus;

    public String nodeStartTime;

    public String descript;

    public NodeInfo(NodeType nodeType, String host, @Nullable String port, String id, RunMode runMode) {
        this.nodeType = nodeType;
        this.host = host;
        this.port = port;
        this.id = id;
        this.runMode = runMode;
    }

    public NodeInfo(NodeType nodeType, String host, @Nullable String port, String id,
        String paramStr, String logPath) {
        this.nodeType = nodeType;
        this.host = host;
        this.port = port;
        this.id = id;
        this.paramStr = paramStr;
        this.logPath = logPath;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getParamStr() {
        return paramStr;
    }

    public void setParamStr(String paramStr) {
        this.paramStr = paramStr;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public CommandExecutionResult getCmdExRes() {
        return cmdExRes;
    }

    public void setCmdExRes(CommandExecutionResult cmdExRes) {
        this.cmdExRes = cmdExRes;
    }

    public String typeLow() {
        return nodeType.toString().toLowerCase();
    }

    public String toShortStr(){
        return String.format("%s%s", typeLow(), id);
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
     * @param runMode New run mode.
     */
    public void runMode(RunMode runMode) {
        this.runMode = runMode;
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
     * @return Descript.
     */
    public String descript() {
        return descript;
    }

    /**
     * @param descript New descript.
     */
    public void descript(String descript) {
        this.descript = descript;
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
     * @return Logger path.
     */
    public String loggerPath() {
        return logPath;
    }

    /**
     * @param logPath New logger path.
     */
    public void loggerPath(String logPath) {
        this.logPath = logPath;
    }
}
