package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkConfiguration;

import static org.yardstickframework.BenchmarkUtils.jcommander;

public class StartNodeWorkContext extends CommonWorkContext{

    private StartMode startMode;

    private String fullCfgStr;

    private String propPath;

    private BuildDockerResult dockerInfo;

    public StartNodeWorkContext(List<String> hostList, StartMode startMode, String fullCfgStr, String propPath) {
        super(hostList);
        this.startMode = startMode;
        this.fullCfgStr = fullCfgStr;
        this.propPath = propPath;
    }

    public StartMode getStartMode() {
        return startMode;
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
