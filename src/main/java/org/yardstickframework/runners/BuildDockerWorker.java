package org.yardstickframework.runners;

import java.util.Properties;

public class BuildDockerWorker extends Worker{

    public BuildDockerWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        BuildDockerWorkContext ctx = (BuildDockerWorkContext)getWorkContext();

        String path = ctx.getDockerFilePath();

        String imageName = ctx.getImageName();

        String imageVer = ctx.getImageVer();

        String buildDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s %s/bin/build-docker.sh %s %s %s",
            ip, getMainDir(), path, imageName, imageVer);

        runCmd(buildDockerCmd);

        return new BuildDockerResult(imageName, imageVer, ip, cnt);
    }
}
