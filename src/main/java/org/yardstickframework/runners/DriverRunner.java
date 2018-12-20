package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeInfo;
import org.yardstickframework.runners.context.NodeStatus;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.host.CollectWorker;

public class DriverRunner extends AbstractRunner {
    /**
     *
     * @param runCtx Run context.
     */
    public DriverRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        DriverRunner runner = new DriverRunner(runCtx);

        runner.run0();
    }

    /**
     *
     * @return Exit code. TODO implement exit code return.
     */
    private int run0() {
        generalPrepare();

        for (String cfgStr : runCtx.configList()) {
            List<NodeInfo> drvrRes = startNodes(NodeType.DRIVER, cfgStr);

            checkLogs(drvrRes);

            waitForNodes(drvrRes, NodeStatus.RUNNING);

            waitForNodes(drvrRes, NodeStatus.NOT_RUNNING);
        }

        generalCleanUp();

        new CollectWorker(runCtx, runCtx.getFullUniqueList()).workOnHosts();

        createCharts();

        return 0;
    }
}
