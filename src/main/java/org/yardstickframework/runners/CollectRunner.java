package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CollectWorker;

public class CollectRunner extends AbstractRunner {

    public CollectRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CollectRunner runner = new CollectRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        checkPlain(new CheckConnWorker(runCtx, runCtx.getFullUniqList()));

        List<NodeType> dockerList = runCtx.getNodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.collect(dockerList);

        new CollectWorker(runCtx, runCtx.getFullUniqList()).workOnHosts();

        return 0;
    }
}
