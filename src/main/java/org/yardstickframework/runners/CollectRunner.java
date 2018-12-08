package org.yardstickframework.runners;

import java.util.List;

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
