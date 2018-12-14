package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;

public class CollectRunner extends AbstractRunner {
    /**
     *
     * @param runCtx Run context.
     */
    public CollectRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CollectRunner runner = new CollectRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        checkPlain(new CheckConnWorker(runCtx, runCtx.getFullUniqueList()));

        List<NodeType> dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.collect(dockerList);

        new CollectWorker(runCtx, runCtx.getFullUniqueList()).workOnHosts();

        return 0;
    }
}
