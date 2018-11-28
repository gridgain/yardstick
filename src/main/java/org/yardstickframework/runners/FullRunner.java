package org.yardstickframework.runners;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.docker.DockerCleanContWorker;
import org.yardstickframework.runners.docker.DockerCleanImagesWorker;
import org.yardstickframework.runners.docker.DockerStartContWorker;
import org.yardstickframework.runners.docker.DockerWorker;
import org.yardstickframework.runners.docker.PrepareDockerResult;
import org.yardstickframework.runners.docker.DockerWorkContext;
import org.yardstickframework.runners.docker.DockerBuildImagesWorker;

public class FullRunner extends AbstractRunner {



    public FullRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
//        for(String a : args)
//            System.out.println(a);


        RunContext runCtx = RunContext.getRunContext(args);

        FullRunner runner = new FullRunner(runCtx);

//        DockerWorkContext dctx = new DockerWorkContext(runCtx.getServUniqList(), NodeType.SERVER);
//
//        DockerBuildImagesWorker pdw = new DockerBuildImagesWorker(runCtx, dctx);
//
//        pdw.workOnHosts();

        runner.run1();
    }

    public int run1() {
        Worker killWorker = new KillWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

        killWorker.workOnHosts();

        Worker deployWorker = new DeployWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

        deployWorker.workOnHosts();

        List<NodeType> forDockerPrep = new ArrayList<>();

        if (runCtx.getServRunMode() == RunMode.IN_DOCKER)
            forDockerPrep.add(NodeType.SERVER);

        if (runCtx.getDrvrRunMode() == RunMode.IN_DOCKER)
            forDockerPrep.add(NodeType.DRIVER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        dockerRunner.prepare(forDockerPrep);

        String cfgStr0 = runCtx.getProps().getProperty("CONFIGS").split(",")[0];

        List<WorkResult> servRes = null;

        List<WorkResult> drvrRes = null;

        if(!Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
            servRes = startServNodes(cfgStr0);

            BenchmarkUtils.println("RESTART_SERVERS=false");
        }

        for (String cfgStr : runCtx.getCfgList()) {
            if(Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
                servRes = startServNodes(cfgStr);

//                BenchmarkUtils.println("RESTART_SERVERS=true");
            }

            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            drvrRes = startDrvrNodes(cfgStr);

            BenchmarkUtils.println("Waiting for driver nodes to start.");

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            BenchmarkUtils.println("Driver nodes started.");

            BenchmarkUtils.println("Waiting for driver nodes to stop.");

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

            BenchmarkUtils.println("Driver nodes stopped.");

            if(Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS")))
                killNodes(servRes);
        }

        dockerRunner.collect(forDockerPrep);

        if(!forDockerPrep.isEmpty())
            dockerRunner.clean(Collections.singletonList(NodeType.DRIVER));

        new CollectorWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList())).workOnHosts();

//        createCharts();

        return 0;
    }

    /**
     *
     */
    public int run() {
        Worker killWorker = new KillWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

        killWorker.workOnHosts();

        if(runCtx.getServRunMode() == RunMode.IN_DOCKER){
            if(runCtx.getDockerContext().isRemoveContainersBeforeRun()){
                DockerWorker dockerWorker = new DockerCleanContWorker(runCtx, new DockerWorkContext(
                    runCtx.getServUniqList(),
                    NodeType.SERVER));

                dockerWorker.workOnHosts();
            }
        }

        Worker deployWorker = new DeployWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

        deployWorker.workOnHosts();

        List<WorkResult> buildServResList = null;

        if(runCtx.getServRunMode() == RunMode.IN_DOCKER) {
            Worker cleanUpWorker = new CleanUpWorker(runCtx, new CommonWorkContext(runCtx.getServUniqList()));

            cleanUpWorker.workOnHosts();

            buildServResList = buildDockerImages(NodeType.SERVER);
        }

        List<WorkResult> buildDrvrResList = null;

        if(runCtx.getDrvrRunMode() == RunMode.IN_DOCKER) {
            Worker cleanUpWorker = new CleanUpWorker(runCtx, new CommonWorkContext(runCtx.getServUniqList()));

            cleanUpWorker.workOnHosts();

            buildDrvrResList = buildDockerImages(NodeType.DRIVER);
        }

        String cfgStr0 = runCtx.getProps().getProperty("CONFIGS").split(",")[0];

        List<WorkResult> servRes = null;

        List<WorkResult> drvrRes = null;

        if(!Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
            servRes = startServNodes(cfgStr0);

            BenchmarkUtils.println("RESTART_SERVERS=false");
        }

        for (String cfgStr : runCtx.getCfgList()) {
            if(Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS"))) {
                servRes = startServNodes(cfgStr0);

//                BenchmarkUtils.println("RESTART_SERVERS=true");
            }

            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            drvrRes = startDrvrNodes(cfgStr);

            BenchmarkUtils.println("Waiting for driver nodes to start.");

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            BenchmarkUtils.println("Driver nodes started.");

            BenchmarkUtils.println("Waiting for driver nodes to stop.");

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

            BenchmarkUtils.println("Driver nodes stopped.");

            if(Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS")))
                killNodes(servRes);
        }

        if(!Boolean.valueOf(runCtx.getProps().getProperty("RESTART_SERVERS")))
            killNodes(servRes);

//        collectResults(servRes);
//
//        collectResults(drvrRes);

        if(runCtx.getServRunMode() == RunMode.IN_DOCKER || runCtx.getDrvrRunMode() == RunMode.IN_DOCKER) {

            Worker cleanUpWorker = new CleanUpWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

            cleanUpWorker.workOnHosts();
        }

        createCharts();

        return 0;
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

    private List<WorkResult> killNodes(List<WorkResult> nodeList){
        KillWorker killWorker = new KillWorker(runCtx, null);

        for(WorkResult nodeInfo : nodeList)
            killWorker.killNode((NodeInfo)nodeInfo);

        return null;
    }

    private void waitForNodes(List<WorkResult> nodeInfoList, NodeStatus expectedStatus){
        boolean unexpected = true;

        if(nodeInfoList.isEmpty()){
            BenchmarkUtils.println("List of nodes to wait is empty.");

            return;
        }

        NodeChecker checker = runCtx.getNodeChecker((NodeInfo)nodeInfoList.get(0));

        while(unexpected) {
            unexpected = false;

            for (WorkResult nodeInfo : nodeInfoList) {
                NodeCheckResult res = (NodeCheckResult)checker.checkNode((NodeInfo)nodeInfo);

                if (res.getNodeStatus() != expectedStatus) {
                    unexpected = true;

                    try {
                        Thread.sleep(1000L);
                    }
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
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
            runCtx.getServUniqList():
            runCtx.getDrvrUniqList();

        DockerWorkContext dockerWorkCtx = new DockerWorkContext(hostList, type);

        Worker buildDocWorker = new DockerBuildImagesWorker(runCtx, dockerWorkCtx);

        return buildDocWorker.workOnHosts();
    }

    private void collectResults(){
        File outDir = new File(String.format("%s/output", runCtx.getRemWorkDir()));



//        for(String res : resList){
//            NodeInfo nodeInfo = (NodeInfo)res;
//
//            String nodeOutDir = String.format("%s/output", runCtx.getRemWorkDir());
//
//            String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s",
//                nodeInfo.getHost(), nodeOutDir);
//
//            runCmd(mkdirCmd);
//
//            String collectCmd = String.format("scp -r -o StrictHostKeyChecking=no %s:%s/* %s",
//                nodeInfo.getHost(), nodeOutDir, outDir.getAbsolutePath());
//
//            BenchmarkUtils.println(String.format("Running cp from host cmd: %s", collectCmd));
//
//            runCmd(collectCmd);
//        }
    }

    private void createCharts(){
        String mainResDir = String.format("%s/output/result-%s", runCtx.getRemWorkDir(), runCtx.getMainDateTime());

        String createStdCmd = String.format("/bin/bash %s/bin/jfreechart-graph-gen.sh -gm STANDARD -i %s",
            runCtx.getRemWorkDir(), mainResDir);

        System.out.println(createStdCmd);

        runCmd(createStdCmd);


        String createCmd = String.format("/bin/bash %s/bin/jfreechart-graph-gen.sh -i %s",
            runCtx.getRemWorkDir(), mainResDir);

        runCmd(createCmd);

        File outDir = new File(mainResDir).getParentFile();

        if(outDir.exists() && outDir.isDirectory()) {
            File[] arr = outDir.listFiles();

            for(File resComp : arr){
                if(resComp.getName().startsWith("results-compound")){
                    String mvCmd = String.format("mv %s %s",
                        resComp.getAbsolutePath(), mainResDir);

                    runCmd(mvCmd);
                }
            }


        }

//        OUT_DIR=$(cd $results_folder/../; pwd)
//        echo "<"$(date +"%H:%M:%S")"><yardstick> Creating charts"
//                . ${SCRIPT_DIR}/jfreechart-graph-gen.sh -gm STANDARD -i $results_folder >> /dev/null
//                . ${SCRIPT_DIR}/jfreechart-graph-gen.sh -i $results_folder >> /dev/null
//        echo "<"$(date +"%H:%M:%S")"><yardstick> Moving chart directory to the ${MAIN_DIR}/output/results-${date_time} directory."
//        mv $MAIN_DIR/output/results-compound* $MAIN_DIR/output/results-$date_time
    }
}
