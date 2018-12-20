package org.yardstickframework.runners;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.yardstickframework.BenchmarkUtils;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.host.CollectWorker;
import org.yardstickframework.runners.workers.node.NodeWorker;
import org.yardstickframework.runners.workers.node.RestartNodeWorker;
import org.yardstickframework.runners.workers.node.StopNodeWorker;

public class FullRunner extends AbstractRunner {
    /**
     *
     * @param runCtx Run context.
     */
    FullRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        FullRunner runner = new FullRunner(runCtx);

        runner.run0();
    }

    /**
     *
     * @return Exit code. TODO implement exit code return.
     */
    private int run0() {
        generalPrepare();

        String cfgStr0 = runCtx.properties().getProperty("CONFIGS").split(",")[0];

        List<NodeInfo> servRes = null;

        if (runCtx.startServersOnce())
            servRes = startNodes(NodeType.SERVER, cfgStr0);

        for (String cfgStr : runCtx.configList()) {
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

        if(runCtx.startServersOnce()){
            stopNodes(servRes);

            waitForNodes(servRes, NodeStatus.NOT_RUNNING);
        }

        generalCleanUp();

        new CollectWorker(runCtx, runCtx.getFullUniqueList()).workOnHosts();

        createCharts();

        return 0;
    }

    /**
     *
     * @param nodeList Node list.
     * @param cfgStr Config string.
     * @param type Node type.
     * @return List of nodes.
     */
    private List<NodeInfo> restart(List<NodeInfo> nodeList, String cfgStr, NodeType type){
        if(runCtx.restartContext(type) == null)
            return nodeList;

        RestartNodeWorker restWorker = new RestartNodeWorker(runCtx, nodeList, cfgStr);

        restWorker.runAsyncOnHost(true);

        return restWorker.workForNodes();
    }

    /**
     *
     * @param nodeList Node list.
     * @return List of nodes.
     */
    private List<NodeInfo> stopNodes(List<NodeInfo> nodeList) {
        NodeWorker stopWorker = new StopNodeWorker(runCtx, nodeList);

        stopWorker.workForNodes();

        return null;
    }
}
