/*
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

package org.yardstickframework.runners;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CheckJavaWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;
import org.yardstickframework.runners.workers.host.DeployWorker;
import org.yardstickframework.runners.workers.host.HostWorker;
import org.yardstickframework.runners.workers.host.KillWorker;
import org.yardstickframework.runners.workers.node.CheckLogWorker;
import org.yardstickframework.runners.workers.node.NodeWorker;
import org.yardstickframework.runners.workers.node.RestartNodeWorker;
import org.yardstickframework.runners.workers.node.StartNodeWorker;
import org.yardstickframework.runners.workers.node.StopNodeWorker;
import org.yardstickframework.runners.workers.node.WaitNodeWorker;

/**
 * Parent for runners.
 */
public abstract class Runner {
    /** Run context. */
    protected RunContext runCtx;

    /**
     * Constructor.
     *
     * @param runCtx Run context.
     */
    public Runner(RunContext runCtx) {
        this.runCtx = runCtx;
    }

    /**
     * @return Exit code.
     */
    protected int run0() {
        if (runCtx.config().help()) {
            printHelp();

            System.exit(0);
        }

        return runCtx.exitCode();
    }

    /**
     *
     */
    protected abstract void printHelp();

    /**
     *
     */
    protected void commonHelp() {
        System.out.println();
        System.out.println("Options:");
        System.out.println();
        System.out.println("-pf  || --propertyFile      Property file.");
        System.out.println("-rwd || --remoteWorkDir     Remote work directory path.");
        System.out.println("-s   || --serverHosts       Comma separated list of server nodes addresses.");
        System.out.println("-d   || --driverHosts       Comma separated list of driver nodes addresses.");
    }

    /**
     *
     * @param  runCtx Run context.
     * @return Runner.
     */
    protected static Runner runner(RunContext runCtx) {
        return runCtx.dockerEnabled() ? new DockerRunner(runCtx) : new PlainRunner(runCtx);
    }

    /**
     *
     * @param  runCtx Run context.
     * @return Runner.
     */
    static Runner driverRunner(RunContext runCtx) {
        return runCtx.dockerEnabled() ? new DockerDriverRunner(runCtx) : new PlainDriverRunner(runCtx);
    }

    /**
     *
     * @param  runCtx Run context.
     * @return Runner.
     */
    static Runner serverRunner(RunContext runCtx) {
        return runCtx.dockerEnabled() ? new DockerServerRunner(runCtx) : new PlainServerRunner(runCtx);
    }

    /**
     * @param checkWorker Check worker.
     */
    void checkPlain(HostWorker checkWorker) {
        List<WorkResult> checks = checkWorker.workOnHosts();

        for (WorkResult check : checks) {
            CheckWorkResult res = (CheckWorkResult)check;

            if (res.exit())
                System.exit(runCtx.exitCode());
        }
    }

    /**
     *
     */
    void generalPrepare() {
        Set<String> fullSet = runCtx.getHostSet();

        check(fullSet);

        new KillWorker(runCtx, fullSet).workOnHosts();

        new DeployWorker(runCtx, fullSet).workOnHosts();
    }

    /**
     *
     */
    void driverPrepare() {
        Set<String> driverSet = runCtx.driverSet();

        check(driverSet);

        new DeployWorker(runCtx, driverSet).workOnHosts();
    }

    /**
     * @param hostSet Host set.
     */
    private void check(Set<String> hostSet) {
        checkPlain(new CheckConnWorker(runCtx, hostSet));

        checkPlain(new CheckJavaWorker(runCtx, runCtx.uniqueHostsByMode(RunMode.PLAIN)));
    }

    /**
     * @return Exit value.
     */
    protected int execute() {
        String cfgStr0 = runCtx.configList().get(0);

        List<NodeInfo> servRes = null;

        if (runCtx.startServersOnce())
            servRes = startNodes(NodeType.SERVER, cfgStr0);

        for (String cfgStr : runCtx.configList()) {
            if (!runCtx.startServersOnce())
                servRes = startNodes(NodeType.SERVER, cfgStr);

            checkLogs(servRes);

            waitForNodes(servRes, NodeStatus.RUNNING);

            iterationBody(cfgStr, servRes);

            if (!runCtx.startServersOnce()) {
                stopNodes(servRes);

                waitForNodes(servRes, NodeStatus.NOT_RUNNING);
            }
        }

        if (runCtx.startServersOnce()) {
            stopNodes(servRes);

            waitForNodes(servRes, NodeStatus.NOT_RUNNING);
        }

        return runCtx.exitCode();
    }

    /**
     *
     */
    protected void startServers(){
        String cfgStr0 = runCtx.configList().get(0);

        List<NodeInfo> servRes = startNodes(NodeType.SERVER, cfgStr0);

        checkLogs(servRes);

        waitForNodes(servRes, NodeStatus.RUNNING);
    }

