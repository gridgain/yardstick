package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.yardstickframework.BenchmarkUtils;

public class FullRunner extends AbstractRunner {

    protected RunMode servRunMode;
    protected RunMode drvrRunMode;

    public FullRunner(Properties runProps) {
        super(runProps);
    }

    public static void main(String[] args) {
//        for(String a : args)
//            System.out.println(a);

        if(args.length == 0)
            args = new String[]{"/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/benchmark.properties",
            "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/bin"};

        FullRunner runner = new FullRunner(null);

        String arg = args.length == 0 ? "/home/oostanin/gg/incubator-ignite/modules/yardstick/target/assembly/config/benchmark.properties" :
            args[0];

        try {
            runner.setRunProps(new File(arg));

//            System.out.println(String.format("setting arg = %s", arg));

            runner.setPropPath(arg);
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        if (runner.runProps.getProperty("WORK_DIR") == null) {
            runner.mainDir = new File(args[1]).getParent();

            runner.runProps.setProperty("WORK_DIR", new File(args[1]).getParent());
        }

        runner.runProps.setProperty("MAIN_DATE_TIME", BenchmarkUtils.dateTime());

        runner.run();
    }

    /**
     *
     */
    public int run() {
        Worker killWorker = new KillWorker(runProps, new CommonWorkContext(getFullUniqList()));

        killWorker.workOnHosts();

        Worker deployWorker = new DeployWorker(runProps, new CommonWorkContext(getFullUniqList()));

        deployWorker.workOnHosts();

        List<WorkResult> buildServResList = null;

        servRunMode = runProps.getProperty("RUN_SERVER_MODE") != null ?
            RunMode.valueOf(runProps.getProperty("RUN_SERVER_MODE")):
            RunMode.PLAIN;

        drvrRunMode = runProps.getProperty("RUN_DRIVER_MODE") != null ?
            RunMode.valueOf(runProps.getProperty("RUN_DRIVER_MODE")):
            RunMode.PLAIN;

        if(servRunMode == RunMode.IN_DOCKER) {
            Worker cleanUpWorker = new CleanUpWorker(runProps, new CommonWorkContext(getServUniqList()));

            cleanUpWorker.workOnHosts();

            buildServResList = buildDockerImages(NodeType.SERVER);
        }

        List<WorkResult> buildDrvrResList = null;

        if(drvrRunMode == RunMode.IN_DOCKER) {
            Worker cleanUpWorker = new CleanUpWorker(runProps, new CommonWorkContext(getDrvrUniqList()));

            cleanUpWorker.workOnHosts();

            buildDrvrResList = buildDockerImages(NodeType.DRIVER);
        }

        String cfgStr0 = runProps.getProperty("CONFIGS").split(",")[0];

        List<WorkResult> servRes = null;

        List<WorkResult> drvrRes = null;

        if(!Boolean.valueOf(runProps.getProperty("RESTART_SERVERS"))) {
            servRes = startServNodes(cfgStr0, buildServResList);

            BenchmarkUtils.println("RESTART_SERVERS=false");
        }

        for (String cfgStr : runProps.getProperty("CONFIGS").split(",")) {
            if(cfgStr.length() < 10)
                continue;

            if(Boolean.valueOf(runProps.getProperty("RESTART_SERVERS"))) {
                servRes = startServNodes(cfgStr0, buildServResList);

//                BenchmarkUtils.println("RESTART_SERVERS=true");
            }

            try {
                Thread.sleep(5000L);
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }

            drvrRes = startDrvrNodes(cfgStr, buildDrvrResList);

            BenchmarkUtils.println("Waiting for driver nodes to start.");

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            BenchmarkUtils.println("Driver nodes started.");

            BenchmarkUtils.println("Waiting for driver nodes to stop.");

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

            BenchmarkUtils.println("Driver nodes stopped.");

            if(Boolean.valueOf(runProps.getProperty("RESTART_SERVERS")))
                killNodes(servRes);
        }

        if(!Boolean.valueOf(runProps.getProperty("RESTART_SERVERS")))
            killNodes(servRes);

        collectResults(servRes);

        collectResults(drvrRes);

        if(servRunMode == RunMode.IN_DOCKER || drvrRunMode == RunMode.IN_DOCKER) {

            Worker cleanUpWorker = new CleanUpWorker(runProps, new CommonWorkContext(getFullUniqList()));

            cleanUpWorker.workOnHosts();
        }

        createCharts();

        return 0;
    }

    private List<WorkResult> startServNodes(String cfgStr, List<WorkResult> buildDocList) {
        String parsedCfgStr = parseCfgStr(cfgStr);

        StartNodeWorkContext nodeWorkCtx = new StartNodeWorkContext(getServList(), servRunMode, parsedCfgStr,
            getPropPath());

        if (buildDocList != null && !buildDocList.isEmpty())
            nodeWorkCtx.setDockerInfo((BuildDockerResult)buildDocList.get(0));

        StartNodeWorker startServWorker = new StartServWorker(runProps, nodeWorkCtx);

        List<WorkResult> startServResList = startServWorker.workOnHosts();

        return startServResList;
    }

    private List<WorkResult> startDrvrNodes(String cfgStr, List<WorkResult> buildDocList) {
        String parsedCfgStr = parseCfgStr(cfgStr);

        StartNodeWorkContext nodeWorkCtx = new StartNodeWorkContext(getDrvrList(), drvrRunMode, parsedCfgStr,
            getPropPath());

        if (buildDocList != null && !buildDocList.isEmpty())
            nodeWorkCtx.setDockerInfo((BuildDockerResult)buildDocList.get(0));

        StartNodeWorker startDrvrWorker = new StartDrvrWorker(runProps, nodeWorkCtx);

        startDrvrWorker.setPropPath(getPropPath());

        return startDrvrWorker.workOnHosts();
    }

    private List<WorkResult> killNodes(List<WorkResult> nodeList){
        KillWorker killWorker = new KillWorker(runProps, null);

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

        NodeChecker checker = getNodeChecker((NodeInfo)nodeInfoList.get(0));

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

        String imageName = runProps.getProperty(imageNameProp);

        String nameProp = String.format("%s_DOCKERFILE_NAME", type);
        String pathProp = String.format("%s_DOCKERFILE_PATH", type);

        if (runProps.getProperty(nameProp) == null &&
            runProps.getProperty(pathProp) == null)
            throw new IllegalArgumentException("Dockerfile name and path is not defined in property file.");

        String dockerfilePath = runProps.getProperty(pathProp) != null ?
            runProps.getProperty(pathProp) :
            String.format("%s/config/%s", getMainDir(), runProps.getProperty(nameProp));

        String imageVer = getMainDateTime();

        List<String> hostList = type == NodeType.SERVER ?
            getServUniqList():
            getDrvrUniqList();

        BuildDockerWorkContext docCtx = new BuildDockerWorkContext(hostList, dockerfilePath, imageName, imageVer);

        Worker buildDocWorker = new BuildDockerWorker(runProps, docCtx);

        return buildDocWorker.workOnHosts();
    }

    private void collectResults(List<WorkResult> resList){
        File outDir = new File(String.format("%s/output", getMainDir()));

        if(!outDir.exists())
            outDir.mkdirs();

        for(WorkResult res : resList){
            NodeInfo nodeInfo = (NodeInfo)res;

            String nodeOutDir = String.format("%s/output", getMainDir());

            String mkdirCmd = String.format("ssh -o StrictHostKeyChecking=no %s mkdir -p %s",
                nodeInfo.getHost(), nodeOutDir);

            runCmd(mkdirCmd);

            if(nodeInfo.getDockerInfo() != null){
                String contId = nodeInfo.getDockerInfo().getContId();

                String cpFromDockerCmd = String.format("ssh -o StrictHostKeyChecking=no %s docker cp %s:%s/output %s",
                    nodeInfo.getHost(), contId, getMainDir(), getMainDir());

                BenchmarkUtils.println(String.format("Running cp from docker cmd: %s", cpFromDockerCmd));

                runCmd(cpFromDockerCmd);
            }

            String collectCmd = String.format("scp -r -o StrictHostKeyChecking=no %s:%s/* %s",
                nodeInfo.getHost(), nodeOutDir, outDir.getAbsolutePath());

            BenchmarkUtils.println(String.format("Running cp from host cmd: %s", collectCmd));

            runCmd(collectCmd);
        }
    }

    private void createCharts(){
        String mainResDir = String.format("%s/output/result-%s", getMainDir(), getMainDateTime());

        String createStdCmd = String.format("/bin/bash %s/bin/jfreechart-graph-gen.sh -gm STANDARD -i %s",
            getMainDir(), mainResDir);

        runCmd(createStdCmd);


        String createCmd = String.format("/bin/bash %s/bin/jfreechart-graph-gen.sh -i %s",
            getMainDir(), mainResDir);

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
