package org.yardstickframework.runners.workers.host;

import java.util.Set;
import org.yardstickframework.runners.workers.CheckWorkResult;
import org.yardstickframework.runners.workers.WorkResult;
import org.yardstickframework.runners.context.RunContext;

/**
 * Parent for check workers.
 */
public abstract class CheckWorker extends HostWorker {
    /** {@inheritDoc} */
    CheckWorker(RunContext runCtx, Set<String> hostSet) {
        super(runCtx, hostSet);
    }

    /** {@inheritDoc} */
    @Override public void afterWork() {
        for(WorkResult res : resList()){
            CheckWorkResult checkRes = (CheckWorkResult) res;

            if(checkRes.exit())
                System.exit(1);
        }
    }
}
