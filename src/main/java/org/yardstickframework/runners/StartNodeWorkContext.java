package org.yardstickframework.runners;

import java.util.List;

public class StartNodeWorkContext extends CommonWorkContext{

    private RunMode runMode;

    private String fullCfgStr;

    private String propPath;

    private BuildDockerResult dockerInfo;

    public StartNodeWorkContext(List<String> hostList, RunMode runMode, String fullCfgStr) {
        super(hostList);
        this.runMode = runMode;
        this.fullCfgStr = fullCfgStr;
    }

    public RunMode getRunMode() {
        return runMode;
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
