package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class BuildDockerWorker extends Worker{

    public BuildDockerWorker(Properties runProps, WorkContext workCtx) {
        super(runProps, workCtx);
    }

    @Override public WorkResult doWork(String ip, int cnt) {
        BuildDockerWorkContext ctx = (BuildDockerWorkContext)getWorkContext();

        String path = ctx.getDockerFilePath();

        String imageName = ctx.getImageName();

        String imageVer = ctx.getImageVer();

        List<String> runningContIds = getIdList(ip);

        for(String runningContId : runningContIds){
            String stopCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker stop %s",
                ip, runningContId);

            BenchmarkUtils.println(String.format("Stopping docker container %s on the host %s", runningContId, ip));

            runCmd(stopCmd);
        }

        String buildDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s %s/bin/build-docker.sh %s %s %s",
            ip, getMainDir(), path, imageName, imageVer);

        runCmd(buildDockerCmd);

        return new BuildDockerResult(imageName, imageVer, ip, cnt);
    }

    private List<String> getIdList(String ip){
        String getListCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", ip);

        String servContNamePref = String.format("SERVER-%s", ip);
        String drvrContNamePref = String.format("DRIVER-%s", ip);

        List<String> res = new ArrayList<>();

        List<String> responseList = runCmd(getListCmd);

        for(String response : responseList)
            if(response.contains(servContNamePref) || response.contains(drvrContNamePref))
                res.add(response.split(" ")[0]);

        return res;
    }
}
