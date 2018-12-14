package org.yardstickframework.runners.workers.node;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.yardstickframework.runners.CommandExecutionResult;
import org.yardstickframework.runners.CommandHandler;
import org.yardstickframework.runners.context.DockerInfo;
import org.yardstickframework.runners.DockerRunner;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.DockerContext;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;

public class DockerStartContWorker extends DockerNodeWorker {
    private static final String DFLT_START_CMD = "sleep 365d";

    private static final String DFLT_RUN_CMD_ARGS = "-d --network host";

    private DockerContext dockerCtx;

    public DockerStartContWorker(RunContext runCtx, List<NodeInfo> nodeList) {
        super(runCtx, nodeList);

        dockerCtx = runCtx.dockerContext();
    }

    @Override public NodeInfo doWork(NodeInfo nodeInfo) throws InterruptedException {
        String host = nodeInfo.host();

        String id  = nodeInfo.id();

        NodeType type = nodeInfo.nodeType();

        String imageName = dockerCtx.getImageName(type);

        String contName = String.format("YARDSTICK_%s_%s", type, id);

        DockerInfo docInfo = new DockerInfo(imageName, contName);

        nodeInfo.dockerInfo(docInfo);

        log().info(String.format("Starting the container '%s' on the host '%s'", contName, host));

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

//            String contId = getContId(host, contName);

            String mkdirCmd = String.format("exec %s mkdir -p %s", contName, runCtx.remoteWorkDirectory());

            synchronized (this) {
                setDockerJavaHome(nodeInfo);
            }

            hndl.runDockerCmd(host, mkdirCmd);

            String remPath = runCtx.remoteWorkDirectory();

            String parentPath = new File(remPath).getParentFile().getAbsolutePath();

            String cpCmd = String.format("cp %s %s:%s", remPath, contName, parentPath);

            hndl.runDockerCmd(host, cpCmd);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        return nodeInfo;
    }

    private String getStartContCmd() {
        return dockerCtx.getStartContCmd() != null ? dockerCtx.getStartContCmd() : DFLT_START_CMD;
    }

    private String getRunArgs() {
        return dockerCtx.getDockerRunCmd() != null ?
            dockerCtx.getDockerRunCmd() :
            DFLT_RUN_CMD_ARGS;
    }

    private void setDockerJavaHome(NodeInfo nodeInfo) throws IOException, InterruptedException {
        NodeType type = nodeInfo.nodeType();

        String contName = nodeInfo.dockerInfo().contName();

        String echoCmd = String.format("exec %s sh -c 'echo $JAVA_HOME'", contName);

        CommandHandler hndl = new CommandHandler(runCtx);

        String host = nodeInfo.host();

        CommandExecutionResult res = hndl.runDockerCmd(host, echoCmd);

        if(!res.getOutStream().isEmpty()){
            String javaHome = res.getOutStream().get(0);

            switch (type){
                case SERVER:
                    if(dockerCtx.getServerDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for server nodes: '%s'",
                            javaHome));

                        dockerCtx.setServerDockerJavaHome(javaHome);
                    }
                    break;
                case DRIVER:
                    if(dockerCtx.getDriverDockerJavaHome() == null) {
                        log().info(String.format("Using docker JAVA_HOME for driver nodes: '%s'",
                            javaHome));

                        dockerCtx.setDriverDockerJavaHome(javaHome);
                    }
                    break;
                default:
                    log().info("Unknown node type " + type);
            }
        }
        else{
            log().info(String.format("Failed to get JAVA_HOME variable from docker container '%s'", contName));

            log().info("Will clean up docker and exit.");

            DockerRunner runner = new DockerRunner(runCtx);

            runner.cleanUp(runCtx.nodeTypes(RunMode.DOCKER), "after");

            System.exit(1);
        }

    }

    @Override public String getWorkerName() {
        return getClass().getSimpleName();
    }
}
