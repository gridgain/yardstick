package org.yardstickframework.runners.docker;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.DockerRunner;
import org.yardstickframework.runners.NodeType;
import org.yardstickframework.runners.RunContext;
import org.yardstickframework.runners.RunMode;
import org.yardstickframework.runners.WorkContext;
import org.yardstickframework.runners.WorkResult;

public class DockerStartContWorker extends DockerWorker {
    private static final String DFLT_START_CMD = "sleep 365d";

    private static final String DFLT_RUN_CMD_ARGS = "-d --network host";

    public DockerStartContWorker(RunContext runCtx, WorkContext workCtx) {
        super(runCtx, workCtx);
    }

    @Override public void beforeWork() {
        //NO_OP
    }

    @Override public void afterWork() {
        //NO_OP
    }

    @Override public WorkResult doWork(String host, int cnt) {
        NodeType type = dockerWorkCtx.getNodeType();

        String imageName = getImageNameToUse(type);

        String contName = String.format("YARDSTICK_%s_%d", type, cnt);

        log().info(String.format("Starting the container %s on the host %s", contName, host));

        String startContCmd = getStartContCmd();

        String runCmdArgs = getRunArgs();

//        System.out.println(startContCmd);

        if(runCmdArgs.contains("NAME_PLACEHOLDER"))
            runCmdArgs = runCmdArgs.replace("NAME_PLACEHOLDER", contName);

        String startCmd = String.format("run %s %s %s",
            runCmdArgs, imageName, startContCmd);

        CommandHandler hndl = new CommandHandler(runCtx);

//        System.out.println(startCmd);



        try {
            hndl.runDockerCmd(host, startCmd);

            String contId = getContId(host, contName);

            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, runCtx.getRemWorkDir());

            synchronized (this) {
                setDockerJavaHome(host, contName);
            }

            hndl.runDockerCmd(host, mkdirCmd);

            String remPath = runCtx.getRemWorkDir();

            String parentPath = new File(remPath).getParentFile().getAbsolutePath();

            String cpCmd = String.format("cp %s %s:%s", remPath, contId, parentPath);

            hndl.runDockerCmd(host, cpCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
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

    private String getStartContCmd() {
        return dockerCtx.getStartContCmd() != null ? dockerCtx.getStartContCmd() : DFLT_START_CMD;
    }

    private String getRunArgs() {
        return dockerCtx.getDockerRunCmdArgs() != null ?
            dockerCtx.getDockerRunCmdArgs() :
            DFLT_RUN_CMD_ARGS;
    }

    private void setDockerJavaHome(String host, String contName) throws IOException, InterruptedException {
        NodeType type = dockerWorkCtx.getNodeType();

        String echoCmd = String.format("exec %s sh -c 'echo $JAVA_HOME'", contName);

        CommandHandler hndl = new CommandHandler(runCtx);

        CommandExecutionResult res = hndl.runDockerCmd(host, echoCmd);

        if(!res.getOutStream().isEmpty()){
            String javaHome = res.getOutStream().get(0);

            switch (type){
                case SERVER:
                    if(dockerCtx.getServerDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for server nodes: %s",
                            javaHome));

                        dockerCtx.setServerDockerJavaHome(javaHome);
                    }
                    break;
                case DRIVER:
                    if(dockerCtx.getDriverDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for driver nodes: %s",
                            javaHome));

                        dockerCtx.setDriverDockerJavaHome(javaHome);
                    }
                    break;
                default:
                    log().info("Unknown node type " + type);
            }
        }
        else{
            log().info(String.format("Failed to get JAVA_HOME variable from docker container %s", contName));

            log().info("Will clean up docker and exit.");

            DockerRunner runner = new DockerRunner(runCtx);

            runner.cleanUp(runCtx.getNodeTypes(RunMode.DOCKER), "after");

            System.exit(1);
        }

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
