package org.yardstickframework.runners;

import java.util.List;

public class CleanUpRunner  extends AbstractRunner {

    public CleanUpRunner(RunContext runCtx) {
        super(runCtx);
    }

    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CleanUpRunner runner = new CleanUpRunner(runCtx);

        runner.run1();
    }

    public int run1() {
        checkPlain(new CheckConnWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList())));

        List<NodeType> dockerList = runCtx.getNodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty())
            dockerRunner.cleanUp(dockerList, "after");

        Worker killWorker = new KillWorker(runCtx, new CommonWorkContext(runCtx.getFullUniqList()));

        killWorker.workOnHosts();

        return 0;
    }
}
