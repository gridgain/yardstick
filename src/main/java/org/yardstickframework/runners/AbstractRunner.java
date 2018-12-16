package org.yardstickframework.runners;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CheckJavaWorker;
import org.yardstickframework.runners.workers.host.DeployWorker;
import org.yardstickframework.runners.workers.host.HostWorker;
import org.yardstickframework.runners.workers.host.KillWorker;
import org.yardstickframework.runners.workers.node.CheckLogWorker;
import org.yardstickframework.runners.workers.node.NodeWorker;
import org.yardstickframework.runners.workers.node.StartNodeWorker;
import org.yardstickframework.runners.workers.node.WaitNodeWorker;

public class AbstractRunner {
    protected RunContext runCtx;

    protected DockerRunner dockerRunner;

    protected List<NodeType> dockerList;

    /**
     *
     * @param runCtx Run context.
     */
    public AbstractRunner(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    protected List<String> runCmd(final String cmd){

        List<String> res = new ArrayList<>();

        final Process p;

        ExecutorService errStreamPrinter = Executors.newSingleThreadExecutor();


        try {
            p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));


            errStreamPrinter.submit(new Callable<Object>() {
                @Override public Object call() throws IOException {
                    String line = "";


                    BufferedReader errReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

                    while ((line = errReader.readLine())!= null)
                        System.out.println(String.format("Command '%s' returned error line: %s:", cmd, line));

                    return null;
                }
            });

            String line = "";

            while ((line = reader.readLine())!= null) {
                res.add(line);

                if(line.contains("Successfully built "))
                     log().info(line);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        errStreamPrinter.shutdown();

        return res;
    }

    protected void checkPlain(HostWorker checkWorker) {
        List<WorkResult> checks = checkWorker.workOnHosts();

        for (WorkResult check : checks) {
            CheckWorkResult res = (CheckWorkResult)check;

            if (res.exit())
                System.exit(1);
        }
    }

    protected void generalPrapare(){
        List<String> fullList = runCtx.getFullUniqueList();

        checkPlain(new CheckConnWorker(runCtx, fullList));

        checkPlain(new CheckJavaWorker(runCtx, runCtx.uniqueHostsByMode(RunMode.PLAIN)));

        dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "before");
        }

        new KillWorker(runCtx, fullList).workOnHosts();

        new DeployWorker(runCtx, fullList).workOnHosts();

        if (!dockerList.isEmpty()) {
            dockerRunner.prepare(dockerList);

            dockerRunner.start(dockerList);
        }
    }

    protected void generalCleanUp(){
        if (!dockerList.isEmpty()) {
            dockerRunner.collect(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }
    }

    protected List<String> getHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.serverList() : runCtx.driverList();
    }

    protected List<String> getUniqHosts(NodeType type){
        return type == NodeType.SERVER ? runCtx.serverUniqueList() : runCtx.driverUniqueList();
    }

    protected Logger log(){
        Logger log = LogManager.getLogger(getClass().getSimpleName());

        return log;
    }

    protected void createCharts() {
        String mainResDir = String.format("%s/output/result-%s", runCtx.localeWorkDirectory(), runCtx.mainDateTime());

        String cp = String.format("%s/libs/*", runCtx.localeWorkDirectory());

        String mainClass = "org.yardstickframework.report.jfreechart.JFreeChartGraphPlotter";

        String jvmOpts = "-Xmx1g";

        String stdCharts = String.format("%s -cp %s %s -gm STANDARD -i %s", jvmOpts, cp, mainClass, mainResDir);

        CommandHandler hand = new CommandHandler(runCtx);

        hand.runLocalJava(stdCharts);

        String charts = String.format("%s -cp %s %s -i %s", jvmOpts, cp, mainClass, mainResDir);

        hand.runLocalJava(charts);

        File outDir = new File(mainResDir).getParentFile();

        try {
            new CountDownLatch(1).await(3000L, TimeUnit.MILLISECONDS);
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

    protected List<NodeInfo> startNodes(NodeType type, String cfgStr) {
        NodeWorker startServWorker = new StartNodeWorker(runCtx, runCtx.getNodes(type), cfgStr);

        return startServWorker.workForNodes();
    }

    protected void checkLogs(List<NodeInfo> list){
        NodeWorker checkWorker = new CheckLogWorker(runCtx,list);

        List<NodeInfo> resList = checkWorker.workForNodes();

        for (NodeInfo nodeInfo : resList){
            if(nodeInfo.nodeStatus() == NodeStatus.NOT_RUNNING)
                System.exit(1);
        }
    }

    protected void waitForNodes(List<NodeInfo> nodeList, NodeStatus expStatus) {
        NodeWorker waitWorker = new WaitNodeWorker(runCtx, nodeList, expStatus);

        waitWorker.workForNodes();
    }
}
