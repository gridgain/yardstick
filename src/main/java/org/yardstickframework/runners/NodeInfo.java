package org.yardstickframework.runners;

import javax.annotation.Nullable;

public class NodeInfo implements WorkResult {

    private NodeType nodeType;

    private String host;

    private String port;

    private String id;

    private String startCmd;

    private String logPath;

    private StartNodeWorkContext startCtx;

    private DockerInfo dockerInfo;

    private CommandExecutionResult cmdExRes;

    private RestartInfo restCtx;

    public NodeInfo(NodeType nodeType, String host, @Nullable String port, String id, StartNodeWorkContext startCtx,
        String startCmd, String logPath) {
        this.nodeType = nodeType;
        this.host = host;
        this.port = port;
        this.id = id;
        this.startCtx = startCtx;
        this.startCmd = startCmd;
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

    public StartNodeWorkContext getStartCtx() {
        return startCtx;
    }

    public String getStartCmd() {
        return startCmd;
    }

    public void setStartCmd(String startCmd) {
        this.startCmd = startCmd;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public DockerInfo getDockerInfo() {
        return dockerInfo;
    }

    public void setDockerInfo(DockerInfo dockerInfo) {
        this.dockerInfo = dockerInfo;
    }

    public void setStartCtx(StartNodeWorkContext startCtx) {
        this.startCtx = startCtx;
    }

    public CommandExecutionResult getCmdExRes() {
        return cmdExRes;
    }

    public void setCmdExRes(CommandExecutionResult cmdExRes) {
        this.cmdExRes = cmdExRes;
    }

    public String typeLow(){
        return nodeType.toString().toLowerCase();
    }
}
