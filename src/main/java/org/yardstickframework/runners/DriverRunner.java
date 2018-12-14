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

public class DriverRunner extends AbstractRunner {
    /**
     *
     * @param runCtx
     */
    public DriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        DriverRunner runner = new DriverRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        generalPrapare();

        for (String cfgStr : runCtx.getCfgList()) {
            List<NodeInfo> drvrRes = startNodes(NodeType.DRIVER, cfgStr);

            checkLogs(drvrRes);

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);
        }

        generalCleanUp();

        new CollectWorker(runCtx, runCtx.getFullUniqList()).workOnHosts();

        createCharts();

        return 0;
    }
}
