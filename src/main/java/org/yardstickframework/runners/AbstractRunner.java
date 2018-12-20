package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.List;
import java.util.concurrent.CountDownLatch;
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

/**
 * Parent for runners.
 */
public class AbstractRunner {
    /** Run context. */
    protected RunContext runCtx;

    /** */
    private DockerRunner dockerRunner;

    /** */
    private List<NodeType> dockerList;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public AbstractRunner(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     *
     * @param checkWorker Check worker.
     */
    void checkPlain(HostWorker checkWorker) {
        List<WorkResult> checks = checkWorker.workOnHosts();

        for (WorkResult check : checks) {
            CheckWorkResult res = (CheckWorkResult)check;

            if (res.exit())
                System.exit(1);
        }
    }

    /**
     *
     */
    void generalPrepare(){
        Set<String> fullSet = runCtx.getHostSet();

        checkPlain(new CheckConnWorker(runCtx, fullSet));

        checkPlain(new CheckJavaWorker(runCtx, runCtx.uniqueHostsByMode(RunMode.PLAIN)));

        dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "before");
        }

        new KillWorker(runCtx, fullSet).workOnHosts();

        new DeployWorker(runCtx, fullSet).workOnHosts();

        if (!dockerList.isEmpty()) {
            dockerRunner.prepare(dockerList);

            dockerRunner.start(dockerList);
        }
    }

    /**
     *
     */
    void generalCleanUp(){
        if (!dockerList.isEmpty()) {
            dockerRunner.collect(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }
    }

    /**
     *
     * @return Logger.
     */
    protected Logger log(){
        return LogManager.getLogger(getClass().getSimpleName());
    }

    /**
     *
     */
    void createCharts() {
        String mainResDir = String.format("%s/output/result-%s", runCtx.localeWorkDirectory(), runCtx.mainDateTime());

        String cp = String.format("%s/libs/*", runCtx.localeWorkDirectory());

        String mainClass = "org.yardstickframework.report.jfreechart.JFreeChartGraphPlotter";

        String jvmOpts = "-Xmx1g";

        String stdCharts = String.format("%s -cp %s %s -gm STANDARD -i %s", jvmOpts, cp, mainClass, mainResDir);



        runCtx.handler().runLocalJava(stdCharts);

        String charts = String.format("%s -cp %s %s -i %s", jvmOpts, cp, mainClass, mainResDir);

        runCtx.handler().runLocalJava(charts);

        try {
            new CountDownLatch(1).await(3000L, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        File outDir = new File(mainResDir).getParentFile();

        if (outDir.exists() && outDir.isDirectory()) {
            File[] arr = outDir.listFiles();

            for (File resComp : arr) {
                if (resComp.getName().startsWith("results-compound")) {
                    try {
                        String mvCmd = String.format("mv %s %s",
                            resComp.getAbsolutePath(), mainResDir);

                        runCtx.handler().runCmd("localhost", mvCmd);
                    }
                    catch (IOException | InterruptedException e) {
                        log().error("Failed to create charts", e);
                    }
                }
            }
        }
    }

    /**
     *
     * @param type Node type.
     * @param cfgStr Config string.
     * @return List of {@code NodeInfo} objects.
     */
    List<NodeInfo> startNodes(NodeType type, String cfgStr) {
        NodeWorker startServWorker = new StartNodeWorker(runCtx, runCtx.getNodes(type), cfgStr);

        return startServWorker.workForNodes();
    }

    /**
     *
     * @param list List of {@code NodeInfo} objects.
     */
    void checkLogs(List<NodeInfo> list){
        NodeWorker checkWorker = new CheckLogWorker(runCtx,list);

        List<NodeInfo> resList = checkWorker.workForNodes();

        for (NodeInfo nodeInfo : resList){
            if(nodeInfo.nodeStatus() == NodeStatus.NOT_RUNNING)
                System.exit(1);
        }
    }

    /**
     *
     * @param nodeList List of {@code NodeInfo} objects.
     * @param expStatus Expected status.
     */
    void waitForNodes(List<NodeInfo> nodeList, NodeStatus expStatus) {
        NodeWorker waitWorker = new WaitNodeWorker(runCtx, nodeList, expStatus);

        waitWorker.workForNodes();
    }
}
