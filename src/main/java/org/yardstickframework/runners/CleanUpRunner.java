package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CleanRemDirWorker;
import org.yardstickframework.runners.workers.host.HostWorker;
import org.yardstickframework.runners.workers.host.KillWorker;

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
        checkPlain(new CheckConnWorker(runCtx, runCtx.getFullUniqueList()));

        List<NodeType> dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }

        HostWorker killWorker = new KillWorker(runCtx, runCtx.getFullUniqueList());

        killWorker.workOnHosts();

        HostWorker cleanWorker = new CleanRemDirWorker(runCtx, runCtx.getFullUniqueList());

        cleanWorker.workOnHosts();

        return 0;
    }
}
