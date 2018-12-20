package org.yardstickframework.runners.workers.host;

import java.util.Set;
import org.yardstickframework.runners.context.RunContext;
import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanImagesWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanImagesWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }
}
