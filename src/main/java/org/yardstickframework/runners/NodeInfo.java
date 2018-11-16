package org.yardstickframework.runners;

public class NodeInfo implements WorkResult {

    private NodeType nodeType;

    private String host;

    private String port;

    private String id;

    private String startCmd;

    private String logPath;

    private DockerInfo dockerInfo;

    public NodeInfo(NodeType nodeType, String host, String port, String id, String startCmd, String logPath) {
        this.nodeType = nodeType;
        this.host = host;
        this.port = port;
        this.id = id;
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
}
