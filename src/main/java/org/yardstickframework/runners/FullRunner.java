package org.yardstickframework.runners;

import java.io.File;
import java.util.List;
import org.yardstickframework.runners.docker.DockerWorkContext;
import org.yardstickframework.runners.docker.DockerBuildImagesWorker;

public class FullRunner extends AbstractRunner {

    public FullRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        FullRunner runner = new FullRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        checkPlain(new CheckConnWorker(runCtx, runCtx.getFullUniqList()));

        List<NodeType> plainList = runCtx.getNodeTypes(RunMode.PLAIN);

        for (NodeType type : plainList)
            checkPlain(new CheckJavaWorker(runCtx, new CommonWorkContext(runCtx.getUniqHostsByType(type))));

        List<NodeType> dockerList = runCtx.getNodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.check(dockerList);


        Worker killWorker = new KillWorker(runCtx, runCtx.getFullUniqList());

        killWorker.workOnHosts();

        Worker deployWorker = new DeployWorker(runCtx, runCtx.getFullUniqList());

        deployWorker.workOnHosts();

        if (!dockerList.isEmpty()) {
            dockerRunner.cleanUp(dockerList, "before");

            dockerRunner.prepare(dockerList);

            dockerRunner.start(dockerList);
        }

        String cfgStr0 = runCtx.getProps().getProperty("CONFIGS").split(",")[0];

        List<WorkResult> servRes = null;

        List<WorkResult> drvrRes = null;

        if (!Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
            servRes = startServNodes(cfgStr0);

            log().info("RESTART_SERVERS=false");
        }

        for (String cfgStr : runCtx.getCfgList()) {
            if (Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS")))
                servRes = startServNodes(cfgStr);

            checkLogs(servRes);

            waitForNodes(servRes, NodeStatus.RUNNING);

            drvrRes = startDrvrNodes(cfgStr);

            checkLogs(drvrRes);

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            performRestart(servRes);

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

            if (Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
                stopNodes(servRes);

                waitForNodes(servRes, NodeStatus.NOT_RUNNING);
            }
        }

        if (!dockerList.isEmpty()) {
            dockerRunner.collect(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }

        new CollectWorker(runCtx, runCtx.getFullUniqList()).workOnHosts();

        createCharts();

        return 0;
    }

    private void performRestart(List<WorkResult> nodeInfos){
        RestartNodeWorkContext restCtx = new RestartNodeWorkContext(nodeInfos);

        RestartNodeWorker restWorker = new RestartNodeWorker(runCtx, restCtx);

        restWorker.workForNodes();

    }



    private void checkLogs(List<WorkResult> list){
        NodeWorker checkWorker = new CheckLogWorker(runCtx, new CommonWorkContext(list));

        List<WorkResult> resList = checkWorker.workForNodes();

        for (WorkResult res : resList){
            CheckWorkResult checkRes = (CheckWorkResult) res;

            if(checkRes.exit())
                System.exit(1);
        }
    }

    private List<WorkResult> startServNodes(String cfgStr) {
        StartNodeWorkContext nodeWorkCtx = new StartNodeWorkContext(runCtx.getServList(), runCtx.getServRunMode(),
            NodeType.SERVER, cfgStr);

        StartNodeWorker startServWorker = new StartNodeWorker(runCtx, nodeWorkCtx);

        List<WorkResult> startServResList = startServWorker.workOnHosts();

        return startServResList;
    }

    private List<WorkResult> startDrvrNodes(String cfgStr) {
        StartNodeWorkContext nodeWorkCtx = new StartNodeWorkContext(runCtx.getDrvrList(), runCtx.getDrvrRunMode(),
            NodeType.DRIVER, cfgStr);

        StartNodeWorker startDrvrWorker = new StartNodeWorker(runCtx, nodeWorkCtx);

        return startDrvrWorker.workOnHosts();
    }

    private List<WorkResult> stopNodes(List<WorkResult> nodeList) {
        NodeWorker stopWorker = new StopNodeWorker(runCtx, new CommonWorkContext(nodeList));

        stopWorker.workForNodes();

        return null;
    }

    private void waitForNodes(List<WorkResult> nodeInfoList, NodeStatus expStatus) {
        WorkContext waitCtx = new WaitNodeWorkContext(nodeInfoList, expStatus);

        NodeWorker waitWorker = new WaitNodeWorker(runCtx, waitCtx);

        waitWorker.workForNodes();
    }

    private List<WorkResult> buildDockerImages(NodeType type) {
        String imageNameProp = String.format("%s_DOCKER_IMAGE_NAME", type);

        String imageName = runCtx.getProps().getProperty(imageNameProp);

        String nameProp = String.format("%s_DOCKERFILE_NAME", type);
        String pathProp = String.format("%s_DOCKERFILE_PATH", type);

        if (runCtx.getProps().getProperty(nameProp) == null &&
            runCtx.getProps().getProperty(pathProp) == null)
            throw new IllegalArgumentException("Dockerfile name and path is not defined in property file.");

        String dockerfilePath = runCtx.getProps().getProperty(pathProp) != null ?
            runCtx.getProps().getProperty(pathProp) :
            String.format("%s/config/%s", runCtx.getRemWorkDir(), runCtx.getProps().getProperty(nameProp));

        String imageVer = runCtx.getMainDateTime();

        List<String> hostList = type == NodeType.SERVER ?
            runCtx.getServUniqList() :
            runCtx.getDrvrUniqList();

        DockerWorkContext dockerWorkCtx = new DockerWorkContext(hostList, type);

        Worker buildDocWorker = new DockerBuildImagesWorker(runCtx, dockerWorkCtx);

        return buildDocWorker.workOnHosts();
    }

    private void createCharts() {
        String mainResDir = String.format("%s/output/result-%s", runCtx.getLocWorkDir(), runCtx.getMainDateTime());

        String cp = String.format("%s/libs/*", runCtx.getLocWorkDir());

        String mainClass = "org.yardstickframework.report.jfreechart.JFreeChartGraphPlotter";

        String jvmOpts = "-Xmx1g";

        String stdCharts = String.format("%s -cp %s %s -gm STANDARD -i %s", jvmOpts, cp, mainClass, mainResDir);

        CommandHandler hndl = new CommandHandler(runCtx);

        hndl.runLocalJava(stdCharts);

        String charts = String.format("%s -cp %s %s -i %s", jvmOpts, cp, mainClass, mainResDir);

        hndl.runLocalJava(charts);

        File outDir = new File(mainResDir).getParentFile();

        try {
            Thread.sleep(3000L);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (outDir.exists() && outDir.isDirectory()) {
            File[] arr = outDir.listFiles();

            for (File resComp : arr) {
                if (resComp.getName().startsWith("results-compound")) {
                    String mvCmd = String.format("mv %s %s",
                        resComp.getAbsolutePath(), mainResDir);

                    runCmd(mvCmd);
                }
            }
        }
    }
}