    /**
     * @param cfgStr Config string.
     * @param servRes List of started server nodes.
     */
    void iterationBody(String cfgStr, List<NodeInfo> servRes) {
        List<NodeInfo> drvrRes = startNodes(NodeType.DRIVER, cfgStr);

        checkLogs(drvrRes);

        waitForNodes(drvrRes, NodeStatus.RUNNING);

        ExecutorService restServ = Executors.newSingleThreadExecutor();

        final List<NodeInfo> forRestart = new ArrayList<>(servRes);

        Future<List<NodeInfo>> restFut = restServ.submit(new Callable<List<NodeInfo>>() {
            @Override public List<NodeInfo> call() throws Exception {
                String threadName = String.format("Restart-handler-%s", BenchmarkUtils.hms());

                Thread.currentThread().setName(threadName);

                return restart(forRestart, cfgStr, NodeType.SERVER);
            }
        });

        if(runCtx.restartContext(NodeType.DRIVER) != null)
            restart(drvrRes, cfgStr, NodeType.DRIVER);

        waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

        restFut.cancel(true);

        restServ.shutdown();
    }

    /**
     *
     */
    void afterExecution() {
        new CollectWorker(runCtx, runCtx.getHostSet()).workOnHosts();

        createCharts();
    }

    /**
     * @return Logger.
     */
    protected Logger log() {
        return LogManager.getLogger(getClass().getSimpleName());
    }

    /**
     *
     */
    private void createCharts() {
        String mainResDir = Paths.get(runCtx.localeWorkDirectory(),
            " output",
            String.format("result-%s", runCtx.mainDateTime())
        ).toString();

        log().info(String.format("Creating charts for result directory '%s'.", mainResDir));

        String cp = String.format("%s%s%s",
            Paths.get(runCtx.localeWorkDirectory(), "libs").toString(),
            File.separator,
            "*");

        String mainCls = "org.yardstickframework.report.jfreechart.JFreeChartGraphPlotter";

        String jvmOpts = "-Xmx1g";

        String stdCharts = String.format("%s -cp %s %s -gm STANDARD -i %s", jvmOpts, cp, mainCls, mainResDir);

        runCtx.handler().runLocalJava(stdCharts);

        String charts = String.format("%s -cp %s %s -i %s", jvmOpts, cp, mainCls, mainResDir);

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
     * @param type Node type.
     * @param cfgStr Config string.
     * @return List of {@code NodeInfo} objects.
     */
    protected List<NodeInfo> startNodes(NodeType type, String cfgStr) {
        List<NodeInfo> nodeList = runCtx.getNodeInfos(type);

        if(nodeList.isEmpty()){
            log().info(String.format("%s_HOSTS property is not set. Will not start %s nodes.", type,
                type.toString().toLowerCase()));

            return nodeList;
        }

        NodeWorker startNodeWorker = new StartNodeWorker(runCtx, nodeList, cfgStr);

        return startNodeWorker.workForNodes();
    }

    /**
     * @param list List of {@code NodeInfo} objects.
     */
    private void checkLogs(List<NodeInfo> list) {
        NodeWorker checkWorker = new CheckLogWorker(runCtx, list);

        List<NodeInfo> resList = checkWorker.workForNodes();

        exitIfNodeNotRunning(resList);
    }

    /**
     * @param nodeList List of {@code NodeInfo} objects.
     * @param expStatus Expected status.
     */
    private void waitForNodes(List<NodeInfo> nodeList, NodeStatus expStatus) {
        NodeWorker waitWorker = new WaitNodeWorker(runCtx, nodeList, expStatus);

        List<NodeInfo> resList = waitWorker.workForNodes();

        if (expStatus == NodeStatus.RUNNING)
            exitIfNodeNotRunning(resList);
    }

    /**
     *
     * @param nodeList List of {@code NodeInfo} objects.
     */
    private void exitIfNodeNotRunning(List<NodeInfo> nodeList){
        for (NodeInfo nodeInfo : nodeList){
            if (nodeInfo.nodeStatus() != NodeStatus.RUNNING){
                new CollectWorker(runCtx, runCtx.getHostSet()).workOnHosts();

                runCtx.exitCode(1);

                System.exit(runCtx.exitCode());
            }
        }
    }

    /**
     * @param nodeList Node list.
     * @return List of nodes.
     */
    private List<NodeInfo> stopNodes(List<NodeInfo> nodeList) {
        NodeWorker stopWorker = new StopNodeWorker(runCtx, nodeList);

        stopWorker.workForNodes();

        return null;
    }

    /**
     * @param nodeList Node list.
     * @param cfgStr Config string.
     * @param type Node type.
     * @return List of nodes.
     */
    private List<NodeInfo> restart(List<NodeInfo> nodeList, String cfgStr, NodeType type) {
        if (runCtx.restartContext(type) == null)
            return nodeList;

        RestartNodeWorker restWorker = new RestartNodeWorker(runCtx, nodeList, cfgStr);

        restWorker.runAsyncOnHost(true);

        return restWorker.workForNodes();
    }

    /**
     * Checks work result list and exit if some of the check results return exit() == true.
     *
     * @param resList Work result list.
     */
    protected void checkRes(List<WorkResult> resList) {
        for (WorkResult wRes : resList) {
            CheckWorkResult cRes = (CheckWorkResult)wRes;

            if (cRes.exit())
                System.exit(runCtx.exitCode());
        }
    }
}
