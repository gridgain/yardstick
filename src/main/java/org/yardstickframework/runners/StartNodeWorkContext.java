package org.yardstickframework.runners;

import java.util.List;

public class StartNodeWorkContext extends CommonWorkContext{

    private RunMode runMode;

    private NodeType nodeType;

    private String fullCfgStr;

    private String propPath;

    private BuildDockerResult dockerInfo;

    public StartNodeWorkContext(List<String> hostList, RunMode runMode, NodeType nodeType, String fullCfgStr) {
        super(hostList);
        this.runMode = runMode;
        this.nodeType = nodeType;
        this.fullCfgStr = fullCfgStr;
    }

    public RunMode getRunMode() {
        return runMode;
    }

    public NodeType getNodeType() {
        return nodeType;
    }

    public String getFullCfgStr() {
        return fullCfgStr;
    }

    public String getPropPath() {
        return propPath;
    }

    public BuildDockerResult getDockerInfo() {
        return dockerInfo;
    }

    public void setDockerInfo(BuildDockerResult dockerInfo) {
        this.dockerInfo = dockerInfo;
    }
}
