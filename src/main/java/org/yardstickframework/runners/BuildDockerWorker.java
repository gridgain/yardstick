package org.yardstickframework.runners;

import java.util.List;
import java.util.Properties;

public class BuildDockerWorker extends Worker{

    public BuildDockerWorker(Properties runProps) {
        super(runProps);
    }

    @Override public WorkResult doWork(String ip, int cnt, int total, WorkContext workCtx) {
        WorkResult res = new BuildDockerResult();

        String buildDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s %s/bin/build-docker.sh",
            ip, getMainDir());

        runCmd(buildDockerCmd);

        return res;
    }

    @Override public List<String> getHostList() {
        return getFullUniqList();
    }
}
