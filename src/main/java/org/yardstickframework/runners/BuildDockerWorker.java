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

        BenchmarkUtils.println(String.format("Building docker image %s:%s on the host %s", imageName, imageVer, ip));

        String buildDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s %s/bin/build-docker.sh %s %s %s",
            ip, getMainDir(), path, imageName, imageVer);

        runCmd(buildDockerCmd);

//        String stopCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker stop TO_CHECK_JAVA", ip);
//
//        runCmd(stopCmd);
//
//        String startToChek = String.format("ssh -o StrictHostKeyChecking=no %s docker run -d --name TO_CHECK_JAVA " +
//                "%s:%s sleep infinity",
//            ip,
//            imageName,
//            imageVer);
//
//
//        runCmd(startToChek);

        String dockerJavaHome;

        if (getDockerJava() != null)
            dockerJavaHome = getDockerJava();
        else {
            BenchmarkUtils.println("DOCKER_JAVA_HOME is not defined in property file.");
            BenchmarkUtils.println(String.format("Will use %s/bin/java to run nodes in docker.", getRemJava()));

            dockerJavaHome = getRemJava();
        }



//        String testCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker exec TO_CHECK_JAVA " +
//                "test -f %s/bin/java && echo java_found || echo not_found", ip, getRemJava());
//
//        List<String> res = runCmd(testCmd);
//
//        boolean foundJava = false;
//
//        for(String resp : res)
//            if(resp.endsWith("java_found"))
//                foundJava = true;
//
//        if(!foundJava) {
//            String getJavaHome = String.format("ssh -o StrictHostKeyChecking=no %s docker exec TO_CHECK_JAVA " +
//                "echo $JAVA_HOME", ip);
//
//            res = runCmd(getJavaHome);
//
//            if (!res.isEmpty() && !res.get(0).isEmpty()) {
//                if (!remJavaHome.equals(res.get(0))) {
//                    BenchmarkUtils.println(String.format("WARNING! Docker's JAVA_HOME %s in not equal defined JAVA_HOME" +
//                        " in property file %s.", res.get(0), remJavaHome));
//
//                    BenchmarkUtils.println(String.format("Will use for running nodes in docker JAVA_HOME=%s.", res.get(0)));
//
//                    remJavaHome = res.get(0);
//                }
//            }
//        }
//
//        runCmd(stopCmd);

        return new BuildDockerResult(imageName, imageVer, dockerJavaHome, ip, cnt);
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

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
