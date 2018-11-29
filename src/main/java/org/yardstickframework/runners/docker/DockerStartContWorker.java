package org.yardstickframework.runners.docker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerStartContWorker extends DockerWorker {
    private static final String DFLT_START_CMD = "sleep";

    private static final List<String> DFLT_START_CMD_ARGS = Collections.singletonList("365d");

    private static final List<String> DFLT_RUN_CMD_ARGS = Arrays.asList("-d", "--network", "host");

    public DockerStartContWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public WorkResult doWork(String host, int cnt) {
        NodeType type = dockerWorkCtx.getNodeType();

        String imageName = getImageNameToUse(type);

        String contName = String.format("YARDSTICK_%s_%d", type, cnt);

        BenchmarkUtils.println(String.format("Starting container %s on the host %s", contName, host));

        String startContCmd = getStartContCmd();

        String runCmdArgs = getRunArgs();

//        System.out.println(startContCmd);

        String startCmd = String.format("run %s --name %s %s %s",
            runCmdArgs, contName, imageName, startContCmd);

        CommandHandler hndl = new CommandHandler(runCtx);

//        System.out.println(startCmd);

        try {
            hndl.runDockerCmd(host, startCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if(dockerCtx.isCopyYardctickIntoContainer()){
            String contId = getContId(host, contName);

            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, runCtx.getRemWorkDir());

            String remPath = runCtx.getRemWorkDir();

            String parentPath = new File(remPath).getParentFile().getAbsolutePath();

            String cpCmd = String.format("cp %s %s:%s", remPath, contId, parentPath);

            try {
                hndl.runDockerCmd(host, mkdirCmd);

                hndl.runDockerCmd(host, cpCmd);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    private List<String> getIdList(String ip) {
        String getListCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker ps -a", ip);

        String servContNamePref = String.format("SERVER-%s", ip);
        String drvrContNamePref = String.format("DRIVER-%s", ip);

        List<String> res = new ArrayList<>();

        List<String> responseList = runCmd(getListCmd);

        for (String response : responseList)
            if (response.contains(servContNamePref) || response.contains(drvrContNamePref))
                res.add(response.split(" ")[0]);

        return res;
    }

    private String getStartContCmd(){
        String name = dockerCtx.getStartContCmd() != null ? dockerCtx.getStartContCmd() : DFLT_START_CMD;

        List<String> args = dockerCtx.getStartContCmdArgs() != null  ?
            dockerCtx.getStartContCmdArgs() :
            DFLT_START_CMD_ARGS;


        StringBuilder sb = new StringBuilder(name);

        for(String arg : args) {
            sb.append(" ");

            sb.append(arg);
        }

        return sb.toString();
    }

    private String getRunArgs(){
        List<String> args = dockerCtx.getDockerRunCmdArgs() != null  ?
            dockerCtx.getDockerRunCmdArgs() :
            DFLT_RUN_CMD_ARGS;

        StringBuilder sb = new StringBuilder();

        for(String arg : args) {
            sb.append(" ");

            sb.append(arg);
        }

        return sb.toString();
    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
