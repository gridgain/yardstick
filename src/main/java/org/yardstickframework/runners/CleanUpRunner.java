package org.yardstickframework.runners;

import java.util.List;
import org.yardstickframework.runners.context.NodeType;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.context.RunMode;
import org.yardstickframework.runners.workers.host.CheckConnWorker;
import org.yardstickframework.runners.workers.host.CleanRemDirWorker;
import org.yardstickframework.runners.workers.host.HostWorker;
import org.yardstickframework.runners.workers.host.KillWorker;

/**
 * Cleans up docker on hosts.
 */
public class CleanUpRunner  extends Runner {
    /** {@inheritDoc} */
    private CleanUpRunner(RunContext runCtx) {
        super(runCtx);
    }

    /**
     *
     * @param args Command line arguments.
     */
    public static void main(String[] args) {
        RunContext runCtx = RunContext.getRunContext(args);

        CleanUpRunner runner = new CleanUpRunner(runCtx);

        runner.run0();
    }

    /**
     *
     * @return Exit code.
     */
    @Override protected int run0() {
        checkPlain(new CheckConnWorker(runCtx, runCtx.getHostSet()));

        List<NodeType> dockerList = runCtx.nodeTypes(RunMode.DOCKER);

        DockerRunner dockerRunner = new DockerRunner(runCtx);

        if (!dockerList.isEmpty()) {
            dockerRunner.check(dockerList);

            dockerRunner.cleanUp(dockerList, "after");
        }

        HostWorker killWorker = new KillWorker(runCtx, runCtx.getHostSet());

        killWorker.workOnHosts();

        HostWorker cleanWorker = new CleanRemDirWorker(runCtx, runCtx.getHostSet());

        cleanWorker.workOnHosts();

        return 0;
    }
}
