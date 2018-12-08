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
        checkPlain(new CheckConnWorker(runCtx, runCtx.getFullUniqList()));

        List<NodeType> dockerList = runCtx.getNodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }

        HostWorker killWorker = new KillWorker(runCtx, runCtx.getFullUniqList());

        killWorker.workOnHosts();

        HostWorker cleanWorker = new CleanRemDirWorker(runCtx, runCtx.getFullUniqList());

        cleanWorker.workOnHosts();

        return 0;
    }
}
