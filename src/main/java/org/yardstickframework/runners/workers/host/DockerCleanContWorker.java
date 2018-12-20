package org.yardstickframework.runners.workers.host;

import java.util.Set;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanContWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanContWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        removeContainers(host);

        return null;
    }
}
