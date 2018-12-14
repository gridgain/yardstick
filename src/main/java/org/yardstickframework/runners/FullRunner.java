package org.yardstickframework.runners;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CheckJavaWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;
import org.yardstickframework.runners.workers.host.DeployWorker;
import org.yardstickframework.runners.workers.host.KillWorker;
import org.yardstickframework.runners.workers.node.CheckLogWorker;
import org.yardstickframework.runners.workers.node.NodeWorker;
import org.yardstickframework.runners.workers.node.RestartNodeWorker;
import org.yardstickframework.runners.workers.node.StartNodeWorker;
import org.yardstickframework.runners.workers.node.StopNodeWorker;
import org.yardstickframework.runners.workers.node.WaitNodeWorker;

public class FullRunner extends AbstractRunner {
    /**
     *
     * @param runCtx
     */
    public FullRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        FullRunner runner = new FullRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        generalPrapare();

        String cfgStr0 = runCtx.getProps().getProperty("CONFIGS").split(",")[0];

        List<NodeInfo> servRes = null;

        if (runCtx.startServersOnce())
            servRes = startNodes(NodeType.SERVER, cfgStr0);

        for (String cfgStr : runCtx.getCfgList()) {
            if (!runCtx.startServersOnce())
                servRes = startNodes(NodeType.SERVER, cfgStr);

            checkLogs(servRes);

            waitForNodes(servRes, NodeStatus.RUNNING);

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

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);

            restFut.cancel(true);

            restServ.shutdown();

            if (!runCtx.startServersOnce()) {
                stopNodes(servRes);

                waitForNodes(servRes, NodeStatus.NOT_RUNNING);
            }
        }

//        if(runCtx.startServersOnce()){
//            stopNodes(servRes);
//
//            waitForNodes(servRes, NodeStatus.NOT_RUNNING);
//        }

        generalCleanUp();

        new CollectWorker(runCtx, runCtx.getFullUniqList()).workOnHosts();

        createCharts();

        return 0;
    }

    private List<NodeInfo> restart(List<NodeInfo> nodeList, String cfgStr, NodeType type){
        if(runCtx.getRestartContext(type) == null)
            return nodeList;

        RestartNodeWorker restWorker = new RestartNodeWorker(runCtx, nodeList, cfgStr);

        restWorker.runAsyncOnHost(true);

        return restWorker.workForNodes();
    }

    private List<NodeInfo> stopNodes(List<NodeInfo> nodeList) {
        NodeWorker stopWorker = new StopNodeWorker(runCtx, nodeList);

        stopWorker.workForNodes();

        return null;
    }
}
