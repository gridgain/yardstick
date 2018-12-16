package org.yardstickframework.runners.workers.host;

import java.util.List;
import org.yardstickframework.runners.context.RunContext;

import org.yardstickframework.runners.workers.WorkResult;

/**
 * Cleans docker containers.
 */
public class DockerCleanImagesWorker extends DockerHostWorker {
    /** {@inheritDoc} */
    public DockerCleanImagesWorker(RunContext runCtx, List<String> hostList) {
        super(runCtx, hostList);
    }

    /** {@inheritDoc} */
    @Override public WorkResult doWork(String host, int cnt) {
        removeImages(host);

        return null;
    }
}
