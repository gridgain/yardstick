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
